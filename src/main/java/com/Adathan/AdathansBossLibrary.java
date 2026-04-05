package com.Adathan;

import com.Adathan.interactions.*;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

public class AdathansBossLibrary extends JavaPlugin {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public AdathansBossLibrary(JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        this.getLogger().atInfo().log("[AdathansBossLibrary] Starting Plugin!");
        new HStats("HStats_UUID", this.getManifest().getVersion().toString());
        Interaction.CODEC.register("ExecuteInteraction", ExecuteInteraction.class, ExecuteInteraction.CODEC);
        Interaction.CODEC.register("ExecuteInteractionAtPlayer", ExecuteInteractionAtPlayer.class, ExecuteInteractionAtPlayer.CODEC);
        Interaction.CODEC.register("ExecuteInteractionAtSelf", ExecuteInteractionAtSelf.class, ExecuteInteractionAtSelf.CODEC);
        Interaction.CODEC.register("ExecuteInteractionAroundPointRandomly", ExecuteInteractionAroundPointRandomly.class, ExecuteInteractionAroundPointRandomly.CODEC);
        Interaction.CODEC.register("SetBlocksSquare", SetBlocksSquare.class, SetBlocksSquare.CODEC);
        Interaction.CODEC.register("SetBlocksAroundCircle", SetBlocksAroundCircle.class, SetBlocksAroundCircle.CODEC);
        Interaction.CODEC.register("RotateSelf", RotateSelf.class, RotateSelf.CODEC);
        Interaction.CODEC.register("FireProjectilesSetIntervals", FireProjectilesSetIntervals.class, FireProjectilesSetIntervals.CODEC);
        Interaction.CODEC.register("TeleportSelfRelative", TeleportSelfRelative.class, TeleportSelfRelative.CODEC);
        Interaction.CODEC.register("TeleportSelfStatic", TeleportSelfStatic.class, TeleportSelfStatic.CODEC);
    }
}
