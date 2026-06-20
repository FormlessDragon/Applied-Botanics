package appbot.ae2;

import java.util.HashSet;
import java.util.Set;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.tileentity.TileEntity;

import vazkii.botania.api.internal.VanillaPacketDispatcher;
import vazkii.botania.api.mana.IManaReceiver;
import vazkii.botania.api.mana.spark.ISparkAttachable;

import ae2.api.config.Actionable;

public interface SafeMana {

    int appbot$insert(int amount, Actionable mode);

    int appbot$extract(int amount, Actionable mode);

    static <R extends IManaReceiver & ISparkAttachable> int insertExcess(R be, int amount, Actionable mode) {
        if (!be.canRecieveManaFromBursts()) {
            return 0;
        }

        amount = Math.min(amount, be.getAvailableSpaceForMana());

        if (mode == Actionable.MODULATE && amount != 0) {
            be.recieveMana(amount);
            notifyClients(be);
        }

        return amount;
    }

    static void notifyClients(IManaReceiver be) {
        if (be instanceof TileEntity tile) {
            VanillaPacketDispatcher.dispatchTEToNearbyPlayers(tile);
        }
    }

    @Contract("null->null; !null->!null")
    @Nullable
    static SafeMana conv(@Nullable IManaReceiver be) {
        if (be == null) {
            return null;
        }

        if (be instanceof SafeMana m) {
            return m;
        }

        return Fail.make(be);
    }
}

class Fail {
    private static final Logger LOGGER = LoggerFactory.getLogger(Fail.class);
    private static final Set<String> KNOWN = new HashSet<>();

    static SafeMana make(IManaReceiver be) {
        if (KNOWN.add(be.getClass().getName())) {
            LOGGER.error(
                    "Applied Botanics does not know how to appbot$insert and appbot$extract Mana out of {} ({}). No extraction will be permitted, and insertions could be widely lossy.",
                    be,
                    be.getClass().getName());
        }

        return new SafeMana() {
            @Override
            public int appbot$insert(int amount, Actionable mode) {
                if (mode == Actionable.SIMULATE) {
                    return Math.min(amount, ManaHelper.getCapacity(be) - be.getCurrentMana());
                }

                if (be.isFull()) {
                    return 0;
                }

                var inserted = Math.clamp(ManaHelper.getCapacity(be) - be.getCurrentMana(), 0, amount);

                if (inserted != 0) {
                    be.recieveMana(inserted);
                    SafeMana.notifyClients(be);
                }

                return inserted;
            }

            @Override
            public int appbot$extract(int amount, Actionable mode) {
                return 0;
            }
        };
    }
}
