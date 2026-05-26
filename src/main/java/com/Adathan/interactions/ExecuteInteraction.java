package com.Adathan.interactions;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionChain;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.InteractionManager;
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
import org.joml.Vector3d;

import javax.annotation.Nonnull;
import java.util.*;

public class ExecuteInteraction extends SimpleInstantInteraction {
    public static final BuilderCodec CODEC;

    protected String rootInteractionName;
    protected String entityName;
    protected Double range = (double) 10.0F;
    protected Double rangeSquared;
    protected int count = 0; // how many entities shall be called
    protected boolean random; // shall the entities picked be random?
    protected boolean nearestToTarget; // position in relation to the target
    

    @Override
    protected void firstRun(@Nonnull InteractionType interactionType, @Nonnull InteractionContext interactionContext, @Nonnull CooldownHandler cooldownHandler) {
        CommandBuffer<EntityStore> commandBuffer = interactionContext.getCommandBuffer();
        World world = interactionContext.getEntity().getStore().getExternalData().getWorld();
        Ref<EntityStore> entityRef = interactionContext.getOwningEntity();
        if (nearestToTarget) {
            Ref<EntityStore> targetRef = interactionContext.getTargetEntity();
            if (targetRef != null) {
                entityRef = targetRef;
            }
        }
        Vector3d position = (commandBuffer.getComponent(entityRef, TransformComponent.getComponentType())).getPosition();
        SpatialResource<Ref<EntityStore>, EntityStore> spatialResource = commandBuffer.getResource(NPCPlugin.get().getNpcSpatialResource());
        ObjectArrayList<Ref<EntityStore>> npcs = new ObjectArrayList();
        spatialResource.getSpatialStructure().collect(position, (this.range + 1), npcs);

        RootInteraction ri = RootInteraction.getAssetMap().getAsset(rootInteractionName);
        Store<EntityStore> store = world.getEntityStore().getStore();

        if (count == 0) { // interact on all the NPCs
            for (int i = 0; i < npcs.size(); ++i) {
                Ref<EntityStore> npcRef = this.filterNPCs(npcs.get(i), position, commandBuffer);
                if (npcRef != null) {
                    InteractionManager im = store.getComponent(npcRef, InteractionModule.get().getInteractionManagerComponent());
                    InteractionContext ctx = InteractionContext.forInteraction(im, npcRef, InteractionType.Primary, store);
                    InteractionChain cn = im.initChain(InteractionType.Primary, ctx, ri, false);
                    commandBuffer.run((_) -> im.queueExecuteChain(cn));
                }
            }
        } else { // grab the count closest NPCs and run the interactions on them
            Map<Double, Ref<EntityStore>> sortedNPCs = npcsSortedByDistance(npcs, position, commandBuffer);
            int totalLoops = count;
            if (sortedNPCs == null) {
                return;
            } else if (sortedNPCs.size() <= count) {
                totalLoops = sortedNPCs.size();
            }
            Iterator<Map.Entry<Double, Ref<EntityStore>>> iterator = sortedNPCs.entrySet().iterator();
            List<Ref<EntityStore>> sortedNPCArrayList = new ArrayList<>(sortedNPCs.values());

            Random randomGenerator = new Random();
            for (int i = 0; i < totalLoops; ++i) {
                Ref<EntityStore> npcRef;
                if (random) {
                    int index = randomGenerator.nextInt(sortedNPCArrayList.size());
                    npcRef = sortedNPCArrayList.get(index);
                    sortedNPCArrayList.remove(index);
                } else {
                    npcRef = iterator.next().getValue();
                }

                InteractionManager im = store.getComponent(npcRef, InteractionModule.get().getInteractionManagerComponent());
                InteractionContext ctx = InteractionContext.forInteraction(im, npcRef, InteractionType.Primary, store);
                InteractionChain cn = im.initChain(InteractionType.Primary, ctx, ri, false);
                commandBuffer.run((_) -> im.queueExecuteChain(cn));
            }
        }
    }

