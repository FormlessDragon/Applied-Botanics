package appbot.item.cell;

import ae2.api.storage.MEStorageChangeListener;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import org.jetbrains.annotations.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;

import appbot.ae2.AEManaKey;
import appbot.ae2.AEManaKeyType;

import ae2.api.config.Actionable;
import ae2.api.networking.security.IActionSource;
import ae2.api.stacks.AEKey;
import ae2.api.stacks.KeyCounter;
import ae2.api.storage.cells.CellState;
import ae2.api.storage.cells.ISaveProvider;
import ae2.api.storage.cells.StorageCell;
import ae2.core.AELog;
import ae2.core.definitions.AEItems;

import java.util.Objects;

public class ManaCellInventory implements StorageCell {

    private static final String AMOUNT = "amount";

    private final IManaCellItem cellType;
    private final ItemStack i;
    @Nullable
    private final ISaveProvider container;
    private final boolean hasVoidUpgrade;
    private final ObjectList<ListenerRegistration> listeners = new ObjectArrayList<>();

    private long storedMana;
    private boolean isPersisted = true;
    private boolean dispatchingListeners;
    private boolean listUpdateRequired;
    @Nullable
    private ListenerRegistration currentListener;

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
        if (this.storedMana >= getMaxMana()) {
            return CellState.FULL;
        }
        return CellState.NOT_EMPTY;
    }

    @Override
    public double getIdleDrain() {
        return this.cellType.getIdleDrain();
    }

    @Override
    public boolean canFitInsideCell() {
        return false;
    }

    private long getMaxMana() {
        return this.cellType.getTotalBytes() * AEManaKeyType.TYPE.getAmountPerByte();
    }

    protected long getTotalBytes() {
        return this.cellType.getTotalBytes();
    }

    protected long getUsedBytes() {
        var amountPerByte = AEManaKeyType.TYPE.getAmountPerByte();
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
        if (what != AEManaKey.KEY) {
            return 0;
        }

        var inserted = Math.clamp(getMaxMana() - this.storedMana, 0, amount);

        if (mode == Actionable.MODULATE && inserted > 0) {
            this.storedMana += inserted;
            saveChanges();
            postChange(inserted);
        }

        return hasVoidUpgrade ? amount : inserted;
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (what != AEManaKey.KEY) {
            return 0;
        }

        var extracted = Math.min(this.storedMana, amount);

        if (mode == Actionable.MODULATE && extracted > 0) {
            this.storedMana -= extracted;
            saveChanges();
            postChange(-extracted);
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
            out.add(AEManaKey.KEY, this.storedMana);
        }
    }

    @Override
    public ITextComponent getDescription() {
        return this.i.getTextComponent();
    }

    @Override
    public void addListener(MEStorageChangeListener listener, Object verificationToken) {
        Objects.requireNonNull(listener, "listener");
        for (ListenerRegistration listenerRegistration : this.listeners) {
            if (listenerRegistration.listener == listener) {
                throw new IllegalStateException("The storage listener is already registered.");
            }
        }
        this.listeners.add(new ListenerRegistration(listener, verificationToken));
    }

    @Override
    public void removeListener(MEStorageChangeListener listener) {
        for (int i = this.listeners.size() - 1; i >= 0; i--) {
            var registration = this.listeners.get(i);
            if (registration.listener == listener) {
                registration.active = false;
                if (!this.dispatchingListeners) {
                    this.listeners.remove(i);
                }
            }
        }
    }

    private void postChange(long delta) {
        if (delta == 0) {
            return;
        }

        if (this.dispatchingListeners) {
            if (this.currentListener != null) {
                AELog.error("Mana cell storage listener {} modified the cell during its callback; disabling it and requesting a full storage refresh.", this.currentListener.listener);
                this.currentListener.active = false;
            } else {
                AELog.error("Mana cell storage was modified during listener dispatch without an active listener; requesting a full storage refresh.");
            }
            this.listUpdateRequired = true;
            return;
        }

        this.dispatchingListeners = true;
        try {
            for (ListenerRegistration registration : this.listeners) {
                if (!registration.active) {
                    continue;
                }
                if (!registration.listener.isValid(registration.verificationToken)) {
                    registration.active = false;
                    continue;
                }
                this.currentListener = registration;
                try {
                    registration.listener.onStackChange(AEManaKey.KEY, delta);
                } finally {
                    this.currentListener = null;
                }
                if (this.listUpdateRequired) {
                    break;
                }
            }
        } finally {
            this.currentListener = null;
            this.dispatchingListeners = false;
        }

        if (this.listUpdateRequired) {
            notifyListUpdate();
        }
        removeInactiveListeners();
    }

    private void notifyListUpdate() {
        int remainingPasses = this.listeners.size() + 1;
        this.dispatchingListeners = true;
        try {
            while (this.listUpdateRequired && remainingPasses-- > 0) {
                this.listUpdateRequired = false;
                for (ListenerRegistration registration : this.listeners) {
                    if (!registration.active) {
                        continue;
                    }
                    if (!registration.listener.isValid(registration.verificationToken)) {
                        registration.active = false;
                        continue;
                    }
                    this.currentListener = registration;
                    try {
                        registration.listener.onListUpdate();
                    } finally {
                        this.currentListener = null;
                    }
                }
            }
            if (this.listUpdateRequired) {
                AELog.error("Mana cell storage listener refresh exceeded its bounded retry count.");
            }
        } finally {
            this.listUpdateRequired = false;
            this.currentListener = null;
            this.dispatchingListeners = false;
        }
    }

    private void removeInactiveListeners() {
        for (int i = this.listeners.size() - 1; i >= 0; i--) {
            if (!this.listeners.get(i).active) {
                this.listeners.remove(i);
            }
        }
    }

    private static final class ListenerRegistration {
        private final MEStorageChangeListener listener;
        private final Object verificationToken;
        private boolean active = true;

        private ListenerRegistration(MEStorageChangeListener listener, Object verificationToken) {
            this.listener = listener;
            this.verificationToken = verificationToken;
        }
    }
}
