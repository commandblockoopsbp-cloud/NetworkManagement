package net.toancb.network_manager.network;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public abstract class NetworkApply {

    public NetworkApply() {}

    protected NetworkApply(PacketBuffer buffer) {}

    public abstract void encode(PacketBuffer buffer);

    public abstract  <T extends NetworkApply> T decode(PacketBuffer buffer);

    protected abstract void handle(NetworkEvent.Context context);

    public NetworkDirection getDirection() {
        return NetworkDirection.PLAY_TO_SERVER;
    }

    public void handlePacket(Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> this.handle(context));
        context.setPacketHandled(true);
    }
}
