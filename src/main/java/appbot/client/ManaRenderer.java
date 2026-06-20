package appbot.client;

import java.util.Objects;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appbot.ae2.AEManaKey;
import appbot.ae2.AEManaKeyType;

import ae2.api.client.AEKeyRenderHandler;

@SideOnly(Side.CLIENT)
public class ManaRenderer implements AEKeyRenderHandler<AEManaKey> {
    private static final ResourceLocation MANA_WATER = new ResourceLocation("botania", "blocks/mana_water");

    private TextureAtlasSprite waterSprite;

    // The constructor is called before modelManager is initialized
    private void lazyInitSprite() {
        if (this.waterSprite == null) {
            this.waterSprite = Objects.requireNonNull(Minecraft.getMinecraft().getTextureMapBlocks()
                    .getAtlasSprite(MANA_WATER.toString()));
        }
    }

    @Override
    public void drawInGui(Minecraft minecraft, int x, int y, AEManaKey stack) {
        lazyInitSprite();
        minecraft.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        GlStateManager.pushMatrix();
        try {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.disableBlend();
            GlStateManager.disableLighting();
            drawQuad(x, y, 16, 16, this.waterSprite);
        } finally {
            GlStateManager.enableBlend();
            GlStateManager.disableDepth();
            GlStateManager.disableLighting();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.popMatrix();
        }
    }

    @Override
    public void drawOnBlockFace(AEManaKey what, float scale, int combinedLight, World world) {
        lazyInitSprite();

        // In comparison to items, make it _slightly_ smaller because item icons
        // usually don't extend to the full size.
        scale -= 0.05f;

        float x0 = -scale / 2.0f;
        float y0 = -scale / 2.0f;
        float x1 = scale / 2.0f;
        float y1 = scale / 2.0f;

        GlStateManager.pushMatrix();
        try {
            GlStateManager.disableLighting();
            GlStateManager.disableCull();
            Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

            Tessellator tessellator = Tessellator.getInstance();
            var buffer = tessellator.getBuffer();
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
            buffer.pos(x0, y0, 0.0001f).tex(waterSprite.getMinU(), waterSprite.getMaxV())
                    .color(1.0f, 1.0f, 1.0f, 1.0f).endVertex();
            buffer.pos(x1, y0, 0.0001f).tex(waterSprite.getMaxU(), waterSprite.getMaxV())
                    .color(1.0f, 1.0f, 1.0f, 1.0f).endVertex();
            buffer.pos(x1, y1, 0.0001f).tex(waterSprite.getMaxU(), waterSprite.getMinV())
                    .color(1.0f, 1.0f, 1.0f, 1.0f).endVertex();
            buffer.pos(x0, y1, 0.0001f).tex(waterSprite.getMinU(), waterSprite.getMinV())
                    .color(1.0f, 1.0f, 1.0f, 1.0f).endVertex();
            tessellator.draw();
        } finally {
            GlStateManager.enableCull();
            GlStateManager.enableLighting();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.popMatrix();
        }
    }

    @Override
    public ITextComponent getDisplayName(AEManaKey stack) {
        return AEManaKeyType.MANA;
    }

    @SuppressWarnings("SameParameterValue")
    private static void drawQuad(int x, int y, int width, int height, TextureAtlasSprite sprite) {
        Tessellator tessellator = Tessellator.getInstance();
        var buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(x, y + height, 0).tex(sprite.getMinU(), sprite.getMaxV()).endVertex();
        buffer.pos(x + width, y + height, 0).tex(sprite.getMaxU(), sprite.getMaxV()).endVertex();
        buffer.pos(x + width, y, 0).tex(sprite.getMaxU(), sprite.getMinV()).endVertex();
        buffer.pos(x, y, 0).tex(sprite.getMinU(), sprite.getMinV()).endVertex();
        tessellator.draw();
    }
}
