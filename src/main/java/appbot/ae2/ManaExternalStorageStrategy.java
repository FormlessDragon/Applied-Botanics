package appbot.ae2;

import com.google.common.primitives.Ints;

import org.jetbrains.annotations.Nullable;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.WorldServer;

import appbot.AppliedBotanics;
import appbot.util.Lookup;
import vazkii.botania.api.mana.IManaReceiver;

import ae2.api.behaviors.ExternalStorageStrategy;
import ae2.api.config.Actionable;
import ae2.api.networking.security.IActionSource;
import ae2.api.stacks.AEKey;
import ae2.api.stacks.KeyCounter;
import ae2.api.storage.MEStorage;
import ae2.core.localization.GuiText;

@SuppressWarnings("UnstableApiUsage")
public class ManaExternalStorageStrategy implements ExternalStorageStrategy {

    private final Lookup<IManaReceiver, EnumFacing> apiCache;
    private final EnumFacing fromSide;

    public ManaExternalStorageStrategy(WorldServer level, BlockPos fromPos, EnumFacing fromSide) {
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

        return new ManaStorageAdapter(receiver, injectOrExtractCallback, extractableOnly);
    }

    private record ManaStorageAdapter(IManaReceiver receiver, Runnable changeListener, boolean extractableOnly)
            implements
                MEStorage {
        @Override
        public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
            if (!(what instanceof AEManaKey)) {
                return 0;
            }

            var inserted = SafeMana.conv(receiver).appbot$insert(Ints.saturatedCast(amount), mode);

            if (inserted > 0 && mode == Actionable.MODULATE) {
                changeListener.run();
            }

            return inserted;
        }

        @Override
        public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
            if (!(what instanceof AEManaKey)) {
                return 0;
            }

            var extracted = SafeMana.conv(receiver).appbot$extract(Ints.saturatedCast(amount), mode);

            if (extracted > 0 && mode == Actionable.MODULATE) {
                changeListener.run();
            }

            return extracted;
        }

        @Override
        public void getAvailableStacks(KeyCounter out) {
            var currentMana = receiver.getCurrentMana();

            if (currentMana != 0 && (!extractableOnly || SafeMana.conv(receiver).appbot$extract(1, Actionable.SIMULATE) > 0)) {
                out.add(AEManaKey.KEY, currentMana);
            }
        }

        @Override
        public ITextComponent getDescription() {
            return GuiText.ExternalStorage.text(AEManaKeyType.TYPE.getDescription());
        }
    }
}
