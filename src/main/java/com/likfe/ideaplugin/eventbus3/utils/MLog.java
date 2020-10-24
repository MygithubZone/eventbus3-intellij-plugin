package com.likfe.ideaplugin.eventbus3.utils;

import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.diagnostic.Logger;

/**
 * Created by likfe on 2018/3/1.
 */
public class MLog {
    private static final String TAG = "eb3";
    public static final Boolean DEBUG = true;
    private static final Logger LOG = Logger.getInstance(MLog.class);


    public static void debug(String s) {
        LOG.info(s);
        PluginManagerCore.getLogger().debug(s);
        log(s);
    }

    public static void debug(Throwable s) {
        LOG.info(s);
        PluginManagerCore.getLogger().debug(s);
        log(s.toString());
    }


    public static void debug(String s, Object... more) {
        PluginManagerCore.getLogger().debug(s, more);
        StringBuilder sb = new StringBuilder(s);
        for (Object obj : more) {
            sb.append(obj);
        }

        log(sb.toString());
        LOG.info(sb.toString());
    }

    private static void log(String s) {
        Notification notification = new Notification(TAG, "debug", s, NotificationType.INFORMATION);
        //Notifications.Bus.notify(notification);

        com.intellij.openapi.diagnostic.Logger.getInstance("#ddd").debug(s);

    }
}
