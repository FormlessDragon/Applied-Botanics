package appbot.block;

import java.util.EnumSet;

import com.google.common.primitives.Ints;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

import appbot.AppliedBotanics;
import appbot.ae2.AEManaKey;
import appbot.ae2.SafeMana;
import vazkii.botania.common.block.tile.mana.TilePool;

import ae2.api.config.Actionable;
import ae2.api.networking.GridFlags;
import ae2.api.networking.GridHelper;
import ae2.api.networking.IGrid;
import ae2.api.networking.IGridNode;
import ae2.api.networking.IManagedGridNode;
import ae2.api.networking.security.IActionSource;
import ae2.api.storage.StorageHelper;
import ae2.api.util.AECableType;
import ae2.hooks.ticking.TickHandler;
import ae2.me.InWorldGridNode;
import ae2.me.helpers.IGridConnectedTile;
import ae2.me.helpers.TileNodeListener;

public class TileFluixPool extends TilePool implements IGridConnectedTile, SafeMana {

    private static final String TAG_MANA = "mana";
    private static final String TAG_MANA_CAP = "manaCap";

    private final IManagedGridNode mainNode = GridHelper.createManagedNode(this, TileNodeListener.INSTANCE)
            .setFlags(GridFlags.REQUIRE_CHANNEL)
            .setVisualRepresentation(new ItemStack(AppliedBotanics.getInstance().fluixManaPool()))
            .setInWorldNode(true)
            .setExposedOnSides(EnumSet.complementOf(EnumSet.of(EnumFacing.UP)))
            .setTagName("proxy");
    private final IActionSource actionSource = IActionSource.ofMachine(mainNode::getNode);

    private boolean saving;
    private boolean markDirtyQueued;
    private int syncTicks;
    private int lastSyncedMana = Integer.MIN_VALUE;
    private int lastSyncedManaCap = Integer.MIN_VALUE;

    @Override
    public boolean isFull() {
        IGrid grid = getMainNode().getGrid();

        if (grid == null || !getMainNode().isActive()) {
            return true;
        }

        return grid.getStorageService().getInventory().insert(AEManaKey.KEY, 1, Actionable.SIMULATE, actionSource) == 0;
    }

    @Override
    public void recieveMana(int mana) {
        IGrid grid = getMainNode().getGrid();

        if (grid == null || !getMainNode().isActive()) {
            return;
        }

        var storage = grid.getStorageService().getInventory();
        boolean changed = false;

        if (mana > 0) {
            changed = StorageHelper.poweredInsert(grid.getEnergyService(), storage, AEManaKey.KEY, mana,
                    actionSource) != 0;
        } else if (mana < 0) {
            changed = StorageHelper.poweredExtraction(grid.getEnergyService(), storage, AEManaKey.KEY, -mana,
                    actionSource) != 0;
        }

        if (changed) {
            markDirty();
            markDispatchable();
        }
    }

    @Override
    public int getCurrentMana() {
        IGrid grid = getMainNode().getGrid();

        if (grid == null || saving) {
            return getLocalMana();
        }

        if (!getMainNode().isActive()) {
            return 0;
        }

        return Ints.saturatedCast(grid.getStorageService().getInventory().extract(AEManaKey.KEY, Long.MAX_VALUE,
                Actionable.SIMULATE, actionSource));
    }

    @SuppressWarnings("unused")
    public int getMaxMana() {
        IGrid grid = getMainNode().getGrid();

        if (grid == null || saving) {
            return getLocalManaCap();
        }

        if (!getMainNode().isActive()) {
            return 0;
        }

        var storage = grid.getStorageService().getInventory();
        long current = storage.extract(AEManaKey.KEY, Long.MAX_VALUE, Actionable.SIMULATE, actionSource);
        long free = storage.insert(AEManaKey.KEY, Long.MAX_VALUE, Actionable.SIMULATE, actionSource);
        return Ints.saturatedCast(saturatedAdd(current, free));
    }

    @Override
    public int getAvailableSpaceForMana() {
        IGrid grid = getMainNode().getGrid();

        if (grid == null || !getMainNode().isActive()) {
            return 0;
        }

        long free = grid.getStorageService().getInventory().insert(AEManaKey.KEY, Long.MAX_VALUE, Actionable.SIMULATE,
                actionSource);
        return Ints.saturatedCast(free);
    }

    @Override
    public void update() {
        super.update();

        if (this.world == null || this.world.isRemote || ++this.syncTicks % 10 != 0) {
            return;
        }

        int mana = getCurrentMana();
        int manaCap = getMaxMana();

        if (mana != this.lastSyncedMana || manaCap != this.lastSyncedManaCap) {
            this.lastSyncedMana = mana;
            this.lastSyncedManaCap = manaCap;
            markDispatchable();
        }
    }

