package appbot.ae2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Predicates;

import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.WorldServer;

import appbot.AppliedBotanics;
import vazkii.botania.api.mana.IManaPool;
import vazkii.botania.api.mana.IManaReceiver;
import vazkii.botania.api.mana.spark.ISparkAttachable;
import vazkii.botania.api.mana.spark.ISparkEntity;

import ae2.api.config.Actionable;
import ae2.api.parts.IPartItem;
import ae2.api.parts.IPartModel;
import ae2.items.parts.PartModels;
import ae2.parts.p2p.P2PModels;
import ae2.parts.p2p.P2PTunnelPart;

public class ManaP2PTunnelPart extends P2PTunnelPart<ManaP2PTunnelPart> {

    private static final P2PModels MODELS = new P2PModels(AppliedBotanics.id("part/p2p_tunnel_mana"));
    private final InputHandler inputHandler = new InputHandler();

    public ManaP2PTunnelPart(IPartItem<?> partItem) {
        super(partItem);
    }

    @PartModels
    public static List<IPartModel> getModels() {
        return MODELS.getModels();
    }

    @Override
    public IPartModel getStaticModels() {
        return MODELS.getModel(isPowered(), isActive());
    }

    public @Nullable InputHandler getBotaniaManaHandler() {
        return isOutput() ? null : inputHandler;
    }

    private @Nullable IManaReceiver getAdjacentManaReceiver() {
        var side = getSide();
        if (side == null || !(getLevel() instanceof WorldServer serverWorld)) {
            return null;
        }

        var pos = getTileEntity().getPos().offset(side);
        return AppliedBotanics.getInstance().manaReceiver(serverWorld, pos).find(side.getOpposite());
    }

    private List<IManaReceiver> getOutputReceivers() {
        var receivers = new ArrayList<IManaReceiver>();
        for (var output : getOutputs()) {
            var receiver = output.getAdjacentManaReceiver();
            if (receiver != null) {
                receivers.add(receiver);
            }
        }
        return receivers;
    }

    public class InputHandler implements IManaPool, ISparkAttachable, SafeMana {

        @Override
        public int getCurrentMana() {
            return 0;
        }

        @Override
        public boolean isFull() {
            for (var receiver : getOutputReceivers()) {
                if (!receiver.isFull()) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public void recieveMana(int mana) {
            if (mana > 0) {
                insert(mana, Actionable.MODULATE);
            }
        }

        @Override
        public boolean canRecieveManaFromBursts() {
            for (var receiver : getOutputReceivers()) {
                if (receiver.canRecieveManaFromBursts() && !receiver.isFull()) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean isOutputtingPower() {
            return false;
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
            var space = 0;
            for (var receiver : getOutputReceivers()) {
                space += ManaHelper.getCapacity(receiver) - receiver.getCurrentMana();
            }
            return Math.max(0, space);
        }

        @Override
        public ISparkEntity getAttachedSpark() {
            var sparkPos = getTileEntity().getPos().up();
            var sparks = getLevel().getEntitiesWithinAABB(Entity.class,
                    new AxisAlignedBB(sparkPos, sparkPos.add(1, 1, 1)),
                    Predicates.instanceOf(ISparkEntity.class));
            return sparks.size() == 1 ? (ISparkEntity) sparks.getFirst() : null;
        }

        @Override
        public boolean areIncomingTranfersDone() {
            return isFull();
        }

        @Override
        public int insert(int amount, Actionable mode) {
            if (amount <= 0) {
                return 0;
            }

            var receivers = new ArrayList<>(getOutputReceivers().stream()
                    .filter(receiver -> receiver.canRecieveManaFromBursts() && !receiver.isFull())
                    .toList());
            if (receivers.isEmpty()) {
                return 0;
            }

            var remaining = amount;
            Collections.shuffle(receivers);
            for (var receiver : receivers) {
                if (remaining <= 0) {
                    break;
                }

                var safeMana = SafeMana.conv(receiver);
                if (safeMana == null) {
                    continue;
                }
                remaining -= safeMana.insert(remaining, mode);
            }

            var inserted = amount - remaining;
            if (mode == Actionable.MODULATE && inserted > 0) {
                deductTransportCost(inserted / 100L, ManaKeyType.TYPE);
            }
            return inserted;
        }

        @Override
        public int extract(int amount, Actionable mode) {
            return 0;
        }
    }
}
