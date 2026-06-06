package appbot.ae2;

import com.google.common.primitives.Ints;

import org.jetbrains.annotations.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import vazkii.botania.api.mana.IManaItem;

import ae2.api.config.Actionable;
import ae2.api.networking.energy.IEnergySource;
import ae2.api.networking.security.IActionSource;
import ae2.api.storage.MEStorage;
import ae2.api.storage.StorageCells;
import ae2.api.storage.StorageHelper;
import ae2.items.tools.powered.AbstractPortableCell;

public class MEStorageManaItem implements IManaItem {

    private final MEStorage storage;
    private final IEnergySource energy;
    private final IActionSource source;

    public MEStorageManaItem(MEStorage storage, IEnergySource energy, IActionSource source) {
        this.storage = storage;
        this.energy = energy;
        this.source = source;
    }

    @Nullable
    public static IManaItem forItem(ItemStack stack) {
        if (stack.getItem() instanceof AbstractPortableCell item) {
            var storage = StorageCells.getCellInventory(stack, null);

            if (storage == null) {
                return null;
            }

            return new MEStorageManaItem(storage, (amount, mode, multiplier) -> {
                amount = multiplier.multiply(amount);

                if (mode == Actionable.SIMULATE) {
                    return multiplier.divide(Math.min(amount, item.getAECurrentPower(stack)));
                }

                return multiplier.divide(item.extractAEPower(stack, amount, Actionable.MODULATE));
            }, IActionSource.empty());
        }

        // we could also add wireless terminal support, but no EntityPlayer
        return null;
    }

    @Override
    public int getMana(ItemStack stack) {
        return (int) StorageHelper.poweredExtraction(energy, storage, ManaKey.KEY, Integer.MAX_VALUE, source,
                Actionable.SIMULATE);
    }

    @Override
    public int getMaxMana(ItemStack stack) {
        return Ints.saturatedCast(StorageHelper.poweredExtraction(energy, storage, ManaKey.KEY, Integer.MAX_VALUE,
                source, Actionable.SIMULATE)
                + StorageHelper.poweredInsert(energy, storage, ManaKey.KEY, Integer.MAX_VALUE, source,
                        Actionable.SIMULATE));
    }

    @Override
    public void addMana(ItemStack stack, int mana) {
        if (mana > 0) {
            StorageHelper.poweredInsert(energy, storage, ManaKey.KEY, mana, source);
        } else {
            StorageHelper.poweredExtraction(energy, storage, ManaKey.KEY, -mana, source);
        }
    }

    @Override
    public boolean canReceiveManaFromPool(ItemStack stack, TileEntity pool) {
        return true;
    }

    @Override
    public boolean canReceiveManaFromItem(ItemStack stack, ItemStack otherStack) {
        return true;
    }

    @Override
    public boolean canExportManaToPool(ItemStack stack, TileEntity pool) {
        return true;
    }

    @Override
    public boolean canExportManaToItem(ItemStack stack, ItemStack otherStack) {
        return true;
    }

    @Override
    public boolean isNoExport(ItemStack stack) {
        return false;
    }
}
