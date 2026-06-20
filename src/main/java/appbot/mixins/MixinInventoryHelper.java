package appbot.mixins;

import ae2.api.AECapabilities;
import appbot.botania.MECorporeaItemHandler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vazkii.botania.common.core.helper.InventoryHelper;

@Mixin(value = InventoryHelper.class, remap = false)
public abstract class MixinInventoryHelper {

    @Inject(method = "getInventory", at = @At("RETURN"), cancellable = true)
    private static void appbot$getMEStorageInventory(World world, BlockPos pos, EnumFacing side,
                                                     CallbackInfoReturnable<IItemHandler> cir) {
        if (cir.getReturnValue() != null) {
            return;
        }

        TileEntity tile = world.getTileEntity(pos);
        if (tile == null || tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side)
                || tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)) {
            return;
        }

        if (tile.hasCapability(AECapabilities.ME_STORAGE, side)) {
            var storage = tile.getCapability(AECapabilities.ME_STORAGE, side);
            if (storage != null) {
                cir.setReturnValue(new MECorporeaItemHandler(storage));
            }
        }
    }
}
