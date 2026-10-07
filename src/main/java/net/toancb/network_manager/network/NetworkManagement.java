package net.toancb.network_manager.network;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.toancb.network_manager.NetworkManagerMod;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class NetworkManagement {
    private static final String PROTOCOL_VERSION = "1.0.5";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(NetworkManagerMod.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private NetworkManagement() {}

    @SafeVarargs
    public static <T extends NetworkApply> void register(Class<T>... classes) {
        int id = 0;
        for (Class<T> clazz : classes) {
            CHANNEL.messageBuilder(clazz, id++, NetworkDirection.PLAY_TO_SERVER)
                    .encoder(NetworkApply::encode)
                    .decoder(buf -> {
                        try {
                            return clazz.cast(clazz.getDeclaredConstructor(PacketBuffer.class).newInstance(buf));
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .consumer(NetworkApply::handlePacket)
                    .add();
        }
    }
}