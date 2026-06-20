package appbot.ae2;

import com.google.common.base.Predicates;
import com.google.common.primitives.Ints;

import net.minecraft.entity.Entity;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import vazkii.botania.api.mana.IManaPool;
import vazkii.botania.api.mana.IManaReceiver;
import vazkii.botania.api.mana.spark.ISparkAttachable;
import vazkii.botania.api.mana.spark.ISparkEntity;

import ae2.api.behaviors.GenericInternalInventory;
import ae2.api.config.Actionable;

@SuppressWarnings("UnstableApiUsage")
public class ManaGenericStackInvStorage implements IManaReceiver, IManaPool, ISparkAttachable, SafeMana {

    private final World level;
    private final BlockPos pos;
    private final GenericInternalInventory inv;

    public ManaGenericStackInvStorage(GenericInternalInventory inv, World level, BlockPos pos) {
        this.inv = inv;
        this.level = level;
        this.pos = pos;
    }

    @Override
    public int getCurrentMana() {
        return appbot$extract(Integer.MAX_VALUE, Actionable.SIMULATE);
    }

    @Override
    public boolean isFull() {
        return appbot$insert(1, Actionable.SIMULATE) == 0;
    }

    @Override
    public void recieveMana(int mana) {
        if (mana > 0) {
            appbot$insert(mana, Actionable.MODULATE);
        } else if (mana < 0) {
            appbot$extract(-mana, Actionable.MODULATE);
        }
    }

    @Override
    public boolean canRecieveManaFromBursts() {
        return !isFull();
    }

    @Override
    public boolean isOutputtingPower() {
        return false;
    }

    @SuppressWarnings("unused")
    public int getMaxMana() {
        var slots = 0;

        for (var i = 0; i < inv.size(); i++) {
            var key = inv.getKey(i);

            if (key == null || key == AEManaKey.KEY) {
                slots += 1;
            }
        }

        return Ints.saturatedCast(slots * inv.getMaxAmount(AEManaKey.KEY));
    }

    @Override
    public EnumDyeColor getColor() {
        return EnumDyeColor.PURPLE;
    }

    @Override
    public void setColor(EnumDyeColor color) {
    }

    @Override
    public boolean canAttachSpark(ItemStack stack) {
        return true;
    }

    @Override
    public void attachSpark(ISparkEntity entity) {
    }

    @Override
    public int getAvailableSpaceForMana() {
        return appbot$insert(Integer.MAX_VALUE, Actionable.SIMULATE);
    }

    @Override
    public ISparkEntity getAttachedSpark() {
        var sparkPos = pos.up();
        var sparks = level.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(sparkPos, sparkPos.add(1, 1, 1)),
                Predicates.instanceOf(ISparkEntity.class));

        if (sparks.size() == 1) {
            return (ISparkEntity) sparks.getFirst();
        }

        return null;
    }

    @Override
    public boolean areIncomingTranfersDone() {
        return !isFull();
    }

    @Override
    public int appbot$insert(int amount, Actionable actionable) {
        var inserted = 0L;

        for (var i = 0; i < inv.size() && inserted < amount; i++) {
            inserted += inv.insert(i, AEManaKey.KEY, amount - inserted, actionable);
        }

        return (int) inserted;
    }

    @Override
    public int appbot$extract(int amount, Actionable actionable) {
        var extracted = 0L;

        for (var i = 0; i < inv.size() && extracted < amount; i++) {
            extracted += inv.extract(i, AEManaKey.KEY, amount - extracted, actionable);
        }

        return (int) extracted;
    }
}
