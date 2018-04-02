package io.github.opencubicchunks.cubicchunks.api;

import io.github.opencubicchunks.cubicchunks.api.util.CubePos;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.world.chunk.Chunk;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface ICubeProvider {

    @Nullable
    ICube getLoadedCube(int cubeX, int cubeY, int cubeZ);

    @Nullable
    ICube getLoadedCube(CubePos coords);

    ICube getCube(int cubeX, int cubeY, int cubeZ);

    ICube getCube(CubePos coords);

    /**
     * Retrieve a column, if it exists and is loaded
     *
     * @param x The x position of the column
     * @param z The z position of the column
     *
     * @return The column, if loaded. Null, otherwise.
     */
    // TODO remove, use vanilla methods
    @Nullable
    Chunk getLoadedColumn(int x, int z); // more strictly define the return type

    Chunk provideColumn(int x, int z);   // more strictly define the return type
}
