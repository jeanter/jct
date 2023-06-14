
package com.json.work.util;

import java.util.HashSet;

public class ShutdownHookUtils {

    private static final HashSet<Thread> hooks = new HashSet<>();

    public static synchronized void addShutdownHook(Object object, Runnable hook) {
        String hookName = object.getClass().getSimpleName() + "-ShutdownHook-" + hooks.size();
        addShutdownHook(hookName, hook, false);
    }

    public static synchronized void addShutdownHook(String hookName, Runnable hook) {
        addShutdownHook(hookName, hook, true);
    }

    public static synchronized void addShutdownHook(String hookName, Runnable hook, boolean addPostfix) {
        if (addPostfix)
            hookName += "-ShutdownHook";
        Thread t = new Thread(hook, hookName);
        addShutdownHook(t);
    }

    public static synchronized void addShutdownHook(Thread hook) {
        hooks.add(hook);
        Runtime.getRuntime().addShutdownHook(hook);
    }

    public static synchronized void removeShutdownHook(Thread hook) {
        hooks.remove(hook);
        Runtime.getRuntime().removeShutdownHook(hook);
    }

    public static synchronized void removeAllShutdownHooks() {
        for (Thread hook : hooks) {
            Runtime.getRuntime().removeShutdownHook(hook);
        }
        hooks.clear();
    }
}
