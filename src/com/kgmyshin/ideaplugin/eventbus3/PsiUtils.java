package com.kgmyshin.ideaplugin.eventbus3;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.lang.Language;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.java.PsiIdentifierImpl;
import com.intellij.psi.impl.source.tree.java.PsiMethodCallExpressionImpl;
import com.intellij.psi.impl.source.tree.java.PsiReferenceExpressionImpl;
import com.kgmyshin.ideaplugin.eventbus3.utils.Constants;
import com.kgmyshin.ideaplugin.eventbus3.utils.MLog;
import org.jetbrains.kotlin.psi.*;

/**
 * modify by likfe ( https://github.com/likfe/ ) on 2018/03/05.
 */
public class PsiUtils {

    public static PsiClass getClass(PsiType psiType) {
        if (psiType instanceof PsiClassType) {
            return ((PsiClassType) psiType).resolve();
        }
        return null;
    }

    public static boolean isEventBusReceiver(PsiElement psiElement) {
        if (psiElement.getLanguage().is(Language.findLanguageByID("JAVA"))) {

            if (psiElement instanceof PsiMethod) {
                PsiMethod method = (PsiMethod) psiElement;
                PsiModifierList modifierList = method.getModifierList();
                for (PsiAnnotation psiAnnotation : modifierList.getAnnotations()) {
                    if (safeEquals(psiAnnotation.getQualifiedName(), Constants.FUN_ANNOTATION)) {
                        return true;
                    }
                }
            }
        } else if (psiElement.getLanguage().is(Language.findLanguageByID("kotlin"))) {

            if (psiElement instanceof KtNamedFunction) {
                KtNamedFunction function = (KtNamedFunction) psiElement;
                KtModifierList modifierList = function.getModifierList();
                if (modifierList != null) {
                    for (KtAnnotationEntry annotationEntry : modifierList.getAnnotationEntries()) {
                        KtConstructorCalleeExpression calleeExpression = annotationEntry.getCalleeExpression();
                        if (calleeExpression != null && safeEquals(calleeExpression.getText(), Constants.FUN_ANNOTATION_KT)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public static boolean isEventBusPost(PsiElement psiElement) {
        if (psiElement.getLanguage().is(Language.findLanguageByID("JAVA"))) {

            if (psiElement instanceof PsiMethodCallExpressionImpl && psiElement.getFirstChild() != null && psiElement.getFirstChild() instanceof PsiReferenceExpressionImpl) {
                PsiReferenceExpressionImpl all = (PsiReferenceExpressionImpl) psiElement.getFirstChild();
                if (all.getFirstChild() instanceof PsiMethodCallExpressionImpl && all.getLastChild() instanceof PsiIdentifierImpl) {
                    PsiMethodCallExpressionImpl start = (PsiMethodCallExpressionImpl) all.getFirstChild();
                    PsiIdentifierImpl post = (PsiIdentifierImpl) all.getLastChild();
                    if (safeEquals(post.getText(), Constants.FUN_NAME) && safeEquals(start.getText(), Constants.FUN_START)) {
                        return true;
                    }
                }
            }

        } else if (psiElement.getLanguage().is(Language.findLanguageByID("kotlin"))) {

            if (psiElement instanceof KtDotQualifiedExpression) {
                KtDotQualifiedExpression all = (KtDotQualifiedExpression) psiElement;
                if (all.getFirstChild() instanceof KtDotQualifiedExpression && all.getLastChild() instanceof KtCallExpression) {
                    String start = all.getFirstChild().getText();
                    if (start != null && start.equals(Constants.FUN_START)) {
                        KtCallExpression postRoot = (KtCallExpression) all.getLastChild();
                        if (postRoot.getFirstChild() instanceof KtNameReferenceExpression) {
                            KtNameReferenceExpression referenceExpression = (KtNameReferenceExpression) postRoot.getFirstChild();
                            if (referenceExpression.getReferencedName().equals(Constants.FUN_NAME)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private static boolean safeEquals(String obj, String value) {
        return obj != null && obj.equals(value);
    }

    public static boolean isKotlin(PsiElement psiElement) {
        return psiElement.getLanguage().is(Language.findLanguageByID("kotlin"));
    }

    public static boolean isJava(PsiElement psiElement) {
        return psiElement.getLanguage().is(Language.findLanguageByID("JAVA"));
    }

    /**
     * is kotlin plug installed and enable
     *
     * @return boolean
     */
    public static boolean checkIsKotlinInstalled() {
        PluginId pluginId = PluginId.findId("org.jetbrains.kotlin");
        if (pluginId != null) {
            IdeaPluginDescriptor pluginDescriptor = PluginManager.getPlugin(pluginId);
            return pluginDescriptor != null && pluginDescriptor.isEnabled();
        }
        return false;
    }

    private static void logPluginList() {
        IdeaPluginDescriptor[] pluginDescriptors = PluginManager.getPlugins();
        MLog.debug("== list plug ==");
        for (IdeaPluginDescriptor item : pluginDescriptors) {
            MLog.debug("id: " + item.getPluginId().getIdString() + " name: " + item.getName() + " isEnable: " + item.isEnabled());
        }
        MLog.debug("== list plug end ==");
    }

}
