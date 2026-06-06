package appbot;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldServer;
import net.minecraft.item.Item;
import net.minecraft.block.Block;

import appbot.forge.AppliedBotanicsImpl;
import vazkii.botania.api.mana.IManaReceiver;

import ae2.container.GuiIds;
import ae2.api.storage.MEStorage;

public interface AppliedBotanics {

    String MOD_ID = "appbot";

    static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    static AppliedBotanics getInstance() {
        return AppliedBotanicsImpl.getInstance();
    }

    Lookup<MEStorage, EnumFacing> meStorage(WorldServer level, BlockPos pos);

    Lookup<IManaReceiver, EnumFacing> manaReceiver(WorldServer level, BlockPos pos);

    Block fluixManaPool();

    Item manaCellHousing();

    GuiIds.GuiKey portableCellMenu();
}
