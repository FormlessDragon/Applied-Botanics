package appbot.ae2;

import com.google.common.primitives.Ints;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

import appbot.AppliedBotanics;
import appbot.Lookup;
import vazkii.botania.api.mana.ManaReceiver;

import appeng.api.behaviors.ExternalStorageStrategy;
import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import appeng.core.localization.GuiText;

@SuppressWarnings("UnstableApiUsage")
public class ManaExternalStorageStrategy implements ExternalStorageStrategy {

    private final Lookup<ManaReceiver, Direction> apiCache;
    private final Direction fromSide;

    public ManaExternalStorageStrategy(ServerLevel level, BlockPos fromPos, Direction fromSide) {
        this.apiCache = AppliedBotanics.getInstance().manaReceiver(level, fromPos);
        this.fromSide = fromSide;
    }

    @Nullable
    @Override
    public MEStorage createWrapper(boolean extractableOnly, Runnable injectOrExtractCallback) {
        var receiver = apiCache.find(fromSide);

        if (receiver == null) {
            // If storage is absent, never query again until the next update.
            return null;
        }

        return new ManaStorageAdapter(receiver, injectOrExtractCallback);
    }

    private record ManaStorageAdapter(ManaReceiver receiver, Runnable changeListener) implements MEStorage {
        @Override
        public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
            if (!(what instanceof ManaKey)) {
                return 0;
            }

            if (receiver.isFull()) {
                return 0;
            }

            var amt = Ints.saturatedCast(amount);
            var prevMana = receiver.getCurrentMana();

            if (mode != Actionable.MODULATE) {
                // play safe and guess how much is insertable
                return Math.min(amt, ManaHelper.getCapacity(receiver) - prevMana);
            }

            // give the mana and measure
            receiver.receiveMana(amt);
            var inserted = Math.abs(receiver.getCurrentMana() - prevMana);

            // This is to prevent ManaReceivers that have a constant capacity from
            // either duping (mana splitter) or causing other unintended issues with mana
            if (inserted == 0) {
                inserted = amt;
            }

            if (inserted > 0) {
                changeListener.run();
            }

            return inserted;
        }

        @Override
        public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
            if (!(what instanceof ManaKey)) {
                return 0;
            }

            var extracted = (int) Math.min(amount, receiver.getCurrentMana());

            if (extracted > 0 && mode == Actionable.MODULATE) {
                receiver.receiveMana(-extracted);
                changeListener.run();
            }

            return extracted;
        }

        @Override
        public void getAvailableStacks(KeyCounter out) {
            var currentMana = receiver.getCurrentMana();

            if (currentMana != 0) {
                out.add(ManaKey.KEY, currentMana);
            }
        }

        @Override
        public Component getDescription() {
            return GuiText.ExternalStorage.text(ManaKeyType.TYPE.getDescription());
        }
    }
}
