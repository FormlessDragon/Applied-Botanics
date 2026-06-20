package appbot.ae2;

import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;

import vazkii.botania.api.mana.IManaItem;

import ae2.api.behaviors.ContainerItemStrategy;
import ae2.api.config.Actionable;
import ae2.api.stacks.GenericStack;
import vazkii.botania.common.core.handler.ModSounds;

@SuppressWarnings("UnstableApiUsage")
public class ManaContainerItemStrategy
        implements ContainerItemStrategy<AEManaKey, ManaContainerItemStrategy.ManaItemContext> {

    @Override
    public @Nullable GenericStack getContainedStack(ItemStack stack) {
        var context = findManaItem(stack);

        if (context != null) {
            return new GenericStack(AEManaKey.KEY, context.getMana());
        } else {
            return null;
        }
    }

    @Override
    public @Nullable ManaItemContext findCarriedContext(EntityPlayer player, Container menu) {
        return findManaItem(player.inventory.getItemStack());
    }

    @Override
    public @Nullable ManaItemContext findPlayerSlotContext(EntityPlayer player, int slot) {
        return findManaItem(player.inventory.getStackInSlot(slot));
    }

    @Override
    public long extract(ManaItemContext item, AEManaKey what, long amount, Actionable mode) {
        if (!item.canExport()) {
            return 0;
        }

        var extracted = (int) Math.min(amount, item.getMana());

        if (extracted > 0 && mode == Actionable.MODULATE) {
            item.addMana(-extracted);
        }

        return extracted;
    }

    @Override
    public long insert(ManaItemContext item, AEManaKey what, long amount, Actionable mode) {
        if (!item.canReceive()) {
            return 0;
        }

        var inserted = (int) Math.min(amount, item.getMaxMana() - item.getMana());

        if (inserted > 0 && mode == Actionable.MODULATE) {
            item.addMana(inserted);
        }

        return inserted;
    }

    @Override
    public void playFillSound(EntityPlayer player, AEManaKey what) {
        player.world.playSound(null, player.posX, player.posY, player.posZ, ModSounds.manaPoolCraft,
                SoundCategory.PLAYERS, 0.5F, 1.0F);
    }

    @Override
    public void playEmptySound(EntityPlayer player, AEManaKey what) {
        player.world.playSound(null, player.posX, player.posY, player.posZ, ModSounds.blackLotus,
                SoundCategory.PLAYERS, 0.5F, 1.0F);
    }

    @Override
    public @Nullable GenericStack getExtractableContent(ManaItemContext item) {
        if (!item.canExport()) {
            return null;
        }

        return new GenericStack(AEManaKey.KEY, item.getMana());
    }

    private static @Nullable ManaItemContext findManaItem(ItemStack stack) {
        if (!stack.isEmpty() && stack.getItem() instanceof IManaItem item) {
            return new ManaItemContext(stack, item);
        }

        var portableCell = MEStorageManaItem.forItem(stack);
        if (portableCell != null) {
            return new ManaItemContext(stack, portableCell);
        }

        return null;
    }

    public record ManaItemContext(ItemStack stack, IManaItem item) {
        int getMana() {
            return item.getMana(stack);
        }

        int getMaxMana() {
            return item.getMaxMana(stack);
        }

        void addMana(int mana) {
            item.addMana(stack, mana);
        }

        boolean canReceive() {
            return item.canReceiveManaFromItem(stack, ItemStack.EMPTY);
        }

        @SuppressWarnings("BooleanMethodIsAlwaysInverted")
        boolean canExport() {
            return !item.isNoExport(stack) && item.canExportManaToItem(stack, ItemStack.EMPTY);
        }
    }
}
