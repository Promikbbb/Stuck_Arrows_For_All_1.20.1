package traben.entity_pin_cushions;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.FrogModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.SpectralArrow;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class PinCushionLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger("EntityPinCushions");
    private static final ResourceLocation BEE_STINGER_LOCATION = new ResourceLocation("textures/entity/bee/bee_stinger.png");
    
    private static Class<?> advancedModelBoxClass;
    private static Method advancedModelBoxTranslateAndRotate;
    private static Field advancedModelBoxCubesField;
    private static Field advancedModelBoxShowModelField;
    private static boolean citadelAvailable = false;
    
    private final EntityRenderDispatcher dispatcher;
    
    static {
        try {
            advancedModelBoxClass = Class.forName("com.github.alexthe666.citadel.client.model.AdvancedModelBox");
            advancedModelBoxTranslateAndRotate = advancedModelBoxClass.getMethod("translateAndRotate", PoseStack.class);
            advancedModelBoxCubesField = advancedModelBoxClass.getField("cubeList");
            advancedModelBoxShowModelField = advancedModelBoxClass.getField("showModel");
            citadelAvailable = true;
        } catch (Exception e) {
            citadelAvailable = false;
        }
    }
    
    public PinCushionLayer(EntityRendererProvider.Context context, LivingEntityRenderer<T, M> renderer) {
        super(renderer);
        this.dispatcher = context.getEntityRenderDispatcher();
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T livingEntity, 
                      float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, 
                      float netHeadYaw, float headPitch) {
        
        int arrowCount = livingEntity.getArrowCount();
        int spectralCount = LivingEntityDataHelper.getStuckSpectralArrowCount(livingEntity);
        int stingerCount = livingEntity.getStingerCount();
        
        int totalCount = arrowCount + spectralCount + stingerCount;
        if (totalCount == 0) return;
        
        RandomSource randomSource = RandomSource.create(livingEntity.getId());
        M model = getParentModel();
        
        for (int j = 0; j < arrowCount; ++j) {
            renderSingleStuckItem(poseStack, buffer, packedLight, livingEntity, randomSource, model, partialTicks,
                (stack, buf, light, entity, x, y, z, partial) -> {
                    renderStuckArrow(stack, buf, light, entity, x, y, z, partial);
                });
        }
        
        for (int j = 0; j < spectralCount; ++j) {
            renderSingleStuckItem(poseStack, buffer, packedLight, livingEntity, randomSource, model, partialTicks,
                (stack, buf, light, entity, x, y, z, partial) -> {
                    renderStuckSpectralArrow(stack, buf, light, entity, x, y, z, partial);
                });
        }
        
        for (int j = 0; j < stingerCount; ++j) {
            renderSingleStuckItem(poseStack, buffer, packedLight, livingEntity, randomSource, model, partialTicks,
                (stack, buf, light, entity, x, y, z, partial) -> {
                    renderStuckStinger(stack, buf, light, entity, x, y, z, partial);
                });
        }
    }
    
    private void renderSingleStuckItem(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                                       T livingEntity, RandomSource randomSource, M model, float partialTicks,
                                       StuckItemRenderer renderer) {
        Random partRand = new Random(randomSource.nextLong());
        poseStack.pushPose();
        
        boolean found = false;
        
        if (citadelAvailable) {
            Object advancedModelBox = findRandomAdvancedModelBox(model, partRand);
            if (advancedModelBox != null) {
                try {
                    advancedModelBoxTranslateAndRotate.invoke(advancedModelBox, poseStack);
                    found = true;
                } catch (Exception e) {
                }
            }
        }
        
        if (!found) {
            Pair<ModelPart, Runnable> vanillaPart = findRandomVanillaModelPart(model, partRand, poseStack);
            if (vanillaPart != null) {
                vanillaPart.getSecond().run();
                found = true;
            }
        }
        
        if (!found) {
            poseStack.popPose();
            return;
        }

        float f = (randomSource.nextFloat() - 0.5F) * 2.0F;
        float g = (randomSource.nextFloat() - 0.5F) * 2.0F;
        float h = (randomSource.nextFloat() - 0.5F) * 2.0F;
        
        float len = Mth.sqrt(f * f + g * g + h * h);
        if (len > 0.001F) {
            f /= len;
            g /= len;
            h /= len;
        }
        
        float distance = 0.2F + randomSource.nextFloat() * 0.4F;
        f *= distance;
        g *= distance;
        h *= distance;
        
        renderer.render(poseStack, buffer, packedLight, livingEntity, f, g, h, partialTicks);
        poseStack.popPose();
    }

    @Nullable
    private Object findRandomAdvancedModelBox(M model, Random random) {
        if (!citadelAvailable) return null;
        
        List<Object> allBoxes = new ArrayList<>();
        
        try {
            Method getAllParts = model.getClass().getMethod("getAllParts");
            Iterable<?> parts = (Iterable<?>) getAllParts.invoke(model);
            for (Object part : parts) {
                if (advancedModelBoxClass.isInstance(part)) {
                    allBoxes.add(part);
                }
            }
        } catch (Exception e) {
            for (Field field : model.getClass().getDeclaredFields()) {
                try {
                    field.setAccessible(true);
                    Object value = field.get(model);
                    if (value != null && advancedModelBoxClass.isInstance(value)) {
                        allBoxes.add(value);
                    }
                } catch (IllegalAccessException ex) {
                }
            }
        }
        
        if (allBoxes.isEmpty()) {
            return null;
        }
        
        Collections.shuffle(allBoxes, random);
        
        for (Object box : allBoxes) {
            try {
                boolean showModel = advancedModelBoxShowModelField.getBoolean(box);
                if (!showModel) continue;
                
                List<?> cubes = (List<?>) advancedModelBoxCubesField.get(box);
                if (cubes != null && !cubes.isEmpty()) {
                    return box;
                }
            } catch (Exception e) {
            }
        }
        
        return allBoxes.get(0);
    }
    
    @Nullable
    private Pair<ModelPart, Runnable> findRandomVanillaModelPart(M model, Random random, PoseStack poseStack) {
        if (model instanceof AgeableListModel<?> animal) {
            return bestFromList(animal.headParts(), animal.bodyParts(), random, poseStack);
        } else if (model instanceof FrogModel<?> frogModel) {
            return bestFromListMutable(new ArrayList<>(Collections.singleton(frogModel.root())), random, poseStack, true);
        } else if (model instanceof HierarchicalModel<?> hierarchicalModel) {
            return bestFromListMutable(new ArrayList<>(hierarchicalModel.root().children.values()), random, poseStack, true);
        }
        return null;
    }

    @Nullable
    private Pair<ModelPart, Runnable> bestFromList(Iterable<ModelPart> part1, Iterable<ModelPart> part2, Random randomSource, PoseStack poseStack) {
        List<ModelPart> list = new ArrayList<>();
        part1.forEach(list::add);
        part2.forEach(list::add);
        return bestFromListMutable(list, randomSource, poseStack, true);
    }

    @Nullable
    private Pair<ModelPart, Runnable> bestFromListMutable(List<ModelPart> partsMutable, Random randomSource, PoseStack poseStack, boolean firstIteration) {
        Collections.shuffle(partsMutable, randomSource);
        for (ModelPart modelPart : partsMutable) {
            if (modelPart.visible) {
                if (!modelPart.cubes.isEmpty() && !modelPart.skipDraw) {
                    return Pair.of(modelPart, () -> modelPart.translateAndRotate(poseStack));
                }
                if (modelPart.children.isEmpty()) continue;

                var child = bestFromListMutable(new ArrayList<>(modelPart.children.values()), randomSource, poseStack, false);
                if (child != null) {
                    var runnable = child.getSecond();
                    return Pair.of(child.getFirst(), () -> {
                        modelPart.translateAndRotate(poseStack);
                        runnable.run();
                    });
                }
            }
        }
        if (firstIteration && !partsMutable.isEmpty()) {
            var part = partsMutable.get(0);
            return Pair.of(part, () -> part.translateAndRotate(poseStack));
        }
        return null;
    }
    
    private void renderStuckArrow(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                                  Entity entity, float x, float y, float z, float partialTick) {
        float f = Mth.sqrt(x * x + z * z);
        Arrow arrow = new Arrow(entity.level(), entity.getX(), entity.getY(), entity.getZ());
        arrow.setYRot((float) (Math.atan2(x, z) * 57.2957763671875));
        arrow.setXRot((float) (Math.atan2(y, f) * 57.2957763671875));
        arrow.yRotO = arrow.getYRot();
        arrow.xRotO = arrow.getXRot();
        
        this.dispatcher.render(arrow, 0.0, 0.0, 0.0, 0.0F, partialTick, poseStack, buffer, packedLight);
    }
    
    private void renderStuckSpectralArrow(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                                          Entity entity, float x, float y, float z, float partialTick) {
        float f = Mth.sqrt(x * x + z * z);
        SpectralArrow spectralArrow = new SpectralArrow(entity.level(), entity.getX(), entity.getY(), entity.getZ());
        spectralArrow.setYRot((float) (Math.atan2(x, z) * 57.2957763671875));
        spectralArrow.setXRot((float) (Math.atan2(y, f) * 57.2957763671875));
        spectralArrow.yRotO = spectralArrow.getYRot();
        spectralArrow.xRotO = spectralArrow.getXRot();
        
        spectralArrow.tickCount = -9999;
        
        this.dispatcher.render(spectralArrow, 0.0, 0.0, 0.0, 0.0F, partialTick, poseStack, buffer, packedLight);
    }
    
    private void renderStuckStinger(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                                    Entity entity, float x, float y, float z, float partialTick) {
        float f = Mth.sqrt(x * x + z * z);
        float g = (float) (Math.atan2(x, z) * 57.2957763671875);
        float h = (float) (Math.atan2(y, f) * 57.2957763671875);
        poseStack.translate(0.0F, 0.0F, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(g - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(h));
        poseStack.mulPose(Axis.XP.rotationDegrees(45.0F));
        poseStack.scale(0.03125F, 0.03125F, 0.03125F);
        poseStack.translate(2.5F, 0.0F, 0.0F);
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(BEE_STINGER_LOCATION));

        for (int n = 0; n < 4; ++n) {
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            PoseStack.Pose pose = poseStack.last();
            vertex(vertexConsumer, pose, -4.5F, -1, 0.0F, 0.0F, packedLight);
            vertex(vertexConsumer, pose, 4.5F, -1, 0.125F, 0.0F, packedLight);
            vertex(vertexConsumer, pose, 4.5F, 1, 0.125F, 0.0625F, packedLight);
            vertex(vertexConsumer, pose, -4.5F, 1, 0.0F, 0.0625F, packedLight);
        }
    }

    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose, float x, int y, float u, float v, int packedLight) {
        consumer.vertex(pose.pose(), x, (float) y, 0.0F)
                .color(255, 255, 255, 255)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0.0F, 1.0F, 0.0F)
                .endVertex();
    }
    
    @FunctionalInterface
    private interface StuckItemRenderer {
        void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, 
                   Entity entity, float x, float y, float z, float partialTick);
    }
}