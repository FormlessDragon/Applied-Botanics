package appbot.botania;

import com.google.common.primitives.Ints;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import ae2.api.config.Actionable;
import ae2.api.networking.security.IActionSource;
import ae2.api.stacks.AEItemKey;
import ae2.api.storage.MEStorage;

public class MECorporeaItemHandler implements IItemHandler {

    private final MEStorage storage;
    private final IActionSource source;

    public MECorporeaItemHandler(MEStorage storage) {
        this(storage, IActionSource.empty());
    }

    public MECorporeaItemHandler(MEStorage storage, IActionSource source) {
        this.storage = storage;
        this.source = source;
    }

    @Override
    public int getSlots() {
        long slots = 0;
        for (var entry : storage.getAvailableStacks()) {
            if (entry.getKey() instanceof AEItemKey itemKey && entry.getLongValue() > 0) {
                slots = saturatedAdd(slots, getVirtualSlotCount(entry.getLongValue(), itemKey.getMaxStackSize()));
            }
        }
        return Ints.saturatedCast(slots);
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        var entry = getItemKey(slot);
        if (entry == null) {
            return ItemStack.EMPTY;
        }

        return entry.key.toStack(getVisibleAmount(entry));
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return stack;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount <= 0) {
            return ItemStack.EMPTY;
        }

        var entry = getItemKey(slot);
        if (entry == null) {
            return ItemStack.EMPTY;
        }

        var extracted = storage.extract(entry.key, Math.min(amount, entry.amount), Actionable.ofSimulate(simulate),
                source);
        return entry.key.toStack(Ints.saturatedCast(extracted));
    }

    @Override
    public int getSlotLimit(int slot) {
        var entry = getItemKey(slot);
        return entry == null ? 0 : getVisibleAmount(entry);
    }

    private static int getVisibleAmount(ItemEntry entry) {
        return Ints.saturatedCast(Math.min(entry.amount, entry.key.getMaxStackSize()));
    }

    private ItemEntry getItemKey(int slot) {
        if (slot < 0) {
            return null;
        }

        long slotOffset = slot;
        for (var entry : storage.getAvailableStacks()) {
            if (entry.getKey() instanceof AEItemKey itemKey && entry.getLongValue() > 0) {
                var maxStackSize = itemKey.getMaxStackSize();
                var virtualSlots = getVirtualSlotCount(entry.getLongValue(), maxStackSize);
                if (slotOffset < virtualSlots) {
                    var consumed = slotOffset * maxStackSize;
                    return new ItemEntry(itemKey, Math.min(entry.getLongValue() - consumed, maxStackSize));
                }
                slotOffset -= virtualSlots;
            }
        }
        return null;
    }

    private static long getVirtualSlotCount(long amount, int maxStackSize) {
        var fullSlots = amount / maxStackSize;
        return amount % maxStackSize == 0 ? fullSlots : fullSlots + 1;
    }

    private static long saturatedAdd(long a, long b) {
        var result = a + b;
        return result < 0 ? Long.MAX_VALUE : result;
    }

    private record ItemEntry(AEItemKey key, long amount) {
    }
}
