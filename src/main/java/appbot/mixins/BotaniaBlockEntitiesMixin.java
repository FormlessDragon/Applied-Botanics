package appbot.mixins;

import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

import java.util.function.BiFunction;

import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.block.state.IBlockState;

import appbot.AppliedBotanics;
import appbot.block.FluixPoolBlockEntity;
import vazkii.botania.common.block.block_entity.BotaniaBlockEntities;
import vazkii.botania.common.lib.LibBlockNames;

@Mixin(value = BotaniaBlockEntities.class, remap = false)
public class BotaniaBlockEntitiesMixin {

    @ModifyArg(method = "<clinit>", at = @At(value = "INVOKE", target = "Lvazkii/botania/common/block/block_entity/BotaniaBlockEntities;type(Lnet/minecraft/resources/ResourceLocation;Ljava/util/function/BiFunction;[Lnet/minecraft/world/World/block/Block;)Lnet/minecraft/world/World/block/entity/BlockEntityType;", remap = true), index = 1)
    private static <T extends TileEntity> BiFunction<BlockPos, IBlockState, T> injectConstructor(ResourceLocation id,
            BiFunction<BlockPos, IBlockState, T> func,
            Block... blocks) {
        if (id.equals(prefix(LibBlockNames.POOL))) {
            return (blockPos, IBlockState) -> {
                if (IBlockState.is(AppliedBotanics.getInstance().fluixManaPool())) {
                    // noinspection unchecked
                    return (T) new FluixPoolBlockEntity(blockPos, IBlockState);
                } else {
                    return func.apply(blockPos, IBlockState);
                }
            };
        }

        return func;
    }

    @SuppressWarnings("InvalidInjectorMethodSignature")
    @ModifyArg(method = "<clinit>", at = @At(value = "INVOKE", target = "Lvazkii/botania/common/block/block_entity/BotaniaBlockEntities;type(Lnet/minecraft/resources/ResourceLocation;Ljava/util/function/BiFunction;[Lnet/minecraft/world/World/block/Block;)Lnet/minecraft/world/World/block/entity/BlockEntityType;", remap = true), index = 2)
    private static <T extends TileEntity> Block[] add(ResourceLocation id, BiFunction<BlockPos, IBlockState, T> func,
            Block... blocks) {
        if (id.equals(prefix(LibBlockNames.POOL))) {
            blocks = ArrayUtils.add(blocks, AppliedBotanics.getInstance().fluixManaPool());
        }

        return blocks;
    }
}
