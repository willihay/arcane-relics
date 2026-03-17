package org.bensam.arcanerelics;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;

import java.util.function.Consumer;

public final class ModComponents {
    private ModComponents() {}

    public static final DataComponentType<WandChargesComponent> WAND_CHARGES_COMPONENT = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Identifier.fromNamespaceAndPath(ArcaneRelics.MOD_ID, "wand_charges"),
            DataComponentType.<WandChargesComponent>builder()
                    .persistent(WandChargesComponent.CODEC)
                    .build()
    );

    public static final DataComponentType<WandTooltipComponent> WAND_TOOLTIP_COMPONENT = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Identifier.fromNamespaceAndPath(ArcaneRelics.MOD_ID, "wand_tooltip"),
            DataComponentType.<WandTooltipComponent>builder()
                    .persistent(WandTooltipComponent.CODEC)
                    .build()
    );

    public record WandChargesComponent(int charges) implements TooltipProvider {
        public static final Codec<WandChargesComponent> CODEC =
                Codec.INT.xmap(WandChargesComponent::new, WandChargesComponent::charges);

        @Override
        public void addToTooltip(
                Item.TooltipContext tooltip,
                Consumer<Component> textConsumer,
                TooltipFlag type,
                DataComponentGetter components
        ) {
            textConsumer.accept(Component.translatable("item." + ArcaneRelics.MOD_ID + ".wand.charges", this.charges)
                    .withStyle(ChatFormatting.GOLD));
        }
    }

    public record WandTooltipComponent(String translationKeyPrefix, int lineCount) implements TooltipProvider {
        public static final Codec<WandTooltipComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("translation_key_prefix").forGetter(WandTooltipComponent::translationKeyPrefix),
                Codec.INT.fieldOf("line_count").forGetter(WandTooltipComponent::lineCount)
        ).apply(instance, WandTooltipComponent::new));

        @Override
        public void addToTooltip(
                Item.TooltipContext tooltip,
                Consumer<Component> textConsumer,
                TooltipFlag type,
                DataComponentGetter components
        ) {
            for (int line = 1; line <= this.lineCount; line++) {
                textConsumer.accept(
                        Component.translatable(this.translationKeyPrefix + "." + line)
                                .withStyle(ChatFormatting.GRAY)
                );
            }
        }
    }

    protected static void initialize() {
        ArcaneRelics.LOGGER.info("Registering components");
    }
}
