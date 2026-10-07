package net.toancb.network_manager.network;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.toancb.network_manager.NetworkManagerMod;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class NetworkManagement {
    private static final String PROTOCOL_VERSION = "1.0.7";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(NetworkManagerMod.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private NetworkManagement() {}

    @SuppressWarnings("unchecked")
    public static void register() {
        final int[] id = {0};
        ModList.get().getAllScanData().forEach(scanData -> scanData.getAnnotations().forEach(annotationData -> {
            String className = annotationData.getClassType().getClassName();
            try {
                Class<?> clazz = Class.forName(className);
                if (NetworkApply.class.isAssignableFrom(clazz)) {
                    Class<? extends NetworkApply> applyClass = (Class<? extends NetworkApply>) clazz;
                    registerPacket(applyClass, id[0]++);
                }
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }));
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