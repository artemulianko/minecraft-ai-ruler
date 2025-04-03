package com.minecraftai.managermod.actions;

import net.minecraft.core.Vec3i;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;

public class SpawnCow extends AbstractAction {
    public Vec3i position;

    public SpawnCow(Vec3i position) {
        super(SpawnCow.class.getSimpleName());
        this.position = position;
    }

    @Override
    public void execute(MinecraftServer server) {
        // Get the server level (dimension)
        var dimension = server.getPlayerList().getPlayers().stream().findFirst().map(it -> it.level().dimension());
        if (dimension.isEmpty()) return;

        ServerLevel level = server.getLevel(dimension.get());
        if (level != null) {
            // Create and spawn the entity
            Cow cow = EntityType.COW.create(level, EntitySpawnReason.COMMAND);
            if (cow != null) {
                cow.setPos(this.position.getX(), this.position.getY(), this.position.getZ());
                level.addFreshEntity(cow);
            }
        }
    }
}
