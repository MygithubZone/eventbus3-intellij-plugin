package com.kgmyshin.ideaplugin.eventbus3;

import com.intellij.ide.plugins.PluginManager;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.kgmyshin.ideaplugin.eventbus3.utils.MLogger;

/**
 * Created by lait on 2018/3/1.
 */
public class MLog {
    private static final String TAG = "eb3";
    public static final Boolean DEBUG = true;
    private static final MLogger mLog = MLogger.getInstance(TAG);


    public static void debug(String s) {
        mLog.debug(s);
        PluginManager.getLogger().debug(s);
        log(s);
    }

    public static void debug(Throwable s) {
        mLog.debug(s);
        PluginManager.getLogger().debug(s);
        log(s.toString());
    }


    public static void debug(String s, Object... more) {
        PluginManager.getLogger().debug(s, more);
        StringBuilder sb = new StringBuilder(s);
        for (Object obj : more) {
            sb.append(obj);
        }

        log(sb.toString());
        mLog.debug(sb.toString());
    }

    private static void log(String s) {
        Notification notification = new Notification(TAG, "debug", s, NotificationType.INFORMATION);
        Notifications.Bus.notify(notification);
    }
}
