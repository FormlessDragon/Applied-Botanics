package appbot;

import appbot.common.ABItems;
import appbot.common.CommonProxy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import appbot.ae2.ManaContainerItemStrategy;
import appbot.ae2.ManaExternalStorageStrategy;
import appbot.ae2.AEManaKey;
import appbot.ae2.AEManaKeyType;
import appbot.ae2.ManaStorageExportStrategy;
import appbot.ae2.ManaStorageImportStrategy;

import ae2.api.behaviors.ContainerItemStrategy;
import ae2.api.behaviors.GenericSlotCapacities;
import ae2.api.stacks.AEKeyTypes;
import ae2.parts.automation.StackWorldBehaviors;

@Mod(modid = AppliedBotanics.MOD_ID, name = AppliedBotanics.MOD_NAME, version = AppliedBotanics.VERSION, acceptedMinecraftVersions = "[1.12.2]", dependencies = "required-after:ae2;required-after:botania")
@SuppressWarnings({"UnstableApiUsage", "unused"})
public class Appbot {

    @SidedProxy(clientSide = "appbot.client.ClientProxy", serverSide = "appbot.common.CommonProxy")
    private static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        ABItems.preInit();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        AEKeyTypes.register(AEManaKeyType.TYPE);

        StackWorldBehaviors.registerImportStrategy(AEManaKeyType.TYPE, ManaStorageImportStrategy::new);
        StackWorldBehaviors.registerExportStrategy(AEManaKeyType.TYPE, ManaStorageExportStrategy::new);
        StackWorldBehaviors.registerExternalStorageStrategy(AEManaKeyType.TYPE, ManaExternalStorageStrategy::new);

        ContainerItemStrategy.register(AEManaKeyType.TYPE, AEManaKey.class, new ManaContainerItemStrategy());
        GenericSlotCapacities.register(AEManaKeyType.TYPE, 500000L);

        ABItems.init();
        proxy.init(event);
    }
}
