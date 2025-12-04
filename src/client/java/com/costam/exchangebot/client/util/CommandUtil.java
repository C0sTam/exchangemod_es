package com.costam.exchangebot.client.util;

import java.lang.reflect.Field;

public class CommandUtil {
    public static boolean isDispatcherReady() {
        try {
            Class<?> internalsClass = Class.forName("net.fabricmc.fabric.impl.command.client.ClientCommandInternals");
            Field field = internalsClass.getDeclaredField("activeDispatcher");
            field.setAccessible(true);
            Object dispatcher = field.get(null);
            return dispatcher != null;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
