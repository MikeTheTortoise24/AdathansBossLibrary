package com.Adathan.interactions;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.modules.projectile.ProjectileModule;
import com.hypixel.hytale.server.core.modules.projectile.config.ProjectileConfig;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

public class FireProjectilesSetIntervals extends SimpleInstantInteraction {
    public static final BuilderCodec CODEC;

    protected String projectileConfigName;
    protected Integer count = 3;
    protected Float offsetFromCenter = 0.0f;
    protected Float speedChange = 0.0f;
    protected Boolean angledAwayFromMiddle = false;
    protected Boolean angledTowardsMiddle = false;

    @Override
    protected void firstRun(@Nonnull InteractionType interactionType, @Nonnull InteractionContext interactionContext, @Nonnull CooldownHandler cooldownHandler) {
        CommandBuffer<EntityStore> commandBuffer = interactionContext.getCommandBuffer();
        World world = interactionContext.getEntity().getStore().getExternalData().getWorld();
        Ref<EntityStore> entityRef = interactionContext.getOwningEntity();
        Store<EntityStore> store = world.getEntityStore().getStore();
        Vector3d startingPos = (commandBuffer.getComponent(entityRef, TransformComponent.getComponentType())).getPosition();
        Vector3d direction = (commandBuffer.getComponent(entityRef, HeadRotation.getComponentType()).getDirection());

        ProjectileModule projectileModule = ProjectileModule.get();
        ProjectileConfig projectileConfig = ProjectileConfig.getAssetMap().getAsset(projectileConfigName);

        Vector3d flatForward = new Vector3d(direction.x, 0, direction.z).normalize();

        double baseAngle = Math.atan2(flatForward.z, flatForward.x);

        for (int i = 0; i < count; i++) {

            double angle = baseAngle + (2 * Math.PI * i) / count;

            double x = startingPos.x + offsetFromCenter * Math.cos(angle);
            double z = startingPos.z + offsetFromCenter * Math.sin(angle);
            double y = startingPos.y;

            Vector3d newPos = new Vector3d(x, y, z);

            if (angledAwayFromMiddle) {
                direction = new Vector3d(newPos).subtract(startingPos).normalize();
            } else if (angledTowardsMiddle) {
                direction = new Vector3d(startingPos).subtract(newPos).normalize();
            }

            projectileModule.spawnProjectile(entityRef, commandBuffer, projectileConfig, newPos, direction);
        }

    }

    protected void simulateFirstRun(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
    }

    static {
        CODEC = BuilderCodec.builder(FireProjectilesSetIntervals.class, FireProjectilesSetIntervals::new, SimpleInstantInteraction.CODEC)
                .documentation("Spawn projectiles at an even degree angle from eachother.")
                .append(new KeyedCodec<>("ProjectileConfigName", Codec.STRING),
                        (ExecuteInteraction, o) -> ExecuteInteraction.projectileConfigName =(String) o,
                        (ExecuteInteraction) -> ExecuteInteraction.projectileConfigName)
                .documentation("Root Interaction to execute").addValidator(Validators.nonNull()).add()
                .append(new KeyedCodec<>("Count", Codec.INTEGER),
                (ExecuteInteraction, o) -> ExecuteInteraction.count = o,
                (ExecuteInteraction) -> ExecuteInteraction.count)
                .documentation("Amount of projectiles to spawn.").add()
                .append(new KeyedCodec<>("OffsetFromCenter", Codec.FLOAT),
                        (ExecuteInteraction, o) -> ExecuteInteraction.offsetFromCenter = o,
                        (ExecuteInteraction) -> ExecuteInteraction.offsetFromCenter)
                .documentation("How far offset from the center the projectiles should be.").add()
                .append(new KeyedCodec<>("SpeedChange", Codec.FLOAT),
                        (ExecuteInteraction, o) -> ExecuteInteraction.speedChange = o,
                        (ExecuteInteraction) -> ExecuteInteraction.speedChange)
                .documentation("Should the projectiles change speed?").add()
                .append(new KeyedCodec<>("AngledAwayFromMiddle", Codec.BOOLEAN),
                        (ExecuteInteraction, o) -> ExecuteInteraction.angledAwayFromMiddle = o,
                        (ExecuteInteraction) -> ExecuteInteraction.angledAwayFromMiddle)
                .documentation("Should the projectiles angle directly away from the middle?").add()
                .append(new KeyedCodec<>("AngledTowardsMiddle", Codec.BOOLEAN),
                        (ExecuteInteraction, o) -> ExecuteInteraction.angledTowardsMiddle = o,
                        (ExecuteInteraction) -> ExecuteInteraction.angledTowardsMiddle)
                .documentation("Should the projectiles angle directly toward the middle?").add()
                .build();
    }
}
