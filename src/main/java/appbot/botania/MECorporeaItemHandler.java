package appbot.botania;

import java.util.List;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import ae2.api.config.Actionable;
import ae2.api.networking.security.IActionSource;
import ae2.api.stacks.AEItemKey;
import ae2.api.storage.MEStorage;
import org.jetbrains.annotations.NotNull;

public class MECorporeaItemHandler implements IItemHandler {

    private static final int MAX_SLOTS = 16 * 1024;

    private final MEStorage storage;
    private final IActionSource source;

    private List<ItemEntry> slots;

    public MECorporeaItemHandler(MEStorage storage) {
        this(storage, IActionSource.empty());
    }

    public MECorporeaItemHandler(MEStorage storage, IActionSource source) {
        this.storage = storage;
        this.source = source;
    }

    @Override
    public int getSlots() {
        return slots().size();
    }

    @Override
    @NotNull
    public ItemStack getStackInSlot(int slot) {
        var entry = getEntry(slot);
        return entry == null ? ItemStack.EMPTY : entry.key().toStack(entry.amount());
    }

    @Override
    @NotNull
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return stack;
    }

    @Override
    @NotNull
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount <= 0) {
            return ItemStack.EMPTY;
        }

        var entry = getEntry(slot);
        if (entry == null) {
            return ItemStack.EMPTY;
        }

        var extracted = storage.extract(entry.key(), Math.min(amount, entry.amount()),
                Actionable.ofSimulate(simulate), source);

        if (extracted > 0 && !simulate) {
            this.slots = null;
        }

        return entry.key().toStack((int) extracted);
    }

    @Override
    public int getSlotLimit(int slot) {
        var entry = getEntry(slot);
        return entry == null ? 0 : entry.amount();
    }

    private List<ItemEntry> slots() {
        var cached = this.slots;

        if (cached == null) {
            cached = buildSlots();
            this.slots = cached;
        }

        return cached;
    }

    private List<ItemEntry> buildSlots() {
        var result = new ObjectArrayList<ItemEntry>();

        for (var entry : storage.getAvailableStacks()) {
            if (!(entry.getKey() instanceof AEItemKey itemKey) || entry.getLongValue() <= 0) {
                continue;
            }

            var maxStackSize = itemKey.getMaxStackSize();
            if (maxStackSize <= 0) {
                continue;
            }

            var remaining = entry.getLongValue();

            while (remaining > 0 && result.size() < MAX_SLOTS) {
                int amount = (int) Math.min(remaining, maxStackSize);
                result.add(new ItemEntry(itemKey, amount));
                remaining -= amount;
            }

            if (result.size() >= MAX_SLOTS) {
                break;
            }
        }

        return result;
    }

    private ItemEntry getEntry(int slot) {
        var current = slots();
        return slot < 0 || slot >= current.size() ? null : current.get(slot);
    }

    private record ItemEntry(AEItemKey key, int amount) {
    }
}
