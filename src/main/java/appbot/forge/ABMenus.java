package appbot.forge;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

import ae2.api.implementations.menuobjects.IPortableTerminal;
import ae2.core.AppEng;
import ae2.menu.implementations.MenuTypeBuilder;
import ae2.menu.me.common.MEStorageMenu;

public class ABMenus {

    public static final MenuType<MEStorageMenu> PORTABLE_MANA_CELL_TYPE = MenuTypeBuilder
            .create(MEStorageMenu::new, IPortableTerminal.class).build("portable_mana_cell");

    public static void initialize(IEventBus bus) {
        bus.addListener((RegisterEvent event) -> {
            if (!event.getRegistryKey().equals(Registries.BLOCK)) {
                return;
            }

            ForgeRegistries.MENU_TYPES.register(AppEng.makeId("portable_mana_cell"), PORTABLE_MANA_CELL_TYPE);
        });
    }
}
