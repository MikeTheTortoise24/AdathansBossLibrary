package com.Adathan.interactions;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class TeleportSelfRelative extends SimpleInstantInteraction {
    public static final BuilderCodec CODEC;

    protected Double relativeX = 0.0;
    protected Double relativeY = 0.0;
    protected Double relativeZ = 0.0;
    protected Float yawArg = 0.0F;
    protected Float pitchArg = 0.0F;
    protected Float rollArg = 0.0F;

    @Override
    protected void firstRun(@Nonnull InteractionType interactionType, @Nonnull InteractionContext interactionContext, @Nonnull CooldownHandler cooldownHandler) {
        Ref<EntityStore> entityRef = interactionContext.getOwningEntity();

        if (entityRef != null && entityRef.isValid()) {
            Store<EntityStore> store = entityRef.getStore();
            World targetWorld = store.getExternalData().getWorld();
            targetWorld.execute(
                    () -> {
                        TransformComponent transformComponent = store.getComponent(entityRef, TransformComponent.getComponentType());

                        assert transformComponent != null;

                        HeadRotation headRotationComponent = store.getComponent(entityRef, HeadRotation.getComponentType());

                        assert headRotationComponent != null;

                        Vector3d previousPos = transformComponent.getPosition().clone();
                        Vector3f previousBodyRotation = transformComponent.getRotation().clone();

                        double x = relativeX + previousPos.getX();
                        double z = relativeZ + previousPos.getZ();
                        double y = relativeY + previousPos.getY();
                        float yaw = yawArg;
                        float pitch = pitchArg;
                        float roll = rollArg;

                        if (yaw == 0.0) {
                            yaw = previousBodyRotation.getYaw();
                        }
                        if (pitch == 0.0) {
                            pitch = previousBodyRotation.getPitch();
                        }
                        if (roll == 0.0) {
                            roll = previousBodyRotation.getRoll();
                        }

                        Teleport teleport = Teleport.createExact(
                                new Vector3d(x, y, z), new Vector3f(previousBodyRotation.getPitch(), yaw, previousBodyRotation.getRoll()), new Vector3f(pitch, yaw, roll)
                        );
                        store.addComponent(entityRef, Teleport.getComponentType(), teleport);

                        HytaleLogger.getLogger().atInfo().log("Teleported to coordinates X: ", x, " Y: ", y, " Z: ", z);
                    }
            );
        } else {
            HytaleLogger.getLogger().atInfo().log("Entity is not in this world");
        }
    }

    protected void simulateFirstRun(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
    }

    static {
        CODEC = BuilderCodec.builder(TeleportSelfRelative.class, TeleportSelfRelative::new, SimpleInstantInteraction.CODEC)
                .documentation("Teleports self entity relative to its current position.")
                .append(new KeyedCodec<>("RelativeX", Codec.DOUBLE),
                        (ExecuteInteraction, o) -> ExecuteInteraction.relativeX =(Double) o,
                        (ExecuteInteraction) -> ExecuteInteraction.relativeX)
                .documentation("Relative X to add").addValidator(Validators.nonNull()).add()
                .append(new KeyedCodec<>("RelativeY", Codec.DOUBLE),
                        (ExecuteInteraction, o) -> ExecuteInteraction.relativeY =(Double) o,
                        (ExecuteInteraction) -> ExecuteInteraction.relativeY)
                .documentation("Relative Y to add").addValidator(Validators.nonNull()).add()
                .append(new KeyedCodec<>("RelativeZ", Codec.DOUBLE),
                        (ExecuteInteraction, o) -> ExecuteInteraction.relativeZ =(Double) o,
                        (ExecuteInteraction) -> ExecuteInteraction.relativeZ)
                .documentation("Relative Z to add").addValidator(Validators.nonNull()).add()
                .append(new KeyedCodec<>("Yaw", Codec.FLOAT),
                        (ExecuteInteraction, o) -> ExecuteInteraction.yawArg =(Float) o,
                        (ExecuteInteraction) -> ExecuteInteraction.yawArg)
                .documentation("Yaw to set entity to").addValidator(Validators.nonNull()).add()
                .append(new KeyedCodec<>("Pitch", Codec.FLOAT),
                        (ExecuteInteraction, o) -> ExecuteInteraction.pitchArg =(Float) o,
                        (ExecuteInteraction) -> ExecuteInteraction.pitchArg)
                .documentation("Pitch to set entity to").addValidator(Validators.nonNull()).add()
                .append(new KeyedCodec<>("Roll", Codec.FLOAT),
                        (ExecuteInteraction, o) -> ExecuteInteraction.rollArg =(Float) o,
                        (ExecuteInteraction) -> ExecuteInteraction.rollArg)
                .documentation("Roll to set entity to").addValidator(Validators.nonNull()).add()
                .build();
    }
}