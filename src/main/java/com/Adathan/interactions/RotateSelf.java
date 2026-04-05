package com.Adathan.interactions;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionChain;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.InteractionManager;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.interaction.InteractionModule;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import javax.annotation.Nonnull;
import java.util.*;

public class RotateSelf extends SimpleInstantInteraction {
    public static final BuilderCodec CODEC;

    protected Float rotX = 0.0f;
    protected Float rotY = 0.0f;
    protected Float rotZ = 0.0f;

    @Override
    protected void firstRun(@Nonnull InteractionType interactionType, @Nonnull InteractionContext interactionContext, @Nonnull CooldownHandler cooldownHandler) {
        CommandBuffer<EntityStore> commandBuffer = interactionContext.getCommandBuffer();
        World world = interactionContext.getEntity().getStore().getExternalData().getWorld();
        Ref<EntityStore> entityRef = interactionContext.getOwningEntity();


        HeadRotation headRotation = commandBuffer.getComponent(entityRef, HeadRotation.getComponentType());
        TransformComponent transformComponent = commandBuffer.getComponent(entityRef, TransformComponent.getComponentType());

        Vector3f curRot = headRotation.getRotation();
        Vector3f rotationVector = new Vector3f((float) Math.toRadians(rotX) + curRot.getX(), (float) Math.toRadians(rotY) + curRot.getY(), (float) Math.toRadians(rotZ) + curRot.getZ());
        world.execute(() -> {
            headRotation.setRotation(rotationVector);
            transformComponent.setRotation(rotationVector);
        });
    }

    protected void simulateFirstRun(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
    }

    static {
        CODEC = BuilderCodec.builder(RotateSelf.class, RotateSelf::new, SimpleInstantInteraction.CODEC)
                .documentation("Rotates the owning entity X Y Z degrees.")
                .append(new KeyedCodec<>("RotationX", Codec.FLOAT),
                (ExecuteInteraction, o) -> ExecuteInteraction.rotX = o,
                (ExecuteInteraction) -> ExecuteInteraction.rotX)
                .documentation("Rotation on the X axis to add.").add()
                .append(new KeyedCodec<>("RotationY", Codec.FLOAT),
                        (ExecuteInteraction, o) -> ExecuteInteraction.rotY = o,
                        (ExecuteInteraction) -> ExecuteInteraction.rotY)
                .documentation("Rotation on the Y axis to add.").add()
                .append(new KeyedCodec<>("RotationZ", Codec.FLOAT),
                        (ExecuteInteraction, o) -> ExecuteInteraction.rotZ = o,
                        (ExecuteInteraction) -> ExecuteInteraction.rotZ)
                .documentation("Rotation on the Z axis to add.").add()
                .build();
    }
}
