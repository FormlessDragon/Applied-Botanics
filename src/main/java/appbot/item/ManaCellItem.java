package appbot.item;

import java.util.List;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import appbot.AppliedBotanics;
import appbot.item.cell.IManaCellItem;
import appbot.item.cell.ManaCellHandler;

import ae2.api.storage.StorageCells;
import ae2.api.upgrades.IUpgradeInventory;
import ae2.api.upgrades.UpgradeInventories;
import ae2.core.localization.PlayerMessages;
import ae2.util.InteractionUtil;
import org.jetbrains.annotations.Nullable;

public class ManaCellItem extends Item implements IManaCellItem {

    private final Item coreItem;
    private final int totalBytes;
    private final double idleDrain;

    public ManaCellItem(Item coreItem, int kilobytes, double idleDrain) {
        this.coreItem = coreItem;
        this.totalBytes = kilobytes * 1000;
        this.idleDrain = idleDrain;
        setMaxStackSize(1);
    }

    @Override
    public IUpgradeInventory getUpgrades(ItemStack is) {
        return UpgradeInventories.forItem(is, 1);
    }

    @Override
    public long getTotalBytes() {
        return this.totalBytes;
    }

    @Override
    public double getIdleDrain() {
        return this.idleDrain;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        this.disassembleDrive(player.getHeldItem(hand), world, player);
        return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }

    private boolean disassembleDrive(ItemStack stack, World world, EntityPlayer player) {
        if (!InteractionUtil.isInAlternateUseMode(player)) {
            return false;
        }

        if (player.inventory.getCurrentItem() != stack || stack.getCount() != 1) {
            return false;
        }

        if (world.isRemote) {
            return true;
        }

        var inv = StorageCells.getCellInventory(stack, null);
        if (inv != null && !inv.getAvailableStacks().isEmpty()) {
            player.sendStatusMessage(PlayerMessages.OnlyEmptyCellsCanBeDisassembled.text(), true);
            return true;
        }

        player.inventory.setInventorySlotContents(player.inventory.currentItem, ItemStack.EMPTY);
        player.inventory.placeItemBackInInventory(world, new ItemStack(coreItem));

        for (var upgrade : this.getUpgrades(stack)) {
            player.inventory.placeItemBackInInventory(world, upgrade);
        }

        player.inventory.placeItemBackInInventory(world,
                new ItemStack(AppliedBotanics.getInstance().manaCellHousing()));
        return true;
    }

    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, net.minecraft.util.math.BlockPos pos,
            EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
        return this.disassembleDrive(player.getHeldItem(hand), world, player)
                ? EnumActionResult.SUCCESS
                : EnumActionResult.PASS;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void addInformation(ItemStack stack, @Nullable World world, List lines, ITooltipFlag advancedTooltips) {
        ManaCellHandler.INSTANCE.addCellInformationToTooltip(stack, lines);
    }
}
