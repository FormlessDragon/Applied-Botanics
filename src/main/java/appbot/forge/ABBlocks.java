package appbot.forge;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import appbot.AppliedBotanics;
import appbot.block.FluixPool;
import appbot.block.FluixPoolBlockEntity;

@Mod.EventBusSubscriber(modid = AppliedBotanics.MOD_ID)
public final class ABBlocks {

    public static final FluixPool FLUIX_MANA_POOL = new FluixPool();

    private ABBlocks() {
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(FLUIX_MANA_POOL);
        GameRegistry.registerTileEntity(FluixPoolBlockEntity.class, AppliedBotanics.id("fluix_mana_pool"));
    }

    @SubscribeEvent
    public static void registerItemBlocks(RegistryEvent.Register<net.minecraft.item.Item> event) {
        event.getRegistry().register(new ItemBlock(FLUIX_MANA_POOL).setRegistryName(FLUIX_MANA_POOL.getRegistryName()));
    }
}
