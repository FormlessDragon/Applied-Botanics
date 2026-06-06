package appbot.item.cell;

import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.item.ItemStack;

import appbot.ae2.ManaKey;
import appbot.ae2.ManaKeyType;

import ae2.api.config.Actionable;
import ae2.api.networking.security.IActionSource;
import ae2.api.stacks.AEKey;
import ae2.api.stacks.KeyCounter;
import ae2.api.storage.cells.CellState;
import ae2.api.storage.cells.ISaveProvider;
import ae2.api.storage.cells.StorageCell;
import ae2.core.definitions.AEItems;

public class ManaCellInventory implements StorageCell {

    private static final String AMOUNT = "amount";

    private final IManaCellItem cellType;
    private final ItemStack i;
    @Nullable
    private final ISaveProvider container;
    private final boolean hasVoidUpgrade;

    private long storedMana;
    private boolean isPersisted = true;

    public ManaCellInventory(IManaCellItem cellType, ItemStack o, @Nullable ISaveProvider container) {
        this.cellType = cellType;
        this.i = o;
        this.container = container;
        this.storedMana = getTag().getLong(AMOUNT);
        this.hasVoidUpgrade = cellType.getUpgrades(o).isInstalled(AEItems.VOID_CARD.item());
    }

    private NBTTagCompound getTag() {
        NBTTagCompound tag = this.i.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            this.i.setTagCompound(tag);
        }
        return tag;
    }

    @Override
    public CellState getStatus() {
        if (this.storedMana == 0) {
            return CellState.EMPTY;
        }
        if (this.storedMana == getMaxMana()) {
            return CellState.FULL;
        }
        if (this.storedMana > getMaxMana() / 2) {
            return CellState.TYPES_FULL;
        }
        return CellState.NOT_EMPTY;
    }

    @Override
    public double getIdleDrain() {
        return this.cellType.getIdleDrain();
    }

    private long getMaxMana() {
        return this.cellType.getTotalBytes() * ManaKeyType.TYPE.getAmountPerByte();
    }

    protected long getTotalBytes() {
        return this.cellType.getTotalBytes();
    }

    protected long getUsedBytes() {
        var amountPerByte = ManaKeyType.TYPE.getAmountPerByte();
        return (this.storedMana + amountPerByte - 1) / amountPerByte;
    }

    protected void saveChanges() {
        this.isPersisted = false;
        if (this.container != null) {
            this.container.saveChanges();
        } else {
            // if there is no ISaveProvider, store to NBT immediately
            this.persist();
        }
    }

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (!(what instanceof ManaKey)) {
            return 0;
        }

        var inserted = Math.min(getMaxMana() - this.storedMana, amount);

        if (mode == Actionable.MODULATE) {
            this.storedMana += inserted;
            saveChanges();
        }

        return hasVoidUpgrade ? amount : inserted;
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (!(what instanceof ManaKey)) {
            return 0;
        }

        var extracted = Math.min(this.storedMana, amount);

        if (mode == Actionable.MODULATE) {
            this.storedMana -= extracted;
            saveChanges();
        }

        return extracted;
    }

    @Override
    public void persist() {
        if (this.isPersisted) {
            return;
        }

        if (this.storedMana <= 0) {
            this.getTag().removeTag(AMOUNT);
        } else {
            this.getTag().setLong(AMOUNT, this.storedMana);
        }

        this.isPersisted = true;
    }

    @Override
    public void getAvailableStacks(KeyCounter out) {
        if (this.storedMana > 0) {
            out.add(ManaKey.KEY, this.storedMana);
        }
    }

    @Override
    public ITextComponent getDescription() {
        return this.i.getTextComponent();
    }
}
