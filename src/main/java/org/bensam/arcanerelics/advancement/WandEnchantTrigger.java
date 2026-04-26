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

import java.util.Optional;

public class WandEnchantTrigger extends SimpleCriterionTrigger<WandEnchantTrigger.TriggerInstance> {
    @Override
    public Codec<WandEnchantTrigger.TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, ItemStack enchantedWand) {
        this.trigger(player, instance -> instance.matches(enchantedWand));
    }

    public record TriggerInstance(
            Optional<ContextAwarePredicate> player,
            Optional<Holder<Item>> wand
    ) implements SimpleInstance {
        public static final  Codec<WandEnchantTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
                builder -> builder.group(
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                        Item.CODEC.optionalFieldOf("wand").forGetter(TriggerInstance::wand)
                )
                        .apply(builder, TriggerInstance::new)
        );

        public static Criterion<TriggerInstance> enchantedAnyWand() {
            return ModAdvancements.ENCHANT_WAND_TRIGGER.get().createCriterion(
                    new TriggerInstance(Optional.empty(), Optional.empty())
            );
        }

        public static Criterion<TriggerInstance> enchantedWand(ItemLike wand) {
            return ModAdvancements.ENCHANT_WAND_TRIGGER.get().createCriterion(
                    new TriggerInstance(Optional.empty(), Optional.of(BuiltInRegistries.ITEM.wrapAsHolder(wand.asItem())))
            );
        }

        public boolean matches(ItemStack enchantedWand) {
            return this.wand.isEmpty() || enchantedWand.is(this.wand.get());
        }
    }
}