    protected Ref<EntityStore> filterNPCs(@Nonnull Ref<EntityStore> targetRef, @Nonnull Vector3d position, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        if (!targetRef.isValid()) {
            return null;
        } else {
            TransformComponent targetTransformComponent = (TransformComponent)commandBuffer.getComponent(targetRef, TransformComponent.getComponentType());

            assert targetTransformComponent != null;

            Vector3d targetPosition = targetTransformComponent.getPosition();

            NPCEntity npcEntityComponent = commandBuffer.getComponent(targetRef, NPCEntity.getComponentType());
            assert npcEntityComponent != null;

            return npcEntityComponent == null || !(position.distanceSquared(targetPosition) <= this.rangeSquared) || !npcEntityComponent.getRoleName().toLowerCase().strip().equals(entityName.toLowerCase().strip()) ? null : targetRef;
        }
    }

    protected Map<Double, Ref<EntityStore>> npcsSortedByDistance(@Nonnull ObjectArrayList<Ref<EntityStore>> npcRefs, @Nonnull Vector3d position, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        Map<Double, Ref<EntityStore>> unsortedMap = new HashMap<>();
        for (int i = 0; i < npcRefs.size(); ++i) {
            Ref<EntityStore> npcRef = npcRefs.get(i);
            if (npcRef.isValid()) {
                TransformComponent targetTransformComponent = commandBuffer.getComponent(npcRef, TransformComponent.getComponentType());

                assert targetTransformComponent != null;

                Vector3d targetPosition = targetTransformComponent.getPosition();

                NPCEntity npcEntityComponent = commandBuffer.getComponent(npcRef, NPCEntity.getComponentType());
                assert npcEntityComponent != null;

                if (npcEntityComponent.getRoleName().toLowerCase().strip().equals(entityName.toLowerCase().strip())) {
                    double distanceSquared = position.distanceSquared(targetPosition);
                    unsortedMap.put(distanceSquared, npcRef);
                }
            }
        }
        if (unsortedMap.size() == 0) {
            return null;
        }
        Map<Double, Ref<EntityStore>> sortedMap = new TreeMap<>(unsortedMap);

        return sortedMap;
    }

    protected void simulateFirstRun(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
    }

    static {
        CODEC = BuilderCodec.builder(ExecuteInteraction.class, ExecuteInteraction::new, SimpleInstantInteraction.CODEC)
                .documentation("Executes an interaction at a given NPC or location")
                .append(new KeyedCodec<>("RootInteractionName", Codec.STRING),
                        (ExecuteInteraction, o) -> ExecuteInteraction.rootInteractionName =(String) o,
                        (ExecuteInteraction) -> ExecuteInteraction.rootInteractionName)
                .documentation("Root Interaction to execute").addValidator(Validators.nonNull()).add()
                .append(new KeyedCodec<>("EntityName", Codec.STRING),
                        (ExecuteInteraction, o) -> ExecuteInteraction.entityName =(String) o,
                        (ExecuteInteraction) -> ExecuteInteraction.entityName)
                .documentation("NPC/Entity Name to execute the interaction on").addValidator(Validators.nonNull()).add()
                .append(new KeyedCodec<>("Range", Codec.DOUBLE),
                        (ExecuteInteraction, o) -> ExecuteInteraction.range = o,
                        (ExecuteInteraction) -> ExecuteInteraction.range)
                .documentation("range to execute interaction on desired NPCs").add()
                .append(new KeyedCodec<>("Count", Codec.INTEGER),
                        (ExecuteInteraction, o) -> ExecuteInteraction.count = o,
                        (ExecuteInteraction) -> ExecuteInteraction.count)
                .documentation("count of NPCs to run the interaction on").add()
                .append(new KeyedCodec<>("Random", Codec.BOOLEAN),
                        (ExecuteInteraction, o) -> ExecuteInteraction.random = o,
                        (ExecuteInteraction) -> ExecuteInteraction.random)
                .documentation("run interactions on random NPCs of same name within range").add()
                .append(new KeyedCodec<>("NearestToTarget", Codec.BOOLEAN),
                        (ExecuteInteraction, o) -> ExecuteInteraction.nearestToTarget = o,
                        (ExecuteInteraction) -> ExecuteInteraction.nearestToTarget)
                .documentation("run interactions on npcs nearest to target rather than owning entity").add()
                .afterDecode((i) -> i.rangeSquared = i.range * i.range).build();
    }
}
