package appbot.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appbot.AppliedBotanics;
import appbot.ae2.ManaKey;
import appbot.ae2.ManaKeyType;
import appbot.common.ABItems;

import ae2.api.client.AEKeyRendering;
import ae2.items.storage.BasicStorageCell;
import ae2.items.tools.powered.AbstractPortableCell;

import java.util.Objects;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(modid = AppliedBotanics.MOD_ID, value = Side.CLIENT)
public interface AppliedBotanicsClient {

    static void initialize() {
        AEKeyRendering.register(ManaKeyType.TYPE, ManaKey.class, new ManaRenderer());
        registerItemColors(Minecraft.getMinecraft().getItemColors());
    }

    static void registerItemColors(ItemColors itemColors) {
        for (var tier : ABItems.Tier.values()) {
            itemColors.registerItemColorHandler(BasicStorageCell::getColor, ABItems.get(tier));
            itemColors.registerItemColorHandler(AbstractPortableCell::getColor, ABItems.getPortable(tier));
        }
    }

    @SubscribeEvent
    static void registerModels(ModelRegistryEvent event) {
        registerInventoryModel(ABItems.MANA_CELL_HOUSING);
        registerInventoryModel(ABItems.MANA_CELL_CREATIVE);
        registerInventoryModel(ABItems.MANA_CELL_1K);
        registerInventoryModel(ABItems.MANA_CELL_4K);
        registerInventoryModel(ABItems.MANA_CELL_16K);
        registerInventoryModel(ABItems.MANA_CELL_64K);
        registerInventoryModel(ABItems.MANA_CELL_256K);
        registerInventoryModel(ABItems.PORTABLE_MANA_CELL_1K);
        registerInventoryModel(ABItems.PORTABLE_MANA_CELL_4K);
        registerInventoryModel(ABItems.PORTABLE_MANA_CELL_16K);
        registerInventoryModel(ABItems.PORTABLE_MANA_CELL_64K);
        registerInventoryModel(ABItems.PORTABLE_MANA_CELL_256K);
        registerInventoryModel(ABItems.MANA_P2P_TUNNEL);
    }

    private static void registerInventoryModel(Item item) {
        ModelLoader.setCustomModelResourceLocation(item, 0,
                new ModelResourceLocation(Objects.requireNonNull(item.getRegistryName()), "inventory"));
    }
}
