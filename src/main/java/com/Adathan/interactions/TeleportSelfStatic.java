package com.Adathan.interactions;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Rotation3f;
import org.joml.Vector3d;
import org.joml.Vector3f;
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

public class TeleportSelfStatic extends SimpleInstantInteraction {
    public static final BuilderCodec CODEC;

    protected Double staticX = 0.0;
    protected Double staticY = 0.0;
    protected Double staticZ = 0.0;
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

                        Rotation3f previousBodyRotation = transformComponent.getRotation().clone();

                        double x = staticX;
                        double z = staticZ;
                        double y = staticY;
                        float yaw = yawArg;
                        float pitch = pitchArg;
                        float roll = rollArg;

                        if (yaw == 0.0) {
                            yaw = previousBodyRotation.yaw();
                        }
                        if (pitch == 0.0) {
                            pitch = previousBodyRotation.pitch();
                        }
                        if (roll == 0.0) {
                            roll = previousBodyRotation.roll();
                        }

                        Teleport teleport = Teleport.createExact(
                                new Vector3d(x, y, z), new Rotation3f(previousBodyRotation.pitch(), yaw, previousBodyRotation.roll()), new Rotation3f(pitch, yaw, roll)
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
        CODEC = BuilderCodec.builder(TeleportSelfStatic.class, TeleportSelfStatic::new, SimpleInstantInteraction.CODEC)
                .documentation("Teleports self entity to static coordinates.")
                .append(new KeyedCodec<>("StaticX", Codec.DOUBLE),
                        (ExecuteInteraction, o) -> ExecuteInteraction.staticX =(Double) o,
                        (ExecuteInteraction) -> ExecuteInteraction.staticX)
                .documentation("Static X to teleport to").addValidator(Validators.nonNull()).add()
                .append(new KeyedCodec<>("StaticY", Codec.DOUBLE),
                        (ExecuteInteraction, o) -> ExecuteInteraction.staticY =(Double) o,
                        (ExecuteInteraction) -> ExecuteInteraction.staticY)
                .documentation("Static Y to teleport to").addValidator(Validators.nonNull()).add()
                .append(new KeyedCodec<>("StaticZ", Codec.DOUBLE),
                        (ExecuteInteraction, o) -> ExecuteInteraction.staticZ =(Double) o,
                        (ExecuteInteraction) -> ExecuteInteraction.staticZ)
                .documentation("Static Z to teleport to").addValidator(Validators.nonNull()).add()
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