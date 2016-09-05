package com.kgmyshin.ideaplugin.eventbus3;

import com.intellij.openapi.compiler.CompilationException;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.IconLoader;
import com.intellij.psi.*;
import com.intellij.usages.Usage;
import com.intellij.usages.UsageInfo2UsageAdapter;

import javax.swing.*;

/**
 * Created by kgmyshin on 2015/06/07.
 *
 * modify by likfe ( https://github.com/likfe/ ) in 2016/09/05
 *
 * add try-catch
 */
public class SenderFilter implements Filter {

    private final PsiClass eventClass;

    SenderFilter(PsiClass eventClass) {
        this.eventClass = eventClass;
    }

    private static final Icon ICON = IconLoader.getIcon("/icons/icon.png");

    @Override
    public boolean shouldShow(Usage usage) {
        PsiElement element = ((UsageInfo2UsageAdapter) usage).getElement();
        if (element instanceof PsiReferenceExpression) {
            if ((element = element.getParent()) instanceof PsiMethodCallExpression) {
                PsiMethodCallExpression callExpression = (PsiMethodCallExpression) element;
                PsiType[] types = callExpression.getArgumentList().getExpressionTypes();
                for (PsiType type : types) {
                    if (PsiUtils.getClass(type).getName().equals(eventClass.getName())) {
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
                                            if (psiClass.getName().equals(eventClass.getName())) {
                                                // pattern :
                                                //   Event event = new Event();
                                                //   EventBus.getDefault().post(event);
                                                return true;
                                            }
                                        }catch (NullPointerException e){
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
