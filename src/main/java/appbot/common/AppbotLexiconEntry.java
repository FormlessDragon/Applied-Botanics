package appbot.common;

import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.lexicon.LexiconCategory;
import vazkii.botania.api.lexicon.LexiconEntry;
import vazkii.botania.api.lexicon.LexiconPage;

final class AppbotLexiconEntry extends LexiconEntry {

    AppbotLexiconEntry(String name, LexiconCategory category) {
        super(name, category);
        BotaniaAPI.addEntry(this, category);
    }

    @Override
    public LexiconEntry setLexiconPages(LexiconPage... pages) {
        for (var page : pages) {
            if (!page.unlocalizedName.isEmpty()) {
                page.unlocalizedName = ABLexicon.PAGE_PREFIX + getLazyUnlocalizedName() + page.unlocalizedName;
            }
        }

        return super.setLexiconPages(pages);
    }

    @Override
    public String getUnlocalizedName() {
        return ABLexicon.ENTRY_PREFIX + super.getUnlocalizedName();
    }

    String getLazyUnlocalizedName() {
        return super.getUnlocalizedName();
    }
}