    @Override
    public void writePacketNBT(NBTTagCompound tag) {
        int mana = getCurrentMana();
        int manaCap = getMaxMana();

        try {
            saving = true;
            super.writePacketNBT(tag);
        } finally {
            saving = false;
        }

        tag.setInteger(TAG_MANA, mana);
        tag.setInteger(TAG_MANA_CAP, manaCap);
        this.getMainNode().saveToNBT(tag);
    }

    @Override
    public void readPacketNBT(NBTTagCompound tag) {
        super.readPacketNBT(tag);
        this.getMainNode().loadFromNBT(tag);
    }

    @Override
    public IGridNode getGridNode(EnumFacing dir) {
        var node = this.getMainNode().getNode();
        return node instanceof InWorldGridNode inWorldNode && inWorldNode.isExposedOnSide(dir) ? node : null;
    }

    @Override
    public AECableType getCableConnectionType(EnumFacing dir) {
        return AECableType.SMART;
    }

    @Override
    public IManagedGridNode getMainNode() {
        return mainNode;
    }

    @Override
    public void saveChanges() {
        if (this.world == null) {
            return;
        }

        if (this.world.isRemote) {
            this.markDirty();
        } else {
            this.world.markChunkDirty(this.pos, this);
            if (!this.markDirtyQueued) {
                TickHandler.instance().addCallable(null, this::markDirtyAtEndOfTick);
                this.markDirtyQueued = true;
            }
        }
    }

    private void markDirtyAtEndOfTick() {
        this.markDirty();
        this.markDirtyQueued = false;
    }

    @Override
    public void onChunkUnload() {
        super.onChunkUnload();
        this.getMainNode().destroy();
    }

    public void onReady() {
        this.getMainNode().create(getWorld(), getPos());
    }

    @Override
    public void validate() {
        super.validate();
        GridHelper.onFirstTick(this, TileFluixPool::onReady);
    }

    @Override
    public void invalidate() {
        super.invalidate();
        this.getMainNode().destroy();
    }

    public int calculateComparatorLevel() {
        IGrid grid = getMainNode().getGrid();
        long currentMana;
        long freeMana;

        if (grid == null) {
            currentMana = getLocalMana();
            freeMana = getLocalManaCap() - currentMana;
        } else if (!getMainNode().isActive()) {
            currentMana = 0;
            freeMana = 0;
        } else {
            var storage = grid.getStorageService().getInventory();
            currentMana = storage.extract(AEManaKey.KEY, Long.MAX_VALUE, Actionable.SIMULATE, actionSource);
            freeMana = storage.insert(AEManaKey.KEY, Long.MAX_VALUE, Actionable.SIMULATE, actionSource);
        }

        if (currentMana == 0) {
            return 0;
        }

        return (int) Math.ceil(1 / (1 + (double) freeMana / currentMana) * 15.0);
    }

    @Override
    public int appbot$insert(int amount, Actionable mode) {
        IGrid grid = getMainNode().getGrid();

        if (grid == null || !getMainNode().isActive()) {
            return 0;
        }

        var storage = grid.getStorageService().getInventory();
        long inserted = StorageHelper.poweredInsert(grid.getEnergyService(), storage, AEManaKey.KEY, amount, actionSource,
                mode);

        if (inserted != 0 && mode == Actionable.MODULATE) {
            markDirty();
            markDispatchable();
        }

        return Ints.saturatedCast(inserted);
    }

    @Override
    public int appbot$extract(int amount, Actionable mode) {
        IGrid grid = getMainNode().getGrid();

        if (grid == null || !getMainNode().isActive()) {
            return 0;
        }

        var storage = grid.getStorageService().getInventory();
        long extracted = StorageHelper.poweredExtraction(grid.getEnergyService(), storage, AEManaKey.KEY, amount,
                actionSource, mode);

        if (extracted != 0 && mode == Actionable.MODULATE) {
            markDirty();
            markDispatchable();
        }

        return Ints.saturatedCast(extracted);
    }

    private int getLocalMana() {
        NBTTagCompound tag = new NBTTagCompound();
        super.writePacketNBT(tag);
        return tag.getInteger(TAG_MANA);
    }

    private int getLocalManaCap() {
        NBTTagCompound tag = new NBTTagCompound();
        super.writePacketNBT(tag);
        return tag.getInteger(TAG_MANA_CAP);
    }

    private static long saturatedAdd(long left, long right) {
        if (right > 0 && left > Long.MAX_VALUE - right) {
            return Long.MAX_VALUE;
        }
        return left + right;
    }
}
