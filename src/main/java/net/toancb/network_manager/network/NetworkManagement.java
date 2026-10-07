package net.toancb.network_manager.network;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.minecraftforge.forgespi.language.ModFileScanData;
import net.toancb.network_manager.NetworkManagerMod;
import org.objectweb.asm.Type;

import java.util.Map;

public final class NetworkManagement {
    private static int id = 0;
    private static final String PROTOCOL_VERSION = "1.0.0";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(NetworkManagerMod.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private NetworkManagement() {}

    @SuppressWarnings("unchecked")
    public static void register() {
        ModList.get().getAllScanData().forEach(scanData -> {
            scanData.getAnnotations().forEach(annotation -> {
                if (annotation.getAnnotationType().equals(Type.getType(AutoPacket.class))) {
                    try {
                        String className = annotation.getClassType().getClassName();
                        Class<? extends NetworkApply> packetClass = (Class<? extends NetworkApply>) Class.forName(className);
                        registerChannel(annotation, packetClass);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            });
        });
    }

    private static void registerChannel(ModFileScanData.AnnotationData annotation, Class<? extends NetworkApply> packetClass) {
        Map<String, Object> memberValues = annotation.getAnnotationData();
        if (memberValues != null && memberValues.containsKey("direction")) {
            Object rawDirection = memberValues.get("direction");
            if (rawDirection instanceof Object[]) {
                Object[] enumData = (Object[]) rawDirection;
                String enumName = (String) enumData[1];
                NetworkDirection direction = NetworkDirection.valueOf(enumName);
                registerPacket(packetClass, id++, direction);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void registerPacket(Class<?> packetClass, int id, NetworkDirection direction) {
        Class<NetworkApply> clazz = (Class<NetworkApply>) packetClass;
        CHANNEL.messageBuilder(clazz, id, direction)
                .encoder(NetworkApply::encode)
                .decoder(buf -> {
                    try {
                        return clazz.getConstructor(PacketBuffer.class).newInstance(buf);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to decode packet: " + clazz.getName(), e);
                    }
                })
                .consumer(NetworkApply::handlePacket)
                .add();
    }
}