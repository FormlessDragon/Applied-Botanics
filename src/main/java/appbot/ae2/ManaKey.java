package appbot.ae2;

import java.text.NumberFormat;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

import ae2.api.stacks.AEKey;
import ae2.api.stacks.AEKeyType;
import ae2.api.stacks.AmountFormat;

public class ManaKey extends AEKey {

    public static final AEKey KEY = new ManaKey();

    private static final ResourceLocation ID = new ResourceLocation("botania", "mana");

    private ManaKey() {
    }

    @Override
    public AEKeyType getType() {
        return ManaKeyType.TYPE;
    }

    @Override
    public AEKey dropSecondary() {
        return this;
    }

    @Override
    public NBTTagCompound toTag() {
        return new NBTTagCompound();
    }

    @Override
    public Object getPrimaryKey() {
        return this;
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void writeToPacket(PacketBuffer data) {
    }

    @Override
    public void addDrops(long amount, List<ItemStack> drops, World level, BlockPos pos) {
    }

    @Override
    protected ITextComponent computeDisplayName() {
        return ManaKeyType.MANA;
    }

    @Override
    public String formatAmount(long amount, AmountFormat format) {
        if (format == AmountFormat.FULL) {
            var units = amount / (double) getType().getAmountPerUnit();
            return NumberFormat.getNumberInstance().format(units) + (units == 1 ? " pool" : " pools");
        }

        return super.formatAmount(amount, format);
    }

    @Override
    public Object getReadOnlyStack() {
        return null;
    }

    @Override
    public boolean isTagged(String tag) {
        return false;
    }

    @Override
    public NBTBase get(String componentId) {
        return null;
    }

    @Override
    public boolean hasComponents() {
        return false;
    }
}
