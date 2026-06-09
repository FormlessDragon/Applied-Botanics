package appbot.mixins;

import org.spongepowered.asm.mixin.Mixin;

import appbot.ae2.SafeMana;
import vazkii.botania.common.block.tile.mana.TilePool;

import ae2.api.config.Actionable;

@Mixin(value = TilePool.class, remap = false)
public abstract class SafeManaMixin implements SafeMana {

    @Override
    public int insert(int amount, Actionable mode) {
        var pool = (TilePool) (Object) this;
        return SafeMana.insertExcess(pool, amount, mode);
    }

    @Override
    public int extract(int amount, Actionable mode) {
        var pool = (TilePool) (Object) this;

        var old = pool.getCurrentMana();
        if (mode == Actionable.SIMULATE) {
            return PoolManaExtraction.simulate(amount, old);
        }

        pool.recieveMana(-amount);
        return PoolManaExtraction.modulate(amount, old, pool.getCurrentMana());
    }
}
