package appbot.botania;

import java.util.ArrayList;
import java.util.List;

import com.google.common.primitives.Ints;

import org.jetbrains.annotations.Nullable;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.WorldServer;
import net.minecraft.entity.EntityPlayer.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import appbot.AppliedBotanics;
import vazkii.botania.api.corporea.CorporeaNode;
import vazkii.botania.api.corporea.CorporeaRequest;
import vazkii.botania.api.corporea.CorporeaSpark;
import vazkii.botania.common.impl.corporea.AbstractCorporeaNode;

import ae2.api.config.Actionable;
import ae2.api.networking.security.IActionSource;
import ae2.api.stacks.AEItemKey;
import ae2.api.storage.MEStorage;

public class MECorporeaNode extends AbstractCorporeaNode {

    private final MEStorage storage;

    public MECorporeaNode(World World, BlockPos pos, CorporeaSpark spark, MEStorage storage) {
        super(World, pos, spark);
        this.storage = storage;
    }

    @Nullable
    public static CorporeaNode getNode(World World, CorporeaSpark spark) {
        if (!(World instanceof WorldServer WorldServer)) {
            // todo: client-side animation?
            return null;
        }

        var accessor = AppliedBotanics.getInstance().meStorage(WorldServer, spark.getAttachPos())
                .find(EnumFacing.UP);

        if (accessor != null) {
            return new MECorporeaNode(World, spark.getAttachPos(), spark, accessor);
        }

        return null;
    }

    @Override
    public List<ItemStack> countItems(CorporeaRequest request) {
        return work(request, false);
    }

    @Override
    public List<ItemStack> extractItems(CorporeaRequest request) {
        return work(request, true);
    }

    protected List<ItemStack> work(CorporeaRequest request, boolean execute) {
        var list = new ArrayList<ItemStack>();
        IActionSource source;

        if (request.getEntity() instanceof EntityPlayer EntityPlayer) {
            source = IActionSource.ofPlayer(EntityPlayer);
        } else {
            source = IActionSource.empty();
        }

        if (storage == null) {
            return list;
        }

        for (var entry : storage.getAvailableStacks()) {
            var amount = Ints.saturatedCast(entry.getLongValue());

            if (entry.getKey() instanceof AEItemKey itemKey) {
                var stack = itemKey.toStack();

                if (request.getMatcher().test(stack)) {
                    request.trackFound(amount);
                    var remainder = Math.min(amount,
                            request.getStillNeeded() == -1 ? amount : request.getStillNeeded());

                    if (remainder > 0) {
                        request.trackSatisfied(remainder);

                        if (execute) {
                            if (!getSpark().isCreative()) {
                                remainder = (int) storage.extract(entry.getKey(), remainder, Actionable.MODULATE,
                                        source);
                            }

                            getSpark().onItemExtracted(stack);
                            request.trackExtracted(remainder);
                        }

                        while (remainder > 0) {
                            var taken = Math.min(remainder, stack.getMaxStackSize());
                            remainder -= taken;
                            list.add(itemKey.toStack(taken));
                        }
                    }
                }
            }
        }

        return list;
    }
}
