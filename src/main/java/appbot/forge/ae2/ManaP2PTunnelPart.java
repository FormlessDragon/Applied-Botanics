package appbot.forge.ae2;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.common.base.Predicates;

import org.jetbrains.annotations.Nullable;

import net.minecraft.util.math.BlockPos;
import net.minecraft.entity.Entity;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.util.math.AxisAlignedBB;

import appbot.AppliedBotanics;
import appbot.ae2.ManaHelper;
import appbot.ae2.ManaKeyType;
import appbot.ae2.SafeMana;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.mana.IManaPool;
import vazkii.botania.api.mana.IManaReceiver;
import vazkii.botania.api.mana.spark.ISparkEntity;
import vazkii.botania.api.mana.spark.ISparkAttachable;

import ae2.api.config.Actionable;
import ae2.api.parts.IPartItem;
import ae2.api.parts.IPartModel;
import ae2.items.parts.PartModels;
import ae2.parts.p2p.CapabilityP2PTunnelPart;
import ae2.parts.p2p.P2PModels;

public class ManaP2PTunnelPart extends CapabilityP2PTunnelPart<ManaP2PTunnelPart, IManaReceiver> {

    private static final P2PModels MODELS = new P2PModels(AppliedBotanics.id("part/mana_p2p_tunnel"));
    private final ISparkAttachable ISparkAttachable = new P2PSparkAttachable();

    public ManaP2PTunnelPart(IPartItem<?> partItem) {
        super(partItem, BotaniaForgeCapabilities.MANA_RECEIVER);
        inputHandler = new InputHandler();
        outputHandler = emptyHandler = new EmptyHandler();
    }

    @PartModels
    public static List<IPartModel> getModels() {
        return MODELS.getModels();
    }

    @Override
    public IPartModel getStaticModels() {
        return MODELS.getModel(this.isPowered(), this.isActive());
    }

    @Nullable
    public ISparkAttachable getSparkAttachable() {
        return isOutput() ? null : ISparkAttachable;
    }

    private class P2PSparkAttachable implements ISparkAttachable {

        @Override
        public boolean canAttachSpark(ItemStack stack) {
            return true;
        }

        @Override
        public int getAvailableSpaceForMana() {
            var space = 0;

            for (var output : getOutputs()) {
                try (var guard = output.getAdjacentCapability()) {
                    var receiver = guard.get();
                    space += ManaHelper.getCapacity(receiver);
                }
            }

            return space;
        }

        @Override
        public ISparkEntity getAttachedSpark() {
            var sparkPos = getHost().getLocation().getPos().above();
            var sparks = getLevel().getEntitiesOfClass(Entity.class, new AxisAlignedBB(sparkPos, sparkPos.offset(1, 1, 1)),
                    Predicates.instanceOf(ISparkEntity.class));

            if (sparks.size() == 1) {
                return (ISparkEntity) sparks.get(0);
            }

            return null;
        }

        @Override
        public boolean areIncomingTranfersDone() {
            for (var output : getOutputs()) {
                try (var guard = output.getAdjacentCapability()) {
                    var receiver = guard.get();

                    if (receiver.canReceiveManaFromBursts() && !receiver.isFull()) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    private class InputHandler implements IManaReceiver, IManaPool, SafeMana {

        @Override
        public World getManaReceiverLevel() {
            return getLevel();
        }

        @Override
        public BlockPos getManaReceiverPos() {
            return getHost().getLocation().getPos();
        }

        @Override
        public int getCurrentMana() {
            return 0;
        }

        @Override
        public boolean isFull() {
            for (var output : getOutputs()) {
                try (var guard = output.getAdjacentCapability()) {
                    if (!guard.get().isFull()) {
                        return false;
                    }
                }
            }

            return true;
        }

        @Override
        public void receiveMana(int mana) {
            if (mana <= 0) {
                // if mana < 0, they're getting free mana...
                return;
            }

            var outputs = getOutputStream()
                    .filter(part -> {
                        try (var guard = part.getAdjacentCapability()) {
                            var receiver = guard.get();
                            return receiver.canReceiveManaFromBursts() && !receiver.isFull();
                        }
                    })
                    .collect(Collectors.toList());

            if (outputs.isEmpty()) {
                return;
            }

            Collections.shuffle(outputs);

            deductTransportCost(mana / 100, ManaKeyType.TYPE);
            var manaForEach = mana / outputs.size();
            var spill = mana % outputs.size();

            for (var output : outputs) {
                try (var guard = output.getAdjacentCapability()) {
                    guard.get().receiveMana(manaForEach + (spill-- > 0 ? 1 : 0));
                }
            }
        }

        @Override
        public boolean canReceiveManaFromBursts() {
            for (var output : getOutputs()) {
                try (var guard = output.getAdjacentCapability()) {
                    var result = guard.get();

                    if (result.canReceiveManaFromBursts()) {
                        return true;
                    }
                }
            }

            return false;
        }

        @Override
        public boolean isOutputtingPower() {
            return false;
        }

        @Override
        public int getMaxMana() {
            return getOutputStream()
                    .map(part -> {
                        try (var guard = part.getAdjacentCapability()) {
                            return ManaHelper.getCapacity(guard.get());
                        }
                    })
                    .reduce(0, Integer::sum);
        }

        @Override
        public Optional<EnumDyeColor> getColor() {
            return Optional.of(EnumDyeColor.PURPLE);
        }

        @Override
        public void setColor(Optional<EnumDyeColor> color) {
        }

        @Override
        public int insert(int amount, Actionable mode) {
            var inserted = 0;

            for (var output : getOutputs()) {
                try (var guard = output.getAdjacentCapability()) {
                    inserted += SafeMana.conv(guard.get()).insert(amount - inserted, mode);
                }
            }

            return inserted;
        }

        @Override
        public int extract(int amount, Actionable mode) {
            return 0;
        }
    }

    private class EmptyHandler implements IManaReceiver, SafeMana {

        @Override
        public World getManaReceiverLevel() {
            return getLevel();
        }

        @Override
        public BlockPos getManaReceiverPos() {
            return getHost().getLocation().getPos();
        }

        @Override
        public int getCurrentMana() {
            return 0;
        }

        @Override
        public boolean isFull() {
            return true;
        }

        @Override
        public void receiveMana(int mana) {
        }

        @Override
        public boolean canReceiveManaFromBursts() {
            return false;
        }

        @Override
        public int insert(int amount, Actionable mode) {
            return 0;
        }

        @Override
        public int extract(int amount, Actionable mode) {
            return 0;
        }
    }
}
