package appbot.item.cell;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.item.ItemStack;

import ae2.api.storage.cells.ICellHandler;
import ae2.api.storage.cells.ISaveProvider;
import ae2.core.localization.Tooltips;

public class ManaCellHandler implements ICellHandler {
    public static final ManaCellHandler INSTANCE = new ManaCellHandler();

    @Override
    public boolean isCell(ItemStack is) {
        return !is.isEmpty() && is.getItem() instanceof IManaCellItem;
    }

    @Nullable
    @Override
    public ManaCellInventory getCellInventory(ItemStack is, @Nullable ISaveProvider host) {
        if (isCell(is)) {
            return new ManaCellInventory((IManaCellItem) is.getItem(), is, host);
        }
        return null;
    }

    public void addCellInformationToTooltip(ItemStack is, List<String> lines) {
        var handler = getCellInventory(is, null);
        if (handler == null) {
            return;
        }
        lines.add(Tooltips.bytesUsed(handler.getUsedBytes(), handler.getTotalBytes()).getFormattedText());
    }
}
