package appbot.mixins.MixinSafeMana;

import ae2.api.config.Actionable;
import appbot.ae2.SafeMana;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import vazkii.botania.common.block.tile.mana.TilePool;

@SuppressWarnings("unused")
@Mixin(value = TilePool.class, remap = false)
class MixinTilePool implements SafeMana {

    @Override
    public int appbot$insert(int amount, @NonNull Actionable mode) {
        var pool = (TilePool) (Object) this;
        return SafeMana.insertExcess(pool, amount, mode);
    }

    @Override
    public int appbot$extract(int amount, @NonNull Actionable mode) {
        var pool = (TilePool) (Object) this;

        var old = pool.getCurrentMana();
        if (mode == Actionable.SIMULATE) {
            return appbot$simulate(amount, old);
        }

        pool.recieveMana(-amount);
        return appbot$modulate(amount, old, pool.getCurrentMana());
    }

    @Unique
    private static int appbot$simulate(int amount, int currentMana) {
        return Math.min(amount, currentMana);
    }

    @Unique
    private static int appbot$modulate(int amount, int oldMana, int newMana) {
        var requested = Math.min(amount, oldMana);
        return Math.max(requested, oldMana - newMana);
    }
}