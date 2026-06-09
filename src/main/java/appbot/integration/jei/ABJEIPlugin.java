package appbot.integration.jei;

import net.minecraft.item.ItemStack;

import appbot.AppliedBotanics;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import vazkii.botania.client.integration.jei.manapool.ManaPoolRecipeCategory;

@JEIPlugin
public class ABJEIPlugin implements IModPlugin {

    @Override
    public void register(IModRegistry registry) {
        registry.addRecipeCatalyst(new ItemStack(AppliedBotanics.getInstance().fluixManaPool()),
                ManaPoolRecipeCategory.UID);
    }
}
