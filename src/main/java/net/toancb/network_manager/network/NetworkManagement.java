package net.toancb.network_manager.network;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.toancb.network_manager.NetworkManagerMod;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class NetworkManagement {
    private static final String PROTOCOL_VERSION = "1.0.6";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(NetworkManagerMod.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private NetworkManagement() {}

    @SafeVarargs
    public static void register(Class<? extends NetworkApply>... classes) {
        int id = 0;
        for (Class<? extends NetworkApply> clazz : classes) {
            registerPacket(clazz, id++);
        }
    }

    private static <T extends NetworkApply> void registerPacket(Class<T> clazz, int id) {
        if (clazz.isAnnotationPresent(AutoPacket.class)) {
            AutoPacket annotation = clazz.getAnnotation(AutoPacket.class);
            CHANNEL.messageBuilder(clazz, id, annotation.direction())
                    .encoder(NetworkApply::encode)
                    .decoder(buf -> {
                        try {
                            return clazz.getDeclaredConstructor(PacketBuffer.class).newInstance(buf);
                        } catch (Exception e) {
                            throw new RuntimeException("Could not instantiate decoder for packet: " + clazz.getName(), e);
                        }
                    })
                    .consumer(NetworkApply::handlePacket)
                    .add();
        }
    }
}