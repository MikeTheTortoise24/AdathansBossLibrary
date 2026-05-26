package com.Adathan.interactions;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.logger.HytaleLogger;
import org.joml.Vector3d;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.world.World;

import javax.annotation.Nonnull;

public class SetBlocksAroundCircle extends SimpleInstantInteraction {
    public static final BuilderCodec CODEC;

    protected Integer radius = 10;
    protected Integer yHeight = 0;
    protected Integer numberOfBlocks = 2;
    protected String blockName = "Empty";
    protected Double delay = 0.0;
    protected Double anchorX = 0.0;
    protected Double anchorY = 0.0;
    protected Double anchorZ = 0.0;


    @Override
    protected void firstRun(@Nonnull InteractionType interactionType, @Nonnull InteractionContext interactionContext, @Nonnull CooldownHandler cooldownHandler) {
        World world = interactionContext.getEntity().getStore().getExternalData().getWorld();
        // TODO: figure out delay via ticket system if needed
        Vector3d position = new Vector3d(anchorX, anchorY, anchorZ);

        for (int i = 0; i <= numberOfBlocks; ++i) {
            double angle = 2 * Math.PI * i / numberOfBlocks;
            Vector3d blockPosition = new Vector3d(position.x + radius * Math.cos(angle), position.y + yHeight, position.z + + radius * Math.sin(angle));
            try {
                world.setBlock((int) Math.floor(blockPosition.x), (int) Math.floor(blockPosition.y), (int) Math.floor(blockPosition.z), blockName);
            } catch (Exception e) {
                HytaleLogger.getLogger().atWarning().log("[AdathansBossLibrary] Failed to set block %s: %s", blockName, e.getMessage());
            }
        }
    }

    protected void simulateFirstRun(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
    }

    static {
        CODEC = BuilderCodec.builder(SetBlocksAroundCircle.class, SetBlocksAroundCircle::new, SimpleInstantInteraction.CODEC)
                .documentation("Sets blocks around anchor position evenly along the radius. IE radius of 10 and numOfBlocks set to 2 will place one 10 blocks in front and 10 blocks behind anchor position.")
                .append(new KeyedCodec<>("BlockName", Codec.STRING),
                        (ExecuteInteraction, o) -> ExecuteInteraction.blockName = o,
                        (ExecuteInteraction) -> ExecuteInteraction.blockName)
                .documentation("Block to replace selection with").add()
                .append(new KeyedCodec<>("Radius", Codec.INTEGER),
                        (ExecuteInteraction, o) -> ExecuteInteraction.radius = o,
                        (ExecuteInteraction) -> ExecuteInteraction.radius)
                .documentation("Radius to set blocks to").add()
                .append(new KeyedCodec<>("NumberOfBlocks", Codec.INTEGER),
                        (ExecuteInteraction, o) -> ExecuteInteraction.numberOfBlocks = o,
                        (ExecuteInteraction) -> ExecuteInteraction.numberOfBlocks)
                .documentation("Number of blocks around the radius to set evenly").add()
                .append(new KeyedCodec<>("Delay", Codec.DOUBLE),
                        (ExecuteInteraction, o) -> ExecuteInteraction.delay = o,
                        (ExecuteInteraction) -> ExecuteInteraction.delay)
                .documentation("DOES NOT WORK (Not sure if I will ever add)").add()
                .append(new KeyedCodec<>("YHeight", Codec.INTEGER),
                        (ExecuteInteraction, o) -> ExecuteInteraction.yHeight = o,
                        (ExecuteInteraction) -> ExecuteInteraction.yHeight)
                .documentation("Y height to set blocks").add()
                .append(new KeyedCodec<>("AnchorX", Codec.DOUBLE),
                        (ExecuteInteraction, o) -> ExecuteInteraction.anchorX = o,
                        (ExecuteInteraction) -> ExecuteInteraction.anchorX)
                .documentation("Anchor X").add()
                .append(new KeyedCodec<>("AnchorY", Codec.DOUBLE),
                        (ExecuteInteraction, o) -> ExecuteInteraction.anchorY = o,
                        (ExecuteInteraction) -> ExecuteInteraction.anchorY)
                .documentation("Anchor Y").add()
                .append(new KeyedCodec<>("AnchorZ", Codec.DOUBLE),
                        (ExecuteInteraction, o) -> ExecuteInteraction.anchorZ = o,
                        (ExecuteInteraction) -> ExecuteInteraction.anchorZ)
                .documentation("Anchor Z").add()
                .build();
    }
}
