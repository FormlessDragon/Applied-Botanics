package appbot.client;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appbot.block.FluixPoolBlockEntity;
import vazkii.botania.client.core.handler.MiscellaneousIcons;
import vazkii.botania.client.core.helper.ShaderHelper;

@SideOnly(Side.CLIENT)
public class FluixPoolRenderer extends TileEntitySpecialRenderer<FluixPoolBlockEntity> {

    @Override
    public void render(FluixPoolBlockEntity pool, double x, double y, double z, float partialTicks,
                       int destroyStage, float alpha) {
        int mana = pool.getCurrentMana();
        int manaCap = pool.getMaxMana();

        if (mana <= 0 || manaCap <= 0) {
            return;
        }

        float waterLevel = Math.min((float) mana / manaCap, 1.0F) * 0.4F;
        if (waterLevel <= 0.0F) {
            return;
        }

        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        GlStateManager.pushMatrix();
        try {
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GlStateManager.enableRescaleNormal();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

            GlStateManager.translate(x + 0.5F, y + 1.5F, z + 0.5F);
            renderManaWater(waterLevel);
        } finally {
            GlStateManager.disableRescaleNormal();
            GlStateManager.disableBlend();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.popMatrix();
        }
    }

    private static void renderManaWater(float waterLevel) {
        float scale = 14F / 256F;
        float inset = -3.5F / 8F;

        GlStateManager.pushMatrix();
        try {
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GlStateManager.disableAlpha();
            GlStateManager.translate(inset, -1F - (0.43F - waterLevel), inset);
            GlStateManager.rotate(90F, 1F, 0F, 0F);
            GlStateManager.scale(scale, scale, scale);

            ShaderHelper.useShader(ShaderHelper.manaPool);
            renderIcon(MiscellaneousIcons.INSTANCE.manaWater);
        } finally {
            ShaderHelper.releaseShader();
            GlStateManager.enableAlpha();
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
        }
    }

    private static void renderIcon(TextureAtlasSprite sprite) {
        Tessellator tessellator = Tessellator.getInstance();
        var buffer = tessellator.getBuffer();

        buffer.begin(GL11.GL_QUADS, vazkii.botania.client.core.proxy.ClientProxy.POSITION_TEX_LMAP);
        buffer.pos(0, 16, 0).tex(sprite.getMinU(), sprite.getMaxV()).lightmap(240, 240).endVertex();
        buffer.pos(16, 16, 0).tex(sprite.getMaxU(), sprite.getMaxV()).lightmap(240, 240).endVertex();
        buffer.pos(16, 0, 0).tex(sprite.getMaxU(), sprite.getMinV()).lightmap(240, 240).endVertex();
        buffer.pos(0, 0, 0).tex(sprite.getMinU(), sprite.getMinV()).lightmap(240, 240).endVertex();
        tessellator.draw();
    }
}
