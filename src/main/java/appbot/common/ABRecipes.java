package appbot.common;

import appbot.AppliedBotanics;
import ae2.core.definitions.AEBlocks;
import ae2.core.definitions.AEItems;
import ae2.recipes.game.StorageCellDisassemblyRecipe;
import ae2.recipes.game.StorageCellUpgradeRecipe;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.Objects;

@Mod.EventBusSubscriber(modid = AppliedBotanics.MOD_ID)
public final class ABRecipes {

    private static final String[] TIER_NAMES = {"1k", "4k", "16k", "64k", "256k"};

    private ABRecipes() {
    }

    @SubscribeEvent
    public static void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        var registry = event.getRegistry();
        registerStorageCellUpgradeRecipes(registry, "mana_cell", manaCells());
        registerStorageCellUpgradeRecipes(registry, "portable_mana_cell", portableManaCells());
    }

    public static void initStorageCellDisassembly() {
        for (var tier : ABItems.Tier.values()) {
            var component = component(tier);
            registerStorageCellDisassembly(ABItems.get(tier), component, ABItems.MANA_CELL_HOUSING);
            registerPortableCellDisassembly(ABItems.getPortable(tier), component, ABItems.MANA_CELL_HOUSING);
        }
    }

    @SuppressWarnings("SameParameterValue")
    private static void registerStorageCellDisassembly(Item cell, Item component, Item housing) {
        registerDisassembly(cell, new ItemStack(housing), new ItemStack(component));
    }

    @SuppressWarnings("SameParameterValue")
    private static void registerPortableCellDisassembly(Item cell, Item component, Item housing) {
        registerDisassembly(
                cell,
                new ItemStack(Objects.requireNonNull(AEBlocks.ME_CHEST.item())),
                new ItemStack(Objects.requireNonNull(AEBlocks.ENERGY_CELL.item())),
                new ItemStack(housing),
                new ItemStack(component));
    }

    private static void registerDisassembly(Item cell, ItemStack... results) {
        var stacks = new ObjectArrayList<ItemStack>(results.length);
        for (var result : results) {
            stacks.add(result.copy());
        }
        StorageCellDisassemblyRecipe.register(new StorageCellDisassemblyRecipe(cell, stacks));
    }

    private static void registerStorageCellUpgradeRecipes(IForgeRegistry<IRecipe> registry, String prefix, Item[] cells) {
        var components = components();
        for (int from = 0; from < cells.length; from++) {
            for (int to = from + 1; to < cells.length; to++) {
                registerRecipe(registry, "upgrade/" + prefix + "_" + TIER_NAMES[from] + "_to_" + TIER_NAMES[to],
                        new StorageCellUpgradeRecipe(cells[from], components[to], cells[to], components[from]));
            }
        }
    }

    private static Item[] manaCells() {
        var tiers = ABItems.Tier.values();
        var cells = new Item[tiers.length];
        for (int i = 0; i < tiers.length; i++) {
            cells[i] = ABItems.get(tiers[i]);
        }
        return cells;
    }

    private static Item[] portableManaCells() {
        var tiers = ABItems.Tier.values();
        var cells = new Item[tiers.length];
        for (int i = 0; i < tiers.length; i++) {
            cells[i] = ABItems.getPortable(tiers[i]);
        }
        return cells;
    }

    private static Item[] components() {
        var tiers = ABItems.Tier.values();
        var components = new Item[tiers.length];
        for (int i = 0; i < tiers.length; i++) {
            components[i] = component(tiers[i]);
        }
        return components;
    }

    private static Item component(ABItems.Tier tier) {
        return switch (tier) {
            case _1K -> Objects.requireNonNull(AEItems.CELL_COMPONENT_1K.item());
            case _4K -> Objects.requireNonNull(AEItems.CELL_COMPONENT_4K.item());
            case _16K -> Objects.requireNonNull(AEItems.CELL_COMPONENT_16K.item());
            case _64K -> Objects.requireNonNull(AEItems.CELL_COMPONENT_64K.item());
            case _256K -> Objects.requireNonNull(AEItems.CELL_COMPONENT_256K.item());
        };
    }

    private static void registerRecipe(IForgeRegistry<IRecipe> registry, String id, IRecipe recipe) {
        registry.register(recipe.setRegistryName(new ResourceLocation(AppliedBotanics.MOD_ID, id)));
    }
}
