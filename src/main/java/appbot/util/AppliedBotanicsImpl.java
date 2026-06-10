package appbot.util;

import appbot.common.ABBlocks;
import appbot.common.ABItems;
import appbot.AppliedBotanics;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;

import appbot.ae2.ManaGenericStackInvStorage;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.api.mana.IManaReceiver;

import ae2.api.AECapabilities;
import ae2.api.storage.MEStorage;
import ae2.container.GuiIds;

@SuppressWarnings("UnstableApiUsage")
public enum AppliedBotanicsImpl implements AppliedBotanics {

    INSTANCE;

    public static AppliedBotanics getInstance() {
        return INSTANCE;
    }

    @Override
    public Lookup<MEStorage, EnumFacing> meStorage(@NotNull WorldServer world, @NotNull BlockPos pos) {
        return side -> {
            TileEntity tile = world.getTileEntity(pos);
            return tile == null ? null : tile.getCapability(AECapabilities.ME_STORAGE, side);
        };
    }

    @Override
    public Lookup<IManaReceiver, EnumFacing> manaReceiver(@NotNull WorldServer world, @NotNull BlockPos pos) {
        return side -> {
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof IManaReceiver receiver) {
                return receiver;
            }

            if (tile != null && tile.hasCapability(AECapabilities.GENERIC_INTERNAL_INV, side)) {
                var inventory = tile.getCapability(AECapabilities.GENERIC_INTERNAL_INV, side);
                if (inventory != null) {
                    return new ManaGenericStackInvStorage(inventory, world, pos);
                }
            }

            return null;
        };
    }

    @Override
    public Block fluixManaPool() {
        return ABBlocks.FLUIX_MANA_POOL;
    }

    @Override
    public Item manaCellHousing() {
        return ABItems.MANA_CELL_HOUSING;
    }

    @Override
    public GuiIds.GuiKey portableCellMenu() {
        return GuiIds.GuiKey.PORTABLE_ITEM_CELL;
    }
}
