package appbot.ae2;

import com.google.common.primitives.Ints;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;

import appbot.AppliedBotanics;
import appbot.util.Lookup;
import vazkii.botania.api.mana.IManaReceiver;

import ae2.api.behaviors.StackImportStrategy;
import ae2.api.behaviors.StackTransferContext;
import ae2.api.config.Actionable;
import ae2.core.AELog;

@SuppressWarnings("UnstableApiUsage")
public class ManaStorageImportStrategy implements StackImportStrategy {

    private final Lookup<IManaReceiver, EnumFacing> apiCache;
    private final EnumFacing fromSide;

    public ManaStorageImportStrategy(WorldServer level,
            BlockPos fromPos,
            EnumFacing fromSide) {
        this.apiCache = AppliedBotanics.getInstance().manaReceiver(level, fromPos);
        this.fromSide = fromSide;
    }

    @Override
    public boolean transfer(StackTransferContext context) {
        if (!context.isKeyTypeEnabled(ManaKeyType.TYPE)) {
            return false;
        }

        if (context.isInFilter(ManaKey.KEY) == context.isInverted()) {
            return false;
        }

        var receiver = SafeMana.conv(apiCache.find(fromSide));

        if (receiver == null) {
            return false;
        }

        var amountPerOperation = ManaKeyType.TYPE.getAmountPerOperation();
        long maxTransfer = context.getOperationsRemaining()
                * (long) amountPerOperation;

        if (maxTransfer <= 0) {
            return false;
        }

        // see how much we could take
        var inventory = context.getInternalStorage().getInventory();
        var simulate = inventory.insert(ManaKey.KEY, maxTransfer, Actionable.SIMULATE, context.getActionSource());

        // take up to that much
        var extracted = receiver.extract(Ints.saturatedCast(simulate), Actionable.MODULATE);

        // insert to network
        var inserted = inventory.insert(ManaKey.KEY, extracted, Actionable.MODULATE, context.getActionSource());

        if (inserted < extracted) {
            // try to give back overflow
            var difference = extracted - inserted;
            difference -= receiver.insert(Ints.saturatedCast(difference), Actionable.MODULATE);

            if (difference != 0) {
                AELog.warn("Extracted %d Mana from adjacent storage and voided it because network refused insert");
            }
        }

        context.reduceOperationsRemaining((inserted + amountPerOperation - 1) / amountPerOperation);
        return false;
    }
}
