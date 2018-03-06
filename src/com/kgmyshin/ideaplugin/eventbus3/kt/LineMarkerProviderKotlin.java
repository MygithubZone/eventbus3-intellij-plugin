package com.kgmyshin.ideaplugin.eventbus3.kt;

import com.intellij.codeHighlighting.Pass;
import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiUtilBase;
import com.intellij.ui.awt.RelativePoint;
import com.kgmyshin.ideaplugin.eventbus3.PsiUtils;
import com.kgmyshin.ideaplugin.eventbus3.java.ReceiverFilterJava;
import com.kgmyshin.ideaplugin.eventbus3.ShowUsagesAction;
import com.kgmyshin.ideaplugin.eventbus3.utils.Constants;
import com.kgmyshin.ideaplugin.eventbus3.utils.MLog;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.psi.KtNamedFunction;
import org.jetbrains.kotlin.psi.KtParameter;
import org.jetbrains.kotlin.psi.KtParameterList;
import org.jetbrains.kotlin.psi.KtTypeReference;

import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.List;

/**
 * Created by by likfe ( https://github.com/likfe/ )  on 18/03/05.
 */
public class LineMarkerProviderKotlin implements com.intellij.codeInsight.daemon.LineMarkerProvider {

    private static GutterIconNavigationHandler<PsiElement> SHOW_SENDERS =
            new GutterIconNavigationHandler<PsiElement>() {
                @Override
                public void navigate(MouseEvent e, PsiElement psiElement) {
                    MLog.debug("kt SHOW_SENDERS 0: " + psiElement.getText());
                    if (PsiUtils.isKotlin(psiElement) && psiElement instanceof KtNamedFunction) {
                        Project project = psiElement.getProject();
                        JavaPsiFacade psiFacade = JavaPsiFacade.getInstance(project);
                        PsiClass eventBusClass = psiFacade.findClass(Constants.FUN_EVENT_CLASS, GlobalSearchScope.allScope(project));
                        PsiMethod postMethod = null;
                        if (eventBusClass != null) {
                            postMethod = eventBusClass.findMethodsByName(Constants.FUN_NAME, false)[0];
                            MLog.debug("kt SHOW_SENDERS 1: " + eventBusClass.getText());
                            MLog.debug("kt SHOW_SENDERS 2: " + postMethod.getText());
                        }

                        KtTypeReference eventClass = null;
                        KtNamedFunction function = (KtNamedFunction) psiElement;
                        KtParameterList parameterList = function.getValueParameterList();
                        if (parameterList != null && parameterList.getParameters().size() == 1) {
                            KtParameter parameter = parameterList.getParameters().get(0);
                            eventClass = parameter.getTypeReference();
                            MLog.debug("kt SHOW_SENDERS 3: " + eventClass.toString());
                        }

                        if (postMethod != null && eventClass != null) {
                            new ShowUsagesAction(new SenderFilterKotlin(eventClass))
                                    .startFindUsages(
                                            postMethod,
                                            new RelativePoint(e),
                                            PsiUtilBase.findEditor(psiElement),
                                            Constants.MAX_USAGES
                                    );
                        }
                    }
                }
            };

    private static GutterIconNavigationHandler<PsiElement> SHOW_RECEIVERS =
            new GutterIconNavigationHandler<PsiElement>() {
                @Override
                public void navigate(MouseEvent e, PsiElement psiElement) {
                    MLog.debug("kt SHOW_RECEIVERS 0: " + psiElement.getText());

                    if (psiElement instanceof PsiMethodCallExpression) {
                        PsiMethodCallExpression expression = (PsiMethodCallExpression) psiElement;
                        PsiType[] expressionTypes = expression.getArgumentList().getExpressionTypes();
                        if (expressionTypes.length > 0) {
                            PsiClass eventClass = PsiUtils.getClass(expressionTypes[0]);
                            if (eventClass != null) {
                                new ShowUsagesAction(
                                        new ReceiverFilterJava()).startFindUsages(eventClass, new RelativePoint(e),
                                        PsiUtilBase.findEditor(psiElement),
                                        Constants.MAX_USAGES);
                            }
                        }
                    }
                }
            };

    @Nullable
    @Override
    public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement psiElement) {
        return null;
    }


    @Override
    public void collectSlowLineMarkers(@NotNull List<PsiElement> list, @NotNull Collection<LineMarkerInfo> collection) {
        for (PsiElement psiElement : list) {

            ProgressManager.checkCanceled();
            if (PsiUtils.isKotlin(psiElement)) {

                if (PsiUtils.isEventBusPost(psiElement)) {
                    LineMarkerInfo info = new LineMarkerInfo<PsiElement>(psiElement, psiElement.getTextRange(), Constants.ICON,
                            Pass.UPDATE_ALL, null, SHOW_RECEIVERS,
                            GutterIconRenderer.Alignment.LEFT);
                    collection.add(info);
                } else if (PsiUtils.isEventBusReceiver(psiElement)) {
                    LineMarkerInfo info = new LineMarkerInfo<PsiElement>(psiElement, psiElement.getTextRange(), Constants.ICON,
                            Pass.UPDATE_ALL, null, SHOW_SENDERS,
                            GutterIconRenderer.Alignment.LEFT);
                    collection.add(info);
                }
            }
        }

    }
}
