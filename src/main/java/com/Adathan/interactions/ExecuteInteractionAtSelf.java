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
import com.hypixel.hytale.server.core.entity.Frozen;
import com.hypixel.hytale.server.core.entity.InteractionChain;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.InteractionManager;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.interaction.InteractionModule;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.npc.INonPlayerCharacter;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import it.unimi.dsi.fastutil.Pair;

import javax.annotation.Nonnull;

public class ExecuteInteractionAtSelf extends SimpleInstantInteraction {
    public static final BuilderCodec CODEC;

    protected String rootInteractionName;
    protected String entityNameToSpawn;
    protected Double relativeX = 0.0;
    protected Double relativeY = 0.0;
    protected Double relativeZ = 0.0;
    protected float staticXRot = 0.0f;
    protected float staticYRot = 0.0f;
    protected float staticZRot = 0.0f;

    @Override
    protected void firstRun(@Nonnull InteractionType interactionType, @Nonnull InteractionContext interactionContext, @Nonnull CooldownHandler cooldownHandler) {
        CommandBuffer<EntityStore> commandBuffer = interactionContext.getCommandBuffer();
        World world = interactionContext.getEntity().getStore().getExternalData().getWorld();
//        Ref<EntityStore> entityRef = interactionContext.getOwningEntity();
        Ref<EntityStore> entityRef = interactionContext.getEntity();
        Store<EntityStore> store = world.getEntityStore().getStore();

        Vector3d position = (commandBuffer.getComponent(entityRef, TransformComponent.getComponentType())).getPosition();
        Vector3f rotation = (commandBuffer.getComponent(entityRef, TransformComponent.getComponentType())).getRotation();

        if (staticXRot != 0 || staticYRot != 0 || staticZRot != 0) {
            rotation = new Vector3f((float) Math.toRadians(staticXRot), (float) Math.toRadians(staticYRot), (float) Math.toRadians(staticZRot));
        }

        Vector3d finalPosition = new Vector3d(position.getX() + relativeX, position.getY() + relativeY, position.getZ() + relativeZ);

        Vector3f finalRotation = rotation;

        world.execute(() -> {
            Ref<EntityStore> npcRef = null;
            try {
                Pair<Ref<EntityStore>, INonPlayerCharacter> result = NPCPlugin.get().spawnNPC(store, entityNameToSpawn, null, finalPosition, finalRotation);
                npcRef = result.first();
                store.ensureComponent(npcRef, Frozen.getComponentType());

            } catch (Exception e) {
                HytaleLogger.getLogger().atWarning().log("[AdathansBossLibrary] Failed to spawn Mob %s on entity %s: %s", entityNameToSpawn, entityRef.toString(), e.getMessage());
            }
            if (npcRef != null) {
                InteractionManager im = store.getComponent(npcRef, InteractionModule.get().getInteractionManagerComponent());
                RootInteraction ri = RootInteraction.getAssetMap().getAsset(rootInteractionName);
                InteractionContext ctx = InteractionContext.forInteraction(im, npcRef, InteractionType.Primary, store);
                InteractionChain cn = im.initChain(InteractionType.Primary, ctx, ri, false);
                commandBuffer.run((_) -> im.queueExecuteChain(cn));
            } else {
                HytaleLogger.getLogger().atWarning().log("[AdathansBossLibrary] Failed to trigger interaction. NPC Ref is null!");
            }
        });
    }

    protected void simulateFirstRun(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
    }

    static {
        CODEC = BuilderCodec.builder(ExecuteInteractionAtSelf.class, ExecuteInteractionAtSelf::new, SimpleInstantInteraction.CODEC)
                .documentation("Executes an interaction at a given player")
                .append(new KeyedCodec<>("RootInteractionName", Codec.STRING),
                        (ExecuteInteraction, o) -> ExecuteInteraction.rootInteractionName =(String) o,
                        (ExecuteInteraction) -> ExecuteInteraction.rootInteractionName)
                .documentation("Root Interaction to execute").addValidator(Validators.nonNull()).add()
                .append(new KeyedCodec<>("EntityNameToSpawn", Codec.STRING),
                        (ExecuteInteraction, o) -> ExecuteInteraction.entityNameToSpawn =(String) o,
                        (ExecuteInteraction) -> ExecuteInteraction.entityNameToSpawn)
                .documentation("NPC/Entity Name to execute the interaction on").addValidator(Validators.nonNull()).add()
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
                .append(new KeyedCodec<>("StaticXRot", Codec.FLOAT),
                        (ExecuteInteraction, o) -> ExecuteInteraction.staticXRot =(Float) o,
                        (ExecuteInteraction) -> ExecuteInteraction.staticXRot)
                .documentation("Static X Rotation (Deg) (IF ANY STATIC ROT IS CHANGED IT WILL OVERRIDE PARENT ROT").addValidator(Validators.nonNull()).add()
                .append(new KeyedCodec<>("StaticYRot", Codec.FLOAT),
                        (ExecuteInteraction, o) -> ExecuteInteraction.staticYRot =(Float) o,
                        (ExecuteInteraction) -> ExecuteInteraction.staticYRot)
                .documentation("Static Y Rotation (Deg) (IF ANY STATIC ROT IS CHANGED IT WILL OVERRIDE PARENT ROT").addValidator(Validators.nonNull()).add()
                .append(new KeyedCodec<>("StaticZRot", Codec.FLOAT),
                        (ExecuteInteraction, o) -> ExecuteInteraction.staticZRot =(Float) o,
                        (ExecuteInteraction) -> ExecuteInteraction.staticZRot)
                .documentation("Static Z Rotation (Deg) (IF ANY STATIC ROT IS CHANGED IT WILL OVERRIDE PARENT ROT").addValidator(Validators.nonNull()).add()
                .build();
    }
}
