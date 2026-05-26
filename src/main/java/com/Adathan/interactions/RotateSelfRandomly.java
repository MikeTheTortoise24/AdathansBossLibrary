package com.Adathan.interactions;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Rotation3f;
import org.joml.Vector3f;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.Random;

public class RotateSelfRandomly extends SimpleInstantInteraction {
    public static final BuilderCodec CODEC;


    @Override
    protected void firstRun(@Nonnull InteractionType interactionType, @Nonnull InteractionContext interactionContext, @Nonnull CooldownHandler cooldownHandler) {
        CommandBuffer<EntityStore> commandBuffer = interactionContext.getCommandBuffer();
        World world = interactionContext.getEntity().getStore().getExternalData().getWorld();
//        Ref<EntityStore> entityRef = interactionContext.getOwningEntity();
        Ref<EntityStore> entityRef = interactionContext.getEntity();

        HeadRotation headRotation = commandBuffer.getComponent(entityRef, HeadRotation.getComponentType());
        TransformComponent transformComponent = commandBuffer.getComponent(entityRef, TransformComponent.getComponentType());
        Random rand = new Random();
        int randomDegree = rand.nextInt(361);
        Rotation3f curRot = headRotation.getRotation();
        Rotation3f rotationVector = new Rotation3f(curRot.x(), (float) Math.toRadians(randomDegree) + curRot.y(), curRot.z());
        world.execute(() -> {
            headRotation.setRotation(rotationVector);
            transformComponent.setRotation(rotationVector);
        });
    }

    protected void simulateFirstRun(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
    }

    static {
        CODEC = BuilderCodec.builder(RotateSelfRandomly.class, RotateSelfRandomly::new, SimpleInstantInteraction.CODEC)
                .documentation("Rotates the entity randomly about the Y axis")
                .build();
    }
}
