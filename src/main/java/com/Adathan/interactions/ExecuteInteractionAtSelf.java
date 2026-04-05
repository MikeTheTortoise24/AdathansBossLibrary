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
import com.hypixel.hytale.server.core.command.commands.world.entity.stats.EntityStatsSetCommand;
import com.hypixel.hytale.server.core.entity.Frozen;
import com.hypixel.hytale.server.core.entity.InteractionChain;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.InteractionManager;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
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

    @Override
    protected void firstRun(@Nonnull InteractionType interactionType, @Nonnull InteractionContext interactionContext, @Nonnull CooldownHandler cooldownHandler) {
        CommandBuffer<EntityStore> commandBuffer = interactionContext.getCommandBuffer();
        World world = interactionContext.getEntity().getStore().getExternalData().getWorld();
        Ref<EntityStore> entityRef = interactionContext.getOwningEntity();
        Store<EntityStore> store = world.getEntityStore().getStore();

        Vector3d position = (commandBuffer.getComponent(entityRef, TransformComponent.getComponentType())).getPosition();
        Vector3f rotation = (commandBuffer.getComponent(entityRef, TransformComponent.getComponentType())).getRotation();


        world.execute(() -> {
            Ref<EntityStore> npcRef = null;
            try {
                Pair<Ref<EntityStore>, INonPlayerCharacter> result = NPCPlugin.get().spawnNPC(store, entityNameToSpawn, null, position, rotation);
                npcRef = result.first();
                store.ensureComponent(npcRef, Frozen.getComponentType());

            } catch (Exception e) {
                HytaleLogger.getLogger().atWarning().log("[AdathansBossLibrary] Failed to spawn Mob %s on entity %s: %s", entityNameToSpawn, entityRef.toString(), e.getMessage());
            }
            if (npcRef != null) {
                HytaleLogger.getLogger().atInfo().log("[AdathansBossLibrary] Triggered Interaction On: %s at (%.1f, %.1f, %.1f) for entity %s", entityNameToSpawn, position.x, position.y, position.z, entityRef.toString());

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
                .build();
    }
}
