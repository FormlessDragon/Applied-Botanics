package appbot.item.cell;

import ae2.api.storage.MEStorageChangeListener;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

import appbot.ae2.AEManaKey;

import ae2.api.config.Actionable;
import ae2.api.networking.security.IActionSource;
import ae2.api.stacks.AEKey;
import ae2.api.stacks.KeyCounter;
import ae2.api.storage.cells.CellState;
import ae2.api.storage.cells.StorageCell;

import java.util.Objects;

public class CreativeManaCellInventory implements StorageCell {

    private final ItemStack i;
    private final ObjectList<ListenerRegistration> listeners = new ObjectArrayList<>();

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
        removeInvalidListeners();
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
            if (this.listeners.get(i).listener == listener) {
                this.listeners.remove(i);
            }
        }
    }

    private void removeInvalidListeners() {
        for (int i = this.listeners.size() - 1; i >= 0; i--) {
            var registration = this.listeners.get(i);
            if (!registration.listener.isValid(registration.verificationToken)) {
                this.listeners.remove(i);
            }
        }
    }

    private record ListenerRegistration(MEStorageChangeListener listener, Object verificationToken) {
    }
}
