package io.github.opencubicchunks.cubicchunks.api.worldgen.populator;

import io.github.opencubicchunks.cubicchunks.api.ICube;
import io.github.opencubicchunks.cubicchunks.api.ICubicWorld;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldEntitySpawner;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;

import java.util.List;
import java.util.Random;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WorldGenEntitySpawner {

    public static void initialWorldGenSpawn(WorldServer world, Biome biome, int blockX, int blockY, int blockZ,
            int sizeX, int sizeY, int sizeZ, Random random) {
        List<Biome.SpawnListEntry> spawnList = biome.getSpawnableList(EnumCreatureType.CREATURE);

        if (spawnList.isEmpty()) {
            return;
        }
        while (random.nextFloat() < biome.getSpawningChance()) {
            Biome.SpawnListEntry currEntry = WeightedRandom.getRandomItem(world.rand, spawnList);
            int groupCount = MathHelper.getInt(random, currEntry.minGroupCount, currEntry.maxGroupCount);
            IEntityLivingData data = null;
            int randX = blockX + random.nextInt(sizeX);
            int randZ = blockZ + random.nextInt(sizeZ);

            final int initRandX = randX;
            final int initRandZ = randZ;

            for (int i = 0; i < groupCount; ++i) {
                for (int j = 0; j < 4; ++j) {
                    do {
                        randX = initRandX + random.nextInt(5) - random.nextInt(5);
                        randZ = initRandZ + random.nextInt(5) - random.nextInt(5);
                    } while (randX < blockX || randX >= blockX + sizeX || randZ < blockZ || randZ >= blockZ + sizeZ);

                    BlockPos pos = ((ICubicWorld)world).findTopBlock(new BlockPos(randX, blockY + sizeY + ICube.SIZE / 2, randZ),
                            blockY, blockY + sizeY - 1, ICubicWorld.SurfaceType.SOLID);
                    if (pos == null) {
                        continue;
                    }

                    if (WorldEntitySpawner.canCreatureTypeSpawnAtLocation(EntityLiving.SpawnPlacementType.ON_GROUND, (World) world, pos)) {
                        EntityLiving spawnedEntity;

                        try {
                            spawnedEntity = currEntry.newInstance((World) world);
                        } catch (Exception exception) {
                            exception.printStackTrace();
                            continue;
                        }

                        spawnedEntity.setLocationAndAngles(randX + 0.5, pos.getY(), randZ + 0.5, random.nextFloat() * 360.0F, 0.0F);
                        world.spawnEntity(spawnedEntity);
                        data = spawnedEntity.onInitialSpawn(world.getDifficultyForLocation(new BlockPos(spawnedEntity)), data);
                        break;
                    }
                }
            }
        }
    }
}
