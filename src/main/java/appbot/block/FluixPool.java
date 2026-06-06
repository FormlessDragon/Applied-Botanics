package appbot.block;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appbot.AppliedBotanics;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.internal.VanillaPacketDispatcher;
import vazkii.botania.api.lexicon.ILexiconable;
import vazkii.botania.api.lexicon.LexiconEntry;
import vazkii.botania.api.state.BotaniaStateProps;
import vazkii.botania.api.state.enums.PoolVariant;
import vazkii.botania.api.wand.IWandHUD;
import vazkii.botania.api.wand.IWandable;
import vazkii.botania.client.core.handler.ModelHandler;
import vazkii.botania.client.render.IModelRegister;
import vazkii.botania.common.block.tile.mana.TilePool;
import vazkii.botania.common.core.BotaniaCreativeTab;
import vazkii.botania.common.lexicon.LexiconData;

public class FluixPool extends Block implements IWandHUD, IWandable, ILexiconable, IModelRegister {

    private static final AxisAlignedBB AABB = new AxisAlignedBB(0, 0, 0, 1, 0.5, 1);
    private static final AxisAlignedBB BOTTOM_AABB = new AxisAlignedBB(0, 0, 0, 1, 1 / 16.0, 1);
    private static final AxisAlignedBB NORTH_AABB = new AxisAlignedBB(0, 0, 15 / 16.0, 1, 0.5, 1);
    private static final AxisAlignedBB SOUTH_AABB = new AxisAlignedBB(0, 0, 0, 1, 0.5, 1 / 16.0);
    private static final AxisAlignedBB WEST_AABB = new AxisAlignedBB(0, 0, 0, 1 / 16.0, 0.5, 1);
    private static final AxisAlignedBB EAST_AABB = new AxisAlignedBB(15 / 16.0, 0, 0, 1, 0.5, 1);

    public FluixPool() {
        super(Material.ROCK);

        setRegistryName(AppliedBotanics.id("fluix_mana_pool"));
        setTranslationKey(AppliedBotanics.MOD_ID + ".fluix_mana_pool");
        setHardness(2.0F);
        setResistance(10.0F);
        setSoundType(SoundType.STONE);
        setCreativeTab(BotaniaCreativeTab.INSTANCE);
        BotaniaAPI.blacklistBlockFromMagnet(this, Short.MAX_VALUE);
        setDefaultState(blockState.getBaseState()
                .withProperty(BotaniaStateProps.POOL_VARIANT, PoolVariant.FABULOUS)
                .withProperty(BotaniaStateProps.COLOR, EnumDyeColor.WHITE));
    }

    @Nonnull
    @Override
    public BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, BotaniaStateProps.POOL_VARIANT, BotaniaStateProps.COLOR);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(BotaniaStateProps.POOL_VARIANT).ordinal();
    }

    @Nonnull
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState();
    }

    @Nonnull
    @Override
    public IBlockState getActualState(@Nonnull IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntity tile = world instanceof ChunkCache
                ? ((ChunkCache) world).getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK)
                : world.getTileEntity(pos);
        return tile instanceof TilePool pool
                ? state.withProperty(BotaniaStateProps.COLOR, pool.color)
                : state.withProperty(BotaniaStateProps.COLOR, EnumDyeColor.WHITE);
    }

    @Nonnull
    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return AABB;
    }

    @Override
    public int damageDropped(IBlockState state) {
        return 0;
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player,
            boolean willHarvest) {
        if (willHarvest) {
            return true;
        }
        return super.removedByPlayer(state, world, pos, player, false);
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state,
            int fortune) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TilePool pool && !pool.fragile) {
            super.getDrops(drops, world, pos, state, fortune);
        }
    }

    @Override
    public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity tile,
            ItemStack stack) {
        super.harvestBlock(world, player, pos, state, tile, stack);
        world.setBlockToAir(pos);
    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items) {
        items.add(new ItemStack(this));
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Nonnull
    @Override
    public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
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
    public void onEntityCollision(World world, BlockPos pos, IBlockState state, Entity entity) {
        if (entity instanceof EntityItem item && world.getTileEntity(pos) instanceof TilePool pool
                && pool.collideEntityItem(item)) {
            VanillaPacketDispatcher.dispatchTEToNearbyPlayers(world, pos);
        }
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, @Nonnull World world, @Nonnull BlockPos pos,
            @Nonnull AxisAlignedBB entityBox, @Nonnull List<AxisAlignedBB> boxes, Entity entity,
            boolean isActualState) {
        addCollisionBoxToList(pos, entityBox, boxes, BOTTOM_AABB);
        addCollisionBoxToList(pos, entityBox, boxes, NORTH_AABB);
        addCollisionBoxToList(pos, entityBox, boxes, SOUTH_AABB);
        addCollisionBoxToList(pos, entityBox, boxes, WEST_AABB);
        addCollisionBoxToList(pos, entityBox, boxes, EAST_AABB);
    }

    @Override
    public boolean isSideSolid(IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos,
            EnumFacing side) {
        return side == EnumFacing.DOWN;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean hasComparatorInputOverride(IBlockState state) {
        return true;
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

    @SideOnly(Side.CLIENT)
    @Override
    public void renderHUD(Minecraft mc, ScaledResolution res, World world, BlockPos pos) {
        if (world.getTileEntity(pos) instanceof TilePool pool) {
            pool.renderHUD(mc, res);
        }
    }

    @Override
    public boolean onUsedByWand(EntityPlayer player, ItemStack stack, World world, BlockPos pos, EnumFacing side) {
        if (world.getTileEntity(pos) instanceof TilePool pool) {
            pool.onWanded(player, stack);
            return true;
        }
        return false;
    }

    @Override
    public LexiconEntry getEntry(World world, BlockPos pos, EntityPlayer player, ItemStack lexicon) {
        return LexiconData.pool;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerModels() {
        ModelLoader.setCustomStateMapper(this, new StateMap.Builder().ignore(BotaniaStateProps.COLOR).build());
        ModelHandler.registerBlockToState(this, PoolVariant.values().length);
    }

    @Override
    public boolean eventReceived(IBlockState state, World world, BlockPos pos, int id, int param) {
        super.eventReceived(state, world, pos, id, param);
        TileEntity tile = world.getTileEntity(pos);
        return tile != null && tile.receiveClientEvent(id, param);
    }

    @Nonnull
    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing side) {
        return side == EnumFacing.DOWN ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED;
    }
}
