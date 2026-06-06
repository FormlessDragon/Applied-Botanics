package appbot.forge;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

import appbot.AppliedBotanics;
import appbot.ae2.ManaContainerItemStrategy;
import appbot.ae2.ManaExternalStorageStrategy;
import appbot.ae2.ManaKey;
import appbot.ae2.ManaKeyType;
import appbot.ae2.ManaStorageExportStrategy;
import appbot.ae2.ManaStorageImportStrategy;

import ae2.api.behaviors.ContainerItemStrategy;
import ae2.api.behaviors.GenericSlotCapacities;
import ae2.api.stacks.AEKeyTypes;
import ae2.parts.automation.StackWorldBehaviors;

@Mod(modid = AppliedBotanics.MOD_ID, name = "Applied Botanics", version = "@mod_version@",
    acceptedMinecraftVersions = "[1.12.2]", dependencies = "required-after:ae2;required-after:botania")
@SuppressWarnings("UnstableApiUsage")
public class AppliedBotanicsForge {

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        AEKeyTypes.register(ManaKeyType.TYPE);

        StackWorldBehaviors.registerImportStrategy(ManaKeyType.TYPE, ManaStorageImportStrategy::new);
        StackWorldBehaviors.registerExportStrategy(ManaKeyType.TYPE, ManaStorageExportStrategy::new);
        StackWorldBehaviors.registerExternalStorageStrategy(ManaKeyType.TYPE, ManaExternalStorageStrategy::new);

        ContainerItemStrategy.register(ManaKeyType.TYPE, ManaKey.class, new ManaContainerItemStrategy());
        GenericSlotCapacities.register(ManaKeyType.TYPE, 500000L);

        ABItems.init();
    }
}
