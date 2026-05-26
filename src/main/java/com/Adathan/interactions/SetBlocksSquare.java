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
import org.joml.Vector3d;
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

import javax.annotation.Nonnull;

public class SetBlocksSquare extends SimpleInstantInteraction {
    public static final BuilderCodec CODEC;

    protected Integer squareSize;
    protected Integer yHeight = 0;
    protected String blockName = "Empty";


    @Override
    protected void firstRun(@Nonnull InteractionType interactionType, @Nonnull InteractionContext interactionContext, @Nonnull CooldownHandler cooldownHandler) {
        CommandBuffer<EntityStore> commandBuffer = interactionContext.getCommandBuffer();
        World world = interactionContext.getEntity().getStore().getExternalData().getWorld();
        Ref<EntityStore> entityRef = interactionContext.getOwningEntity();

        Vector3d position = (commandBuffer.getComponent(entityRef, TransformComponent.getComponentType())).getPosition();


        for (int i = -squareSize; i <= squareSize; ++i) {
            for (int j = -squareSize; j <= squareSize; ++j) {
                Vector3d blockPosition = new Vector3d(position.x + i, position.y + yHeight, position.z + j);
                try {
                    world.setBlock((int) Math.floor(blockPosition.x), (int) Math.floor(blockPosition.y), (int) Math.floor(blockPosition.z), blockName);
                } catch (Exception e) {
                    HytaleLogger.getLogger().atWarning().log("[AdathansBossLibrary] Failed to set block %s: %s", blockName, e.getMessage());
                }
            }
        }
    }

    protected void simulateFirstRun(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
    }

    static {
        CODEC = BuilderCodec.builder(SetBlocksSquare.class, SetBlocksSquare::new, SimpleInstantInteraction.CODEC)
                .documentation("Sets blocks in a square of size SquareSize around the entity, including where the entity is")
                .append(new KeyedCodec<>("BlockName", Codec.STRING),
                        (ExecuteInteraction, o) -> ExecuteInteraction.blockName = o,
                        (ExecuteInteraction) -> ExecuteInteraction.blockName)
                .documentation("Block to set selection with").add()
                .append(new KeyedCodec<>("SquareSize", Codec.INTEGER),
                        (ExecuteInteraction, o) -> ExecuteInteraction.squareSize = o,
                        (ExecuteInteraction) -> ExecuteInteraction.squareSize)
                .documentation("range to set blocks").add()
                .append(new KeyedCodec<>("YHeight", Codec.INTEGER),
                        (ExecuteInteraction, o) -> ExecuteInteraction.yHeight = o,
                        (ExecuteInteraction) -> ExecuteInteraction.yHeight)
                .documentation("Y height to set blocks").add()
                .build();
    }
}
