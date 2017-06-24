package server.events.custom;

import client.MapleCharacter;
import net.PacketHandler;
import scheduler.Task;
import server.TimerManager;
import tools.annotation.PacketWorker;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author izarooni
 */
public abstract class GenericEvent {

    private HashMap<Class<?>, ArrayList<Method>> methods = new HashMap<>();

    /**
     * Iterates through methods of the specified object class and stores
     * found valid methods in an array list which is mapped to a packet event class
     *
     * @param object a class that contains annotated methods for packet event handlers
     */
    protected final void registerAnnotationPacketEvents(Object object) {
        Class<?> clazz = object.getClass();
        for (Method method : clazz.getMethods()) {
            if (method.getAnnotation(PacketWorker.class) != null && method.getParameterCount() == 1) {
                Class<?>[] pTypes = method.getParameterTypes();
                // only add methods which parameters meet specifications
                // in this case, there is 1 parameter and it is a child of the PacketEvent
                if (PacketHandler.class.isAssignableFrom(pTypes[0])) {
                    methods.putIfAbsent(pTypes[0], new ArrayList<>());
                    methods.get(pTypes[0]).add(method);
                }
            }
        }
    }

    /**
     * Invokes all methods stored with a matching packet event parameter to this specified method call parameter.
     * <p>
     * Not to be overridden by a child class
     * </p>
     *
     * @param event a packet event
     */
    public final void onPacketEvent(PacketHandler event) {
        if (methods.get(event.getClass()) != null) {
            methods.get(event.getClass()).forEach(method -> {
                try {
                    System.out.println(String.format("[DEBUG] PacketEvent method '%s(%s)' invoked", method.getName(), event.getClass().getSimpleName()));
                    method.invoke(this, event);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public void onPlayerDeath(MapleCharacter player) {
        // do nothing
    }

    public void onPlayerDisconnect(MapleCharacter player) {
        // do nothing
    }

    public abstract void registerPlayer(MapleCharacter player);

    public abstract void unregisterPlayer(MapleCharacter player);
}
