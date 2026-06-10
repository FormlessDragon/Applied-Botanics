package appbot.item;

import java.util.List;
import java.util.Objects;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import appbot.AppliedBotanics;
import appbot.item.cell.IManaCellItem;
import appbot.item.cell.ManaCellHandler;

import ae2.api.upgrades.Upgrades;
import ae2.items.tools.powered.AbstractPortableCell;

public class PortableManaCellItem extends AbstractPortableCell implements IManaCellItem {

    private final int totalBytes;
    private final double idleDrain;

    public PortableManaCellItem(int kilobytes, double idleDrain) {
        super(AppliedBotanics.getInstance().portableCellMenu(), 20000, 0x67b9ee);
        this.totalBytes = kilobytes * 1000;
        this.idleDrain = idleDrain;
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
    public ResourceLocation getRecipeId() {
        return AppliedBotanics.id(Objects.requireNonNull(getRegistryName()).getPath());
    }

    @Override
    protected void addCheckedInformation(ItemStack stack, World world, List<String> lines,
            ITooltipFlag advancedTooltips) {
        super.addCheckedInformation(stack, world, lines, advancedTooltips);
        ManaCellHandler.INSTANCE.addCellInformationToTooltip(stack, lines);
    }

    @Override
    public double getChargeRate(ItemStack stack) {
        return 80d + 80d * Upgrades.getEnergyCardMultiplier(getUpgrades(stack));
    }
}
