package appbot.common;

import net.minecraft.item.ItemStack;

import appbot.AppliedBotanics;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.lexicon.LexiconEntry;
import vazkii.botania.common.lexicon.page.PageCraftingRecipe;
import vazkii.botania.common.lexicon.page.PageText;

public final class ABLexicon {

    static final String ENTRY_PREFIX = "appbot.entry.";
    static final String PAGE_PREFIX = "appbot.page.";

    public static LexiconEntry FLUIX_MANA_POOL;
    public static LexiconEntry MANA_DRIVES;

    private ABLexicon() {
    }

    public static void init() {
        var pool = new AppbotLexiconEntry("fluix_mana_pool", BotaniaAPI.categoryMisc);
        pool.setIcon(new ItemStack(ABBlocks.FLUIX_MANA_POOL));
        pool.setKnowledgeType(BotaniaAPI.elvenKnowledge);
        pool.setLexiconPages(new PageText("0"),
                new PageCraftingRecipe("1", AppliedBotanics.id("fluix_mana_pool")));
        FLUIX_MANA_POOL = pool;

        var drives = new AppbotLexiconEntry("mana_drives", BotaniaAPI.categoryMisc);
        drives.setIcon(new ItemStack(ABItems.MANA_CELL_HOUSING));
        drives.setKnowledgeType(BotaniaAPI.elvenKnowledge);
        drives.setLexiconPages(new PageText("0"),
                new PageCraftingRecipe("1", AppliedBotanics.id("mana_cell_housing")),
                new PageCraftingRecipe("", AppliedBotanics.id("mana_cell_1k")),
                new PageCraftingRecipe("", AppliedBotanics.id("mana_cell_4k")),
                new PageCraftingRecipe("", AppliedBotanics.id("mana_cell_16k")),
                new PageCraftingRecipe("", AppliedBotanics.id("mana_cell_64k")),
                new PageCraftingRecipe("", AppliedBotanics.id("mana_cell_256k")));
        MANA_DRIVES = drives;
    }
}
