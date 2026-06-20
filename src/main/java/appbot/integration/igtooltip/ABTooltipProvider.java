package appbot.integration.igtooltip;

import appbot.block.FluixPool;
import appbot.block.TileFluixPool;

import ae2.api.integrations.igtooltip.BaseClassRegistration;
import ae2.api.integrations.igtooltip.TooltipProvider;

@SuppressWarnings("UnstableApiUsage")
public class ABTooltipProvider implements TooltipProvider {

    @Override
    public void registerBlockEntityBaseClasses(BaseClassRegistration registration) {
        registration.addBaseBlockEntity(TileFluixPool.class, FluixPool.class);
    }
}
