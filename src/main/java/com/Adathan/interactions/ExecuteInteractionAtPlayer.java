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
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.interaction.InteractionModule;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.npc.INonPlayerCharacter;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import it.unimi.dsi.fastutil.Pair;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExecuteInteractionAtPlayer extends SimpleInstantInteraction {
    public static final BuilderCodec CODEC;

    protected String rootInteractionName;
    protected String entityNameToSpawn;
    protected Integer players = 1;
    protected Double range = (double)10.0F; // TODO: Add range usage
    protected Vector3d globalLocation;

    @Override
    protected void firstRun(@Nonnull InteractionType interactionType, @Nonnull InteractionContext interactionContext, @Nonnull CooldownHandler cooldownHandler) {
        CommandBuffer<EntityStore> commandBuffer = interactionContext.getCommandBuffer();
        World world = interactionContext.getEntity().getStore().getExternalData().getWorld();
//        Ref<EntityStore> entityRef = interactionContext.getOwningEntity();
        Store<EntityStore> store = world.getEntityStore().getStore();
        int currentPlayersExecuted = 0;

        List<PlayerRef> playerRefs = new ArrayList<>(world.getPlayerRefs().stream().toList());
        Collections.shuffle(playerRefs);

        for (PlayerRef playerRef : playerRefs) {
            if (currentPlayersExecuted >= players) {
                return;
            }
            Ref<EntityStore> playerRefEntityStore = world.getEntityRef(playerRef.getUuid());
            Player playerComponent = playerRefEntityStore.getStore().getComponent(playerRefEntityStore, Player.getComponentType());
            if (playerComponent.getGameMode().getValue() != 0) { // 0 corresponds to adventure game mode, don't want it to trigger for creative mode players
                continue;
            }

            Vector3d position = (commandBuffer.getComponent(playerRefEntityStore, TransformComponent.getComponentType())).getPosition();

            world.execute(() -> {
                Ref<EntityStore> npcRef = null;
                try {
                    Pair<Ref<EntityStore>, INonPlayerCharacter> result = NPCPlugin.get().spawnNPC(store, entityNameToSpawn, null, position, Vector3f.ZERO);
                    npcRef = result.first();
                    store.ensureComponent(npcRef, Frozen.getComponentType());

                } catch (Exception e) {
                    HytaleLogger.getLogger().atWarning().log("[AdathansBossLibrary] Failed to spawn Mob %s on player %s: %s", entityNameToSpawn, playerRef.getUsername(), e.getMessage());
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
            currentPlayersExecuted += 1;
        }
    }

    protected void simulateFirstRun(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
    }

    static {
        CODEC = BuilderCodec.builder(ExecuteInteractionAtPlayer.class, ExecuteInteractionAtPlayer::new, SimpleInstantInteraction.CODEC)
                .documentation("Executes an interaction at a given player")
                .append(new KeyedCodec<>("RootInteractionName", Codec.STRING),
                        (ExecuteInteraction, o) -> ExecuteInteraction.rootInteractionName =(String) o,
                        (ExecuteInteraction) -> ExecuteInteraction.rootInteractionName)
                .documentation("Root Interaction to execute").addValidator(Validators.nonNull()).add()
                .append(new KeyedCodec<>("EntityNameToSpawn", Codec.STRING),
                        (ExecuteInteraction, o) -> ExecuteInteraction.entityNameToSpawn =(String) o,
                        (ExecuteInteraction) -> ExecuteInteraction.entityNameToSpawn)
                .documentation("NPC/Entity Name to execute the interaction on").addValidator(Validators.nonNull()).add()
                .append(new KeyedCodec<>("Players", Codec.INTEGER),
                        (ExecuteInteraction, o) -> ExecuteInteraction.players = o,
                        (ExecuteInteraction) -> ExecuteInteraction.players)
                .documentation("number of players to execute interaction at").add()
                .append(new KeyedCodec<>("Range", Codec.DOUBLE),
                        (ExecuteInteraction, o) -> ExecuteInteraction.range = o,
                        (ExecuteInteraction) -> ExecuteInteraction.range)
                .documentation("range to execute interaction on desired NPCs").add()

                .build();
    }
}
