package appbot.block;

import javax.annotation.Nullable;

import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import appbot.AppliedBotanics;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.state.BotaniaStateProps;
import vazkii.botania.api.state.enums.PoolVariant;
import vazkii.botania.common.block.mana.BlockPool;
import vazkii.botania.common.core.BotaniaCreativeTab;

public class FluixPool extends BlockPool {

    public FluixPool() {
        super();
        /*this.setHardness(2.0F);
        this.setResistance(10.0F);
        this.setSoundType(SoundType.STONE);
        BotaniaAPI.blacklistBlockFromMagnet(this, 32767);*/

        //setRegistryName(AppliedBotanics.id("fluix_mana_pool"));
        setTranslationKey(AppliedBotanics.MOD_ID + ".fluix_mana_pool");
        setCreativeTab(BotaniaCreativeTab.INSTANCE);
        setDefaultState(getDefaultState().withProperty(BotaniaStateProps.POOL_VARIANT, PoolVariant.FABULOUS));
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new FluixPoolBlockEntity();
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, @Nullable EntityLivingBase placer,
            ItemStack stack) {
        if (placer instanceof EntityPlayer player && world.getTileEntity(pos) instanceof FluixPoolBlockEntity tile) {
            tile.getMainNode().setOwningPlayer(player);
        }
    }

    @Override
    public int getComparatorInputOverride(IBlockState state, World world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        return tile instanceof FluixPoolBlockEntity pool ? pool.calculateComparatorLevel() : 0;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }
}
