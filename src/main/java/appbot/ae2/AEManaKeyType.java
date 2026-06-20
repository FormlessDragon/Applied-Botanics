package appbot.ae2;

import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

import appbot.AppliedBotanics;
import vazkii.botania.common.block.tile.mana.TilePool;

import ae2.api.stacks.AEKey;
import ae2.api.stacks.AEKeyType;

public class AEManaKeyType extends AEKeyType {

    public static final ITextComponent MANA = new TextComponentTranslation("gui." + AppliedBotanics.MOD_ID + ".mana");

    public static final AEKeyType TYPE = new AEManaKeyType();

    private AEManaKeyType() {
        super(AppliedBotanics.id("mana"), AEManaKey.class, MANA);
    }

    @Nullable
    @Override
    public AEKey readFromPacket(PacketBuffer input) {
        return AEManaKey.KEY;
    }

    @Nullable
    @Override
    public AEKey loadKeyFromTag(NBTTagCompound tag) {
        return AEManaKey.KEY;
    }

    @Override
    public int getAmountPerOperation() {
        return 500;
    }

    @Override
    public int getAmountPerByte() {
        return 500;
    }

    @Override
    public int getAmountPerUnit() {
        return TilePool.MAX_MANA;
    }

    @Override
    public @Nullable String getUnitSymbol() {
        return "pool";
    }
}
