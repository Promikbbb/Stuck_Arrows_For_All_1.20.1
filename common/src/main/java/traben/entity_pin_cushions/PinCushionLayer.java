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
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public abstract class PinCushionLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger("EntityPinCushions");
    
    private static Class<?> advancedModelBoxClass;
    private static Method advancedModelBoxTranslateAndRotate;
    private static Field advancedModelBoxCubesField;
    private static Field advancedModelBoxShowModelField;
    private static boolean citadelAvailable = false;
    
    static {
        try {
            LOGGER.info("Checking for Citadel library...");
            advancedModelBoxClass = Class.forName("com.github.alexthe666.citadel.client.model.AdvancedModelBox");
            
            advancedModelBoxTranslateAndRotate = advancedModelBoxClass.getMethod("translateAndRotate", PoseStack.class);
            advancedModelBoxCubesField = advancedModelBoxClass.getField("cubeList");
            advancedModelBoxShowModelField = advancedModelBoxClass.getField("showModel");
            
            citadelAvailable = true;
            LOGGER.info("Citadel library detected and loaded successfully!");
        } catch (Exception e) {
            citadelAvailable = false;
            LOGGER.warn("Citadel library not found: {}", e.getMessage());
        }
    }
    
    public PinCushionLayer(LivingEntityRenderer<T, M> renderer) {
        super(renderer);
    }

    protected abstract int numStuck(T entity);

    protected abstract void renderStuckItem(PoseStack poseStack, MultiBufferSource buffer, int packedLight, Entity entity, float x, float y, float z, float partialTick);

    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T livingEntity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        int i = this.numStuck(livingEntity);
        
        RandomSource randomSource = RandomSource.create(livingEntity.getId());
        if (i > 0) {
            M model = getParentModel();
            
            for (int j = 0; j < i; ++j) {
                Random partRand = new Random(j);
                poseStack.pushPose();
                
                Object advancedModelBox = findRandomAdvancedModelBox(model, partRand);
                
                if (advancedModelBox == null) {
                    poseStack.popPose();
                    continue;
                }

                try {
                    advancedModelBoxTranslateAndRotate.invoke(advancedModelBox, poseStack);
                } catch (Exception e) {
                    poseStack.popPose();
                    continue;
                }

                float f = randomSource.nextFloat();
                float g = randomSource.nextFloat();
                float h = randomSource.nextFloat();

                f = -1.0F * (f * 2.0F - 1.0F);
                g = -1.0F * (g * 2.0F - 1.0F);
                h = -1.0F * (h * 2.0F - 1.0F);
                
                this.renderStuckItem(poseStack, buffer, packedLight, livingEntity, f, g, h, partialTicks);
                poseStack.popPose();
            }
        }
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
            LOGGER.info("Found {} parts via getAllParts()", allBoxes.size());
        } catch (Exception e) {
            for (Field field : model.getClass().getDeclaredFields()) {
                try {
                    field.setAccessible(true);
                    Object value = field.get(model);
                    if (value != null && advancedModelBoxClass.isInstance(value)) {
                        allBoxes.add(value);
                        LOGGER.info("Found AdvancedModelBox field: {}", field.getName());
                    }
                } catch (IllegalAccessException ex) {
                }
            }
            LOGGER.info("Found {} parts via fields", allBoxes.size());
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
    private Pair<ModelPart, Runnable> findRandomModelPartOld(M model, Random random, PoseStack poseStack) {
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

    public static class ArrowLayer<T extends LivingEntity, M extends EntityModel<T>> extends PinCushionLayer<T, M> {
        private final EntityRenderDispatcher dispatcher;

        public ArrowLayer(EntityRendererProvider.Context context, LivingEntityRenderer<T, M> renderer) {
            super(renderer);
            this.dispatcher = context.getEntityRenderDispatcher();
        }

        protected int numStuck(T entity) {
            LOGGER.info("TEST: Forcing arrow render on entity: {}", entity.getClass().getSimpleName());
            return 1;
            // return entity.getArrowCount();
        }

        protected void renderStuckItem(PoseStack poseStack, MultiBufferSource buffer, int packedLight, Entity entity, float x, float y, float z, float partialTick) {
            float f = Mth.sqrt(x * x + z * z);
            Arrow arrow = new Arrow(entity.level(), entity.getX(), entity.getY(), entity.getZ());
            arrow.setYRot((float) (Math.atan2(x, z) * 57.2957763671875));
            arrow.setXRot((float) (Math.atan2(y, f) * 57.2957763671875));
            arrow.yRotO = arrow.getYRot();
            arrow.xRotO = arrow.getXRot();
            
            this.dispatcher.render(arrow, 0.0, 0.0, 0.0, 0.0F, partialTick, poseStack, buffer, packedLight);
        }
    }

    public static class BeeStingerLayer<T extends LivingEntity, M extends EntityModel<T>> extends PinCushionLayer<T, M> {
        private static final ResourceLocation BEE_STINGER_LOCATION = new ResourceLocation("textures/entity/bee/bee_stinger.png");

        public BeeStingerLayer(LivingEntityRenderer<T, M> renderer) {
            super(renderer);
        }

        protected int numStuck(T entity) {
            return entity.getStingerCount();
        }

        protected void renderStuckItem(PoseStack poseStack, MultiBufferSource buffer, int packedLight, Entity entity, float x, float y, float z, float partialTick) {
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
    }
}