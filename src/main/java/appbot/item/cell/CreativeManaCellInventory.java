package appbot.item.cell;

import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

import appbot.ae2.AEManaKey;

import ae2.api.config.Actionable;
import ae2.api.networking.security.IActionSource;
import ae2.api.stacks.AEKey;
import ae2.api.stacks.KeyCounter;
import ae2.api.storage.cells.CellState;
import ae2.api.storage.cells.StorageCell;

public class CreativeManaCellInventory implements StorageCell {

    private final ItemStack i;

    public CreativeManaCellInventory(ItemStack o) {
        this.i = o;
    }

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        return what instanceof AEManaKey ? amount : 0;
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        return what instanceof AEManaKey ? amount : 0;
    }

    @Override
    public void getAvailableStacks(KeyCounter out) {
        out.add(AEManaKey.KEY, 1L << 53);
    }

    @Override
    public boolean isPreferredStorageFor(AEKey what, IActionSource source) {
        return what instanceof AEManaKey;
    }

    @Override
    public CellState getStatus() {
        return CellState.NOT_EMPTY;
    }

    @Override
    public double getIdleDrain() {
        return 0;
    }

    @Override
    public boolean canFitInsideCell() {
        return false;
    }

    @Override
    public ITextComponent getDescription() {
        return this.i.getTextComponent();
    }

    @Override
    public void persist() {
    }
}
