package net.toancb.network_manager.network;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import org.apache.logging.log4j.LogManager;

import java.util.function.Supplier;

public abstract class NetworkApply {

    public NetworkApply() {}
    public NetworkApply(PacketBuffer buf) {}

    public abstract void encode(PacketBuffer buffer);

    public abstract void handle(NetworkEvent.Context context);

    public final void handlePacket(Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            this.handle(context);
        });
        context.setPacketHandled(true);
    }
}
