package appbot.item.cell;

import net.minecraft.item.ItemStack;

import ae2.api.config.FuzzyMode;
import ae2.api.storage.cells.ICellWorkbenchItem;

public interface IManaCellItem extends ICellWorkbenchItem {

    long getTotalBytes();

    double getIdleDrain();

    @Override
    default FuzzyMode getFuzzyMode(ItemStack is) {
        return FuzzyMode.IGNORE_ALL;
    }

    @Override
    default void setFuzzyMode(ItemStack is, FuzzyMode fzMode) {
    }
}
