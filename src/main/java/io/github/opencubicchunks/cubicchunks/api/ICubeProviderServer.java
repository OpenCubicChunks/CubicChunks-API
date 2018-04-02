package io.github.opencubicchunks.cubicchunks.api;

import io.github.opencubicchunks.cubicchunks.api.ICube;
import io.github.opencubicchunks.cubicchunks.api.ICubeProvider;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.world.chunk.Chunk;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface ICubeProviderServer extends ICubeProvider {

    /**
     * Retrieve a column. The work done to retrieve the column is specified by the {@link Requirement} <code>req</code>
     *
     * @param columnX Column x position
     * @param columnZ Column z position
     * @param req Work done to retrieve the column
     *
     * @return the column, or <code>null</code> if no column could be created with the specified requirement level
     */
    @Nullable
    Chunk getColumn(int columnX, int columnZ, Requirement req);

    /**
     * Retrieve a cube. The work done to retrieve the cube is specified by {@link Requirement} <code>req</code>
     *
     * @param cubeX the cube's x coordinate
     * @param cubeY the cube's y coordinate
     * @param cubeZ the cube's z coordinate
     * @param req what the requirments are before you get the Cube
     *
     * @return the Cube or null if no Cube could be found or created
     */
    @Nullable
    ICube getCube(int cubeX, int cubeY, int cubeZ, Requirement req);

    /**
     * The effort made to retrieve a cube or column. Any further work should not be done, and returning
     * <code>null</code> is acceptable in those cases
     */
    enum Requirement {
        // Warning, don't modify order of these constants - ordinals are used in comparisons
        // TODO write a custom compare method
        /**
         * Only retrieve the cube/column if it is already cached
         */
        GET_CACHED,
        /**
         * Load the cube/column from disk, if necessary
         */
        LOAD,
        /**
         * Generate the cube/column, if necessary
         */
        GENERATE,
        /**
         * Populate the cube/column, if necessary
         */
        POPULATE,
        /**
         * Generate lighting information for the cube, if necessary
         */
        LIGHT
    }
}
