package org.bensam.arcanerelics.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.bensam.arcanerelics.ModAdvancements;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

public class WandRechargeTrigger extends SimpleCriterionTrigger<WandRechargeTrigger.TriggerInstance> {
    @Override
    public @NonNull Codec<WandRechargeTrigger.TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, ItemStack rechargedWand, boolean wasAlternateSource) {
        this.trigger(player, instance -> instance.matches(rechargedWand, wasAlternateSource));
    }

    public record TriggerInstance(
            Optional<ContextAwarePredicate> player,
            Optional<Holder<Item>> wand,
            Optional<Boolean> alternateSource
    ) implements SimpleInstance {
        public static final  Codec<WandRechargeTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
                builder -> builder.group(
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                        Item.CODEC.optionalFieldOf("wand").forGetter(TriggerInstance::wand),
                        Codec.BOOL.optionalFieldOf("alternate_source").forGetter(TriggerInstance::alternateSource)
                )
                        .apply(builder, TriggerInstance::new)
        );

        public static Criterion<TriggerInstance> rechargedAnyWandInTable() {
            return ModAdvancements.RECHARGE_WAND_TRIGGER.get().createCriterion(
                    new TriggerInstance(Optional.empty(), Optional.empty(), Optional.of(false))
            );
        }

        public static Criterion<TriggerInstance> rechargedWandInTable(ItemLike wand) {
            return ModAdvancements.RECHARGE_WAND_TRIGGER.get().createCriterion(
                    new TriggerInstance(Optional.empty(), Optional.of(BuiltInRegistries.ITEM.wrapAsHolder(wand.asItem())), Optional.of(false))
            );
        }

        public static Criterion<TriggerInstance> rechargedAnyWandFromAlternateSource() {
            return ModAdvancements.RECHARGE_WAND_TRIGGER.get().createCriterion(
                    new TriggerInstance(Optional.empty(), Optional.empty(), Optional.of(true))
            );
        }

        public static Criterion<TriggerInstance> rechargedWandFromAlternateSource(ItemLike wand) {
            return ModAdvancements.RECHARGE_WAND_TRIGGER.get().createCriterion(
                    new TriggerInstance(Optional.empty(), Optional.of(BuiltInRegistries.ITEM.wrapAsHolder(wand.asItem())), Optional.of(true))
            );
        }

        public boolean matches(ItemStack rechargedWand, boolean wasAlternateSource) {
            return (this.wand.isEmpty() || rechargedWand.is(this.wand.get()))
                    && (this.alternateSource.isEmpty() || this.alternateSource.get() == wasAlternateSource);
        }
    }
}
