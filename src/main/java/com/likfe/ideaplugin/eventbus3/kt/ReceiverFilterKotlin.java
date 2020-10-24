package com.likfe.ideaplugin.eventbus3.kt;

import com.intellij.psi.*;
import com.intellij.usages.Usage;
import com.intellij.usages.UsageInfo2UsageAdapter;
import com.likfe.ideaplugin.eventbus3.Filter;
import com.likfe.ideaplugin.eventbus3.PsiUtils;
import com.likfe.ideaplugin.eventbus3.utils.MLog;

/**
 * Created by kgmyshin on 2015/06/07.
 */
public class ReceiverFilterKotlin implements Filter {
    @Override
    public boolean shouldShow(Usage usage) {
        PsiElement element = ((UsageInfo2UsageAdapter) usage).getElement();
        MLog.debug("ReceiverFilterKotlin 0 "+ PsiUtils.isKotlin(element));
        MLog.debug("ReceiverFilterKotlin 0 " + element.toString());
        if (PsiUtils.isEventBusReceiver(element)) {
            MLog.debug("ReceiverFilterKotlin 1 isEventBusReceiver");
            return true;
        }
        return false;
    }
}
