package appbot.mixins.MixinSafeMana;

import ae2.api.config.Actionable;
import appbot.ae2.SafeMana;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import vazkii.botania.common.block.tile.TileTerraPlate;

@SuppressWarnings("unused")
@Mixin(value = TileTerraPlate.class, remap = false)
class MixinTerraPlate implements SafeMana {

    @Override
    public int appbot$insert(int amount, @NonNull Actionable mode) {
        return SafeMana.insertExcess((TileTerraPlate) (Object) this, amount, mode);
    }

    @Override
    public int appbot$extract(int amount, @NonNull Actionable mode) {
        return 0;
    }
}
