package traben.entity_pin_cushions.mixin;

import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import traben.entity_pin_cushions.StuckSpectralArrowsFeatureRenderer;
import traben.entity_pin_cushions.PinCushionLayer;

@Mixin(value = LivingEntityRenderer.class, priority = 2000)
public abstract class MixinAddLayer<T extends LivingEntity, M extends EntityModel<T>> {

    private static final Logger LOGGER = LoggerFactory.getLogger("EntityPinCushions-MixinAddLayer");
    
    @Shadow
    protected M model;

    @Shadow
    protected abstract boolean addLayer(RenderLayer<T, M> layer);
    
    private static boolean citadelAvailable = false;
    private static Class<?> advancedEntityModelClass;
    
    static {
        try {
            advancedEntityModelClass = Class.forName("com.github.alexthe666.citadel.client.model.AdvancedEntityModel");
            citadelAvailable = true;
            LOGGER.info("AdvancedEntityModel class found!");
        } catch (Exception e) {
            citadelAvailable = false;
            LOGGER.warn("AdvancedEntityModel class not found: {}", e.getMessage());
        }
    }

    @Inject(method = "<init>", at = @At(value = "TAIL"))
    private void allStuckArrows$mixin(EntityRendererProvider.Context context, EntityModel<?> model, float shadowRadius, CallbackInfo ci) {
        boolean shouldAddLayers = false;
        
        LOGGER.info("Checking model: {} for layer addition", this.model.getClass().getName());
        
        // Проверка на стандартные модели Minecraft
        if (this.model instanceof AgeableListModel || this.model instanceof HierarchicalModel) {
            LOGGER.info("Model is AgeableListModel or HierarchicalModel, adding layers");
            shouldAddLayers = true;
        }
        
        if (!shouldAddLayers && citadelAvailable && advancedEntityModelClass != null) {
            if (advancedEntityModelClass.isInstance(this.model)) {
                LOGGER.info("Model is AdvancedEntityModel, adding layers");
                shouldAddLayers = true;
            }
        }
        
        if (!shouldAddLayers) {
            try {
                Class<?> advancedModelBoxClass = Class.forName("com.github.alexthe666.citadel.client.model.AdvancedModelBox");
                // Проверяем, есть ли поля типа AdvancedModelBox
                for (java.lang.reflect.Field field : this.model.getClass().getDeclaredFields()) {
                    if (advancedModelBoxClass.isAssignableFrom(field.getType())) {
                        LOGGER.info("Model contains AdvancedModelBox field: {}, adding layers", field.getName());
                        shouldAddLayers = true;
                        break;
                    }
                }
            } catch (Exception e) {
            }
        }
        
        if (shouldAddLayers) {
            @SuppressWarnings("unchecked")
            LivingEntityRenderer<T, M> self = (LivingEntityRenderer<T, M>) (Object) this;
            addLayer(new PinCushionLayer.ArrowLayer<>(context, self));
            addLayer(new PinCushionLayer.BeeStingerLayer<>(self));
            addLayer(new StuckSpectralArrowsFeatureRenderer<>(context, self));
            LOGGER.info("Layers added successfully for model: {}", this.model.getClass().getName());
        } else {
            LOGGER.warn("No layers added for model: {}", this.model.getClass().getName());
        }
    }
}