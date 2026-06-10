package appbot.ae2;

import vazkii.botania.api.mana.IManaCollector;
import vazkii.botania.api.mana.IManaPool;
import vazkii.botania.api.mana.IManaReceiver;
import vazkii.botania.api.mana.spark.ISparkAttachable;
import vazkii.botania.common.block.tile.mana.TilePool;

public class ManaHelper {

    @SuppressWarnings("unused")
    public static int getCapacity(IManaReceiver receiver) {
        if (receiver instanceof IManaPool pool && pool.isOutputtingPower()) {
            return receiver.getCurrentMana();
        } else if (receiver instanceof ISparkAttachable sparkAttachable) {
            return receiver.getCurrentMana() + sparkAttachable.getAvailableSpaceForMana();
        } else if (receiver instanceof IManaPool pool) {
            return receiver.getCurrentMana() + TilePool.MAX_MANA;
        } else if (receiver instanceof IManaCollector collector) {
            return collector.getMaxMana();
        } else if (!receiver.isFull()) {
            return receiver.getCurrentMana() + 1000;
        }

        return 0;
    }
}
