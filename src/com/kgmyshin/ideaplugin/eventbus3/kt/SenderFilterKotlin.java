package com.kgmyshin.ideaplugin.eventbus3.kt;

import com.intellij.psi.*;
import com.intellij.usages.Usage;
import com.intellij.usages.UsageInfo2UsageAdapter;
import com.kgmyshin.ideaplugin.eventbus3.Filter;
import com.kgmyshin.ideaplugin.eventbus3.PsiUtils;
import com.kgmyshin.ideaplugin.eventbus3.utils.MLog;
import org.jetbrains.kotlin.psi.KtTypeReference;

/**
 * Created by likfe ( https://github.com/likfe/ ) in 2018/03/06
 *
 */
public class SenderFilterKotlin implements Filter {

    private final KtTypeReference eventClass;

    SenderFilterKotlin(KtTypeReference eventClass) {
        this.eventClass = eventClass;
    }

    @Override
    public boolean shouldShow(Usage usage) {

        PsiElement element = ((UsageInfo2UsageAdapter) usage).getElement();
        if (element instanceof PsiReferenceExpression) {
            if ((element = element.getParent()) instanceof PsiMethodCallExpression) {
                PsiMethodCallExpression callExpression = (PsiMethodCallExpression) element;
                PsiType[] types = callExpression.getArgumentList().getExpressionTypes();
                for (PsiType type : types) {
                    MLog.debug("shouldShow: 01 : " + PsiUtils.getClass(type).getName());
                    MLog.debug("shouldShow: 02 : " + eventClass.getText());
                    if (PsiUtils.getClass(type).getName().equals(eventClass.getText())) {
                        // pattern : EventBus.getDefault().post(new Event());
                        return true;
                    }
                }
                if ((element = element.getParent()) instanceof PsiExpressionStatement) {
                    if ((element = element.getParent()) instanceof PsiCodeBlock) {
                        PsiCodeBlock codeBlock = (PsiCodeBlock) element;
                        PsiStatement[] statements = codeBlock.getStatements();
                        for (PsiStatement statement : statements) {
                            if (statement instanceof PsiDeclarationStatement) {
                                PsiDeclarationStatement declarationStatement = (PsiDeclarationStatement) statement;
                                PsiElement[] elements = declarationStatement.getDeclaredElements();
                                for (PsiElement variable : elements) {
                                    if (variable instanceof PsiLocalVariable) {
                                        PsiLocalVariable localVariable = (PsiLocalVariable) variable;
                                        PsiClass psiClass = PsiUtils.getClass(localVariable.getTypeElement().getType());
                                        try {
                                            MLog.debug("shouldShow: 03 : " + psiClass.getName());
                                            MLog.debug("shouldShow: 04 : " + eventClass.getText());
                                            if (psiClass.getName().equals(eventClass.getText())) {
                                                // pattern :
                                                //   Event event = new Event();
                                                //   EventBus.getDefault().post(event);
                                                return true;
                                            }
                                        } catch (NullPointerException e) {
                                            System.out.println(e.toString());
                                        }

                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return false;
    }
}
