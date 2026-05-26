//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.Adathan.interactions;

import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.builtin.triggervolumes.TriggerVolumesPlugin;
import com.hypixel.hytale.builtin.triggervolumes.effect.builtin.TaggedVolumeEffectUtil;
import com.hypixel.hytale.builtin.triggervolumes.manager.TriggerVolumeManager;
import com.hypixel.hytale.builtin.triggervolumes.manager.VolumeEntry;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import org.joml.Vector3d;
import org.joml.Vector4d;

public class ModifyVolumesTagInteraction extends SimpleInstantInteraction {
    @Nonnull
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    @Nonnull
    public static final BuilderCodec<ModifyVolumesTagInteraction> CODEC;
    private String matchKey;
    private String matchValue;
    private String setValue;
    private double radius = 50.0F;

    protected void firstRun(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
        Vector4d hitLocation = context.getMetaStore().getIfPresentMetaObject(Interaction.HIT_LOCATION);
        Vector3d center;
        Ref<EntityStore> owningRef = context.getOwningEntity();
        if (hitLocation != null) {
            center = new Vector3d(hitLocation.x(), hitLocation.y(), hitLocation.z());
        } else {
            TransformComponent transform = owningRef.getStore().getComponent(owningRef, TransformComponent.getComponentType());
            if (transform == null) {
                return;
            }

            center = new Vector3d(transform.getPosition());
        }


        String tagFilter = TaggedVolumeEffectUtil.composeTagFilter(this.matchKey, this.matchValue);
        if (tagFilter != null) {
            int tagIndex = AssetRegistry.getTagIndex(tagFilter);
            if (tagIndex == Integer.MIN_VALUE) {
                LOGGER.at(Level.WARNING).log("ModifyVolumesTag: unknown tag '%s'", tagFilter);
            } else {
                TriggerVolumesPlugin plugin = TriggerVolumesPlugin.get();
                Store<EntityStore> store = owningRef.getStore();
                TriggerVolumeManager manager = store.getResource(plugin.getManagerResourceType());
                if (manager != null) {
                    double radiusSq = this.radius * this.radius;
                    ArrayList<VolumeEntry> toModify = new ArrayList();

                    for(VolumeEntry entry : manager.getVolumesByTag(tagIndex)) {
                        if (this.radius <= (double)0.0F || entry.getPosition().distanceSquared(center) <= radiusSq) {
                            toModify.add(entry);
                        }
                    }

                    for(VolumeEntry entry : toModify) {
                        manager.setTag(entry.getId(), matchKey, setValue, owningRef, UUID.randomUUID());
                    }

                }
            }
        }
    }

    @Nonnull
    public String toString() {
        String var10000 = this.matchKey;
        return "ModifyVolumesTagInteraction{matchKey=" + var10000 + ", matchValue=" + this.matchValue + ", setValue=" + this.setValue + ", radius=" + this.radius + "} " + super.toString();
    }

    static {
        CODEC = BuilderCodec.builder(ModifyVolumesTagInteraction.class, ModifyVolumesTagInteraction::new, SimpleInstantInteraction.CODEC)
                .documentation("Modify a volumes tag.")
                .append(new KeyedCodec<>("MatchKey", Codec.STRING),
                        (interaction, o) -> interaction.matchKey =(String) o,
                        (interaction) -> interaction.matchKey).add()
                .append(new KeyedCodec<>("MatchValue", Codec.STRING),
                        (interaction, o) -> interaction.matchValue =(String) o,
                        (interaction) -> interaction.matchValue).add()
                .append(new KeyedCodec<>("SetValue", Codec.STRING),
                        (interaction, o) -> interaction.setValue =(String) o,
                        (interaction) -> interaction.setValue).add()
                .appendInherited(new KeyedCodec("Radius", Codec.DOUBLE, false),
                        (interaction, radius) -> interaction.radius = radius,
                        (interaction) -> interaction.radius,
                        (interaction, parent) -> interaction.radius = parent.radius).add()
                .build();
    }
}
