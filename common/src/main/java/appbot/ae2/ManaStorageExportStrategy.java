package appbot.ae2;

import com.google.common.primitives.Ints;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

import appbot.AppliedBotanics;
import appbot.Lookup;
import vazkii.botania.api.mana.ManaReceiver;

import appeng.api.behaviors.StackExportStrategy;
import appeng.api.behaviors.StackTransferContext;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.stacks.AEKey;

@SuppressWarnings("UnstableApiUsage")
public class ManaStorageExportStrategy implements StackExportStrategy {

    private final Lookup<ManaReceiver, Direction> apiCache;
    private final Direction fromSide;

    public ManaStorageExportStrategy(ServerLevel level,
            BlockPos fromPos,
            Direction fromSide) {
        this.apiCache = AppliedBotanics.getInstance().manaReceiver(level, fromPos);
        this.fromSide = fromSide;
    }

    @Override
    public long transfer(StackTransferContext context, AEKey what, long amount) {
        if (!(what instanceof ManaKey)) {
            return 0;
        }

        var receiver = apiCache.find(fromSide);

        if (receiver == null) {
            return 0;
        }

        // expanded StorageHelper.poweredExtraction to insert mid probe
        var energy = context.getEnergySource();
        var inv = context.getInternalStorage().getInventory();
        var request = ManaKey.KEY;
        var src = context.getActionSource();

        var retrieved = inv.extract(request, amount, Actionable.SIMULATE, src);

        var energyFactor = Math.max(1.0, request.getAmountPerOperation());
        var availablePower = energy.extractAEPower(retrieved / energyFactor, Actionable.SIMULATE,
                PowerMultiplier.CONFIG);
        var itemToExtract = (int) Math.min((long) (availablePower * energyFactor + 0.9), retrieved);

        if (itemToExtract == 0) {
            return 0;
        }

        // probed plausible amount above, insert and measure
        var prevMana = receiver.getCurrentMana();
        receiver.receiveMana(itemToExtract);
        var inserted = Math.abs(receiver.getCurrentMana() - prevMana);

        // This is to prevent ManaReceivers that have a constant capacity from
        // either duping (mana splitter) or causing other unintended issues with mana
        if (inserted == 0) {
            inserted = itemToExtract;
        }

        energy.extractAEPower(inserted / energyFactor, Actionable.MODULATE, PowerMultiplier.CONFIG);
        return inv.extract(request, inserted, Actionable.MODULATE, src);
    }

    @Override
    public long push(AEKey what, long amount, Actionable mode) {
        if (!(what instanceof ManaKey)) {
            return 0;
        }

        var receiver = apiCache.find(fromSide);

        if (receiver == null || receiver.isFull()) {
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

        return inserted;
    }
}
