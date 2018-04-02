package io.github.opencubicchunks.cubicchunks.api;

import io.github.opencubicchunks.cubicchunks.api.ICube;
import io.github.opencubicchunks.cubicchunks.api.util.XYZAddressable;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface ICubeWatcher extends XYZAddressable {

    boolean isSentToPlayers();

    @Nullable ICube getCube();

    void sendPacketToAllPlayers(IMessage packet);

    @Override int getX();

    @Override int getY();

    @Override int getZ();

    boolean shouldTick();
}
