package appbot.mixins.MixinSafeMana;

import ae2.api.config.Actionable;
import appbot.ae2.ManaHelper;
import appbot.ae2.SafeMana;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import vazkii.botania.api.mana.IManaReceiver;
import vazkii.botania.common.block.tile.TileBrewery;
import vazkii.botania.common.block.tile.TileEnchanter;
import vazkii.botania.common.block.tile.TileRuneAltar;
import vazkii.botania.common.block.tile.TileSpawnerClaw;
import vazkii.botania.common.block.tile.mana.TileRFGenerator;
import vazkii.botania.common.block.tile.mana.TileSpreader;

@SuppressWarnings("unused")
@Mixin(value = { TileSpreader.class, TileRFGenerator.class, TileRuneAltar.class, TileBrewery.class,
        TileEnchanter.class, TileSpawnerClaw.class }, remap = false)
class MixinClampedInsertOnly implements SafeMana {

    @Override
    public int appbot$insert(int amount, @NotNull Actionable mode) {
        var be = (IManaReceiver) this;

        if (!be.canRecieveManaFromBursts()) {
            return 0;
        }

        if (mode == Actionable.SIMULATE) {
            return Math.min(amount, ManaHelper.getCapacity(be) - be.getCurrentMana());
        }

        var old = be.getCurrentMana();
        be.recieveMana(amount);
        var inserted = be.getCurrentMana() - old;

        if (inserted != 0) {
            SafeMana.notifyClients(be);
        }

        return inserted;
    }

    @Override
    public int appbot$extract(int amount, @NotNull Actionable mode) {
        return 0;
    }
}
