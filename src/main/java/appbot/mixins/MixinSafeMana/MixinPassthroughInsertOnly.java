package appbot.mixins.MixinSafeMana;

import ae2.api.config.Actionable;
import appbot.ae2.ManaHelper;
import appbot.ae2.SafeMana;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import vazkii.botania.api.mana.IManaReceiver;
import vazkii.botania.common.block.tile.mana.TileDistributor;
import vazkii.botania.common.block.tile.mana.TileManaVoid;

@SuppressWarnings("unused")
@Mixin(value = { TileDistributor.class, TileManaVoid.class }, remap = false)
class MixinPassthroughInsertOnly implements SafeMana {

    @Override
    public int appbot$insert(int amount, @NonNull Actionable mode) {
        var be = (IManaReceiver) this;

        if (!be.canRecieveManaFromBursts()) {
            return 0;
        }

        if (mode == Actionable.SIMULATE) {
            return Math.min(amount, ManaHelper.getCapacity(be) - be.getCurrentMana());
        }

        if (be.isFull()) {
            return 0;
        }

        be.recieveMana(amount);
        return amount;
    }

    @Override
    public int appbot$extract(int amount, @NonNull Actionable mode) {
        return 0;
    }
}
