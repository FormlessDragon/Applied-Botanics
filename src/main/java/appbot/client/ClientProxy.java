package appbot.client;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appbot.common.CommonProxy;

@SideOnly(Side.CLIENT)
@SuppressWarnings("unused")
public class ClientProxy extends CommonProxy {

    @Override
    public void init(FMLInitializationEvent event) {
        AppliedBotanicsClient.initialize();
    }
}
