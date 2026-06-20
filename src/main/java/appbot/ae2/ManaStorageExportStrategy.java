package appbot.ae2;

import com.google.common.primitives.Ints;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;

import appbot.AppliedBotanics;
import appbot.util.Lookup;
import vazkii.botania.api.mana.IManaReceiver;

import ae2.api.behaviors.StackExportStrategy;
import ae2.api.behaviors.StackTransferContext;
import ae2.api.config.Actionable;
import ae2.api.stacks.AEKey;
import ae2.api.storage.StorageHelper;

public class ManaStorageExportStrategy implements StackExportStrategy {

    private static final Logger LOGGER = LoggerFactory.getLogger(ManaStorageExportStrategy.class);

    private final Lookup<IManaReceiver, EnumFacing> apiCache;
    private final EnumFacing fromSide;

    public ManaStorageExportStrategy(WorldServer level,
            BlockPos fromPos,
            EnumFacing fromSide) {
        this.apiCache = AppliedBotanics.getInstance().manaReceiver(level, fromPos);
        this.fromSide = fromSide;
    }

    @Override
    public long transfer(StackTransferContext context, AEKey what, long amount) {
        if (!(what instanceof AEManaKey)) {
            return 0;
        }

        var receiver = SafeMana.conv(apiCache.find(fromSide));

        if (receiver == null) {
            return 0;
        }

        var inv = context.getInternalStorage();

        long extracted;
        long wasInserted;

        extracted = StorageHelper.poweredExtraction(
                context.getEnergySource(),
                inv.getInventory(),
                what,
                amount,
                context.getActionSource(),
                Actionable.SIMULATE);

        wasInserted = receiver.appbot$insert(Ints.saturatedCast(extracted), Actionable.SIMULATE);

        if (wasInserted > 0) {
            extracted = StorageHelper.poweredExtraction(
                    context.getEnergySource(),
                    inv.getInventory(),
                    what,
                    wasInserted,
                    context.getActionSource(),
                    Actionable.MODULATE);

            wasInserted = receiver.appbot$insert(Ints.saturatedCast(extracted), Actionable.MODULATE);

            if (wasInserted < extracted) {
                // Be nice and try to give the overflow back
                long leftover = extracted - wasInserted;
                leftover -= inv.getInventory().insert(what, leftover, Actionable.MODULATE, context.getActionSource());

                if (leftover > 0) {
                    LOGGER.error("Storage export: adjacent block unexpectedly refused appbot$insert, voided {} Mana",
                            leftover);
                }
            }
        }

        return wasInserted;
    }

    @Override
    public long push(AEKey what, long amount, Actionable mode) {
        if (!(what instanceof AEManaKey)) {
            return 0;
        }

        var receiver = SafeMana.conv(apiCache.find(fromSide));

        if (receiver == null) {
            return 0;
        }

        return receiver.appbot$insert(Ints.saturatedCast(amount), mode);
    }
}
