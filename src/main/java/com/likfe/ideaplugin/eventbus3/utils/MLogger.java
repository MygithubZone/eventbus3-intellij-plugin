package com.likfe.ideaplugin.eventbus3.utils;

import com.intellij.openapi.diagnostic.Logger;
import org.apache.log4j.Level;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by likfe on 2018/3/2.
 */
public class MLogger extends Logger {

    @NotNull
    public static MLogger getInstance(@NotNull String s) {
        MLogger logger = new MLogger();
        logger.setLevel(Level.ALL);
        return logger;
    }


    @Override
    public boolean isDebugEnabled() {
        return MLog.DEBUG;
    }

    @Override
    public void debug(@NonNls String s) {
        System.err.println("ERROR: " + s);
    }

    @Override
    public void debug(@Nullable Throwable throwable) {

    }

    @Override
    public void debug(@NonNls String s, @Nullable Throwable throwable) {

    }

    @Override
    public void info(@NonNls String s) {

    }

    @Override
    public void info(@NonNls String s, @Nullable Throwable throwable) {

    }

    @Override
    public void warn(@NonNls String s, @Nullable Throwable throwable) {

    }

    @Override
    public void error(@NonNls String s, @Nullable Throwable throwable, @NonNls @NotNull String... strings) {

    }

    @Override
    public void setLevel(Level level) {

    }

    @Override
    public boolean isTraceEnabled() {
        return MLog.DEBUG;
    }
}
