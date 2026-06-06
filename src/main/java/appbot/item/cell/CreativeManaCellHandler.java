package appbot.item.cell;

import org.jetbrains.annotations.Nullable;

import net.minecraft.item.ItemStack;

import appbot.item.CreativeManaCellItem;

import ae2.api.storage.cells.ICellHandler;
import ae2.api.storage.cells.ISaveProvider;

public class CreativeManaCellHandler implements ICellHandler {
    @Override
    public boolean isCell(ItemStack is) {
        return !is.isEmpty() && is.getItem() instanceof CreativeManaCellItem;
    }

    @Nullable
    @Override
    public CreativeManaCellInventory getCellInventory(ItemStack is, @Nullable ISaveProvider host) {
        if (isCell(is)) {
            return new CreativeManaCellInventory(is);
        }
        return null;
    }
}
