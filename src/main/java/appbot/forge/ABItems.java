package appbot.forge;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import appbot.AppliedBotanics;
import appbot.item.CreativeManaCellItem;
import appbot.item.ManaCellItem;
import appbot.item.PortableManaCellItem;
import appbot.item.cell.CreativeManaCellHandler;
import appbot.item.cell.ManaCellHandler;

import ae2.api.client.StorageCellModels;
import ae2.api.storage.StorageCells;
import ae2.api.upgrades.Upgrades;
import ae2.core.AppEng;
import ae2.core.definitions.AEItems;
import ae2.core.localization.GuiText;

@Mod.EventBusSubscriber(modid = AppliedBotanics.MOD_ID)
public final class ABItems {

    public static final CreativeTabs CREATIVE_TAB = new CreativeTabs(AppliedBotanics.MOD_ID) {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(MANA_CELL_1K);
        }
    };

    public static final Item MANA_CELL_HOUSING = item("mana_cell_housing", new Item());
    public static final CreativeManaCellItem MANA_CELL_CREATIVE = item("creative_mana_cell", new CreativeManaCellItem());

    public static final ManaCellItem MANA_CELL_1K = item("mana_storage_cell_1k",
            new ManaCellItem(AEItems.CELL_COMPONENT_1K.item(), 1, 0.5f));
    public static final ManaCellItem MANA_CELL_4K = item("mana_storage_cell_4k",
            new ManaCellItem(AEItems.CELL_COMPONENT_4K.item(), 4, 1.0f));
    public static final ManaCellItem MANA_CELL_16K = item("mana_storage_cell_16k",
            new ManaCellItem(AEItems.CELL_COMPONENT_16K.item(), 16, 1.5f));
    public static final ManaCellItem MANA_CELL_64K = item("mana_storage_cell_64k",
            new ManaCellItem(AEItems.CELL_COMPONENT_64K.item(), 64, 2.0f));
    public static final ManaCellItem MANA_CELL_256K = item("mana_storage_cell_256k",
            new ManaCellItem(AEItems.CELL_COMPONENT_256K.item(), 256, 2.5f));

    public static final PortableManaCellItem PORTABLE_MANA_CELL_1K = item("portable_mana_storage_cell_1k",
            new PortableManaCellItem(1, 0.5));
    public static final PortableManaCellItem PORTABLE_MANA_CELL_4K = item("portable_mana_storage_cell_4k",
            new PortableManaCellItem(4, 1.0));
    public static final PortableManaCellItem PORTABLE_MANA_CELL_16K = item("portable_mana_storage_cell_16k",
            new PortableManaCellItem(16, 1.5));
    public static final PortableManaCellItem PORTABLE_MANA_CELL_64K = item("portable_mana_storage_cell_64k",
            new PortableManaCellItem(64, 2.0));
    public static final PortableManaCellItem PORTABLE_MANA_CELL_256K = item("portable_mana_storage_cell_256k",
            new PortableManaCellItem(256, 2.5));

    private ABItems() {
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(
                MANA_CELL_HOUSING,
                MANA_CELL_CREATIVE,
                MANA_CELL_1K,
                MANA_CELL_4K,
                MANA_CELL_16K,
                MANA_CELL_64K,
                MANA_CELL_256K,
                PORTABLE_MANA_CELL_1K,
                PORTABLE_MANA_CELL_4K,
                PORTABLE_MANA_CELL_16K,
                PORTABLE_MANA_CELL_64K,
                PORTABLE_MANA_CELL_256K);
    }

    public static void init() {
        StorageCells.addCellHandler(ManaCellHandler.INSTANCE);
        StorageCells.addCellHandler(new CreativeManaCellHandler());

        StorageCellModels.registerModel(MANA_CELL_CREATIVE, AppEng.makeId("block/drive/cells/creative_cell"));
        for (Tier tier : Tier.values()) {
            var cell = get(tier);
            var portable = getPortable(tier);

            Upgrades.add(AEItems.VOID_CARD.item(), cell, 1, GuiText.StorageCells.getTranslationKey());
            Upgrades.add(AEItems.ENERGY_CARD.item(), portable, 2, GuiText.PortableCells.getTranslationKey());

            var id = AppliedBotanics.id("block/drive/cells/" + cell.getRegistryName().getPath());
            StorageCellModels.registerModel(cell, id);
            StorageCellModels.registerModel(portable, id);
        }
    }

    public static ManaCellItem get(Tier tier) {
        return switch (tier) {
            case _1K -> MANA_CELL_1K;
            case _4K -> MANA_CELL_4K;
            case _16K -> MANA_CELL_16K;
            case _64K -> MANA_CELL_64K;
            case _256K -> MANA_CELL_256K;
        };
    }

    public static PortableManaCellItem getPortable(Tier tier) {
        return switch (tier) {
            case _1K -> PORTABLE_MANA_CELL_1K;
            case _4K -> PORTABLE_MANA_CELL_4K;
            case _16K -> PORTABLE_MANA_CELL_16K;
            case _64K -> PORTABLE_MANA_CELL_64K;
            case _256K -> PORTABLE_MANA_CELL_256K;
        };
    }

    private static <T extends Item> T item(String name, T item) {
        item.setRegistryName(AppliedBotanics.id(name));
        item.setTranslationKey(AppliedBotanics.MOD_ID + "." + name);
        item.setCreativeTab(CREATIVE_TAB);
        return item;
    }

    public enum Tier {
        _1K, _4K, _16K, _64K, _256K
    }
}
