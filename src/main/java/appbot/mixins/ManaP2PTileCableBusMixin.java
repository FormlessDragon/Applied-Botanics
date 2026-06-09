package appbot.mixins;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

import appbot.forge.ae2.ManaP2PTunnelPart;
import vazkii.botania.api.mana.IManaPool;
import vazkii.botania.api.mana.spark.ISparkAttachable;
import vazkii.botania.api.mana.spark.ISparkEntity;

import ae2.api.parts.IPart;
import ae2.api.parts.IPartHost;
import ae2.tile.networking.TileCableBus;

@Mixin(value = TileCableBus.class, remap = false)
public abstract class ManaP2PTileCableBusMixin implements IManaPool, ISparkAttachable {

    private ManaP2PTunnelPart.InputHandler appbot$getManaP2PHandler() {
        ManaP2PTunnelPart.InputHandler handler = null;
        for (var side : EnumFacing.VALUES) {
            IPart part = ((IPartHost) this).getPart(side);
            if (part instanceof ManaP2PTunnelPart manaP2P) {
                var candidate = manaP2P.getBotaniaManaHandler();
                if (candidate != null) {
                    if (handler != null) {
                        return null;
                    }
                    handler = candidate;
                }
            }
        }
        return handler;
    }

    @Override
    public int getCurrentMana() {
        var handler = appbot$getManaP2PHandler();
        return handler == null ? 0 : handler.getCurrentMana();
    }

    @Override
    public boolean isFull() {
        var handler = appbot$getManaP2PHandler();
        return handler == null || handler.isFull();
    }

    @Override
    public void recieveMana(int mana) {
        var handler = appbot$getManaP2PHandler();
        if (handler != null) {
            handler.recieveMana(mana);
        }
    }

    @Override
    public boolean canRecieveManaFromBursts() {
        var handler = appbot$getManaP2PHandler();
        return handler != null && handler.canRecieveManaFromBursts();
    }

    @Override
    public boolean isOutputtingPower() {
        var handler = appbot$getManaP2PHandler();
        return handler != null && handler.isOutputtingPower();
    }

    @Override
    public EnumDyeColor getColor() {
        var handler = appbot$getManaP2PHandler();
        return handler == null ? EnumDyeColor.PURPLE : handler.getColor();
    }

    @Override
    public void setColor(EnumDyeColor color) {
        var handler = appbot$getManaP2PHandler();
        if (handler != null) {
            handler.setColor(color);
        }
    }

    @Override
    public boolean canAttachSpark(ItemStack stack) {
        var handler = appbot$getManaP2PHandler();
        return handler != null && handler.canAttachSpark(stack);
    }

    @Override
    public void attachSpark(ISparkEntity entity) {
        var handler = appbot$getManaP2PHandler();
        if (handler != null) {
            handler.attachSpark(entity);
        }
    }

    @Override
    public int getAvailableSpaceForMana() {
        var handler = appbot$getManaP2PHandler();
        return handler == null ? 0 : handler.getAvailableSpaceForMana();
    }

    @Override
    public ISparkEntity getAttachedSpark() {
        var handler = appbot$getManaP2PHandler();
        return handler == null ? null : handler.getAttachedSpark();
    }

    @Override
    public boolean areIncomingTranfersDone() {
        var handler = appbot$getManaP2PHandler();
        return handler == null || handler.areIncomingTranfersDone();
    }
}
