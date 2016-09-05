package com.kgmyshin.ideaplugin.eventbus3;

import com.intellij.psi.*;

/**
 * Created by kgmyshin on 2015/06/07.
 */
public class PsiUtils {

    public static PsiClass getClass(PsiType psiType) {
        if (psiType instanceof PsiClassType) {
            return ((PsiClassType) psiType).resolve();
        }
        return null;
    }

    public static boolean isEventBusReceiver(PsiElement psiElement) {
        if (psiElement instanceof PsiMethod) {
            PsiMethod method = (PsiMethod) psiElement;
            PsiModifierList modifierList = method.getModifierList();
            for (PsiAnnotation psiAnnotation : modifierList.getAnnotations()) {
                if (psiAnnotation.getQualifiedName().equals("org.greenrobot.eventbus.Subscribe")) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isEventBusPost(PsiElement psiElement) {
        if (psiElement instanceof PsiCallExpression) {
            PsiCallExpression callExpression = (PsiCallExpression) psiElement;
            PsiMethod method = callExpression.resolveMethod();
            if (method != null) {
                String name = method.getName();
                PsiElement parent = method.getParent();
                if (name != null && name.equals("post") && parent instanceof PsiClass) {
                    PsiClass implClass = (PsiClass) parent;
                    if (isEventBusClass(implClass) || isSuperClassEventBus(implClass)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean isEventBusClass(PsiClass psiClass) {
        try {
            return psiClass.getName().equals("EventBus");
        } catch (Exception e) {
            System.out.println(e.toString());
            return false;
        }
    }

    private static boolean isSuperClassEventBus(PsiClass psiClass) {
        PsiClass[] supers = psiClass.getSupers();
        if (supers.length == 0) {
            return false;
        }
        for (PsiClass superClass : supers) {
            try {
                if (superClass.getName().equals("EventBus")) {
                    return true;
                }
            } catch (Exception e) {
                System.out.println(e.toString());
            }
//            if (superClass.getName().equals("EventBus")) {
//                return true;
//            }
        }
        return false;
    }

}
