package net.toancb.network_manager.network;

import net.minecraftforge.fml.network.NetworkDirection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AutoPacket {
    NetworkDirection direction() default NetworkDirection.PLAY_TO_SERVER;
}
