package com.kgmyshin.ideaplugin.eventbus3;

import com.intellij.codeHighlighting.Pass;
import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiUtilBase;
import com.intellij.ui.awt.RelativePoint;
import com.kgmyshin.ideaplugin.eventbus3.utils.Constants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.List;

/**
 * Created by kgmyshin on 15/06/08.
 * <p>
 * modify by likfe ( https://github.com/likfe/ ) in 2016/09/05
 * <p>
 * 1.fix package name for EventBus
 * <p>
 * 2. try use `GlobalSearchScope.projectScope(project)` to just search for project,but get NullPointerException,
 * the old use `GlobalSearchScope.allScope(project)` ,it will search in project and libs,so slow
 */
public class LineMarkerProviderJava implements com.intellij.codeInsight.daemon.LineMarkerProvider {

    public static final Icon ICON = IconLoader.getIcon(Constants.ICON_PATH);

    public static final int MAX_USAGES = 100;

    private static GutterIconNavigationHandler<PsiElement> SHOW_SENDERS =
            new GutterIconNavigationHandler<PsiElement>() {
                @Override
                public void navigate(MouseEvent e, PsiElement psiElement) {
                    if (psiElement instanceof PsiMethod) {
                        Project project = psiElement.getProject();
                        JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);
                        PsiClass eventBusClass = javaPsiFacade.findClass("org.greenrobot.eventbus.EventBus", GlobalSearchScope.allScope(project));
                        //PsiClass eventBusClass = javaPsiFacade.findClass("org.greenrobot.eventbus.EventBus", GlobalSearchScope.projectScope(project));
                        if (eventBusClass == null) return;
                        PsiMethod postMethod = eventBusClass.findMethodsByName("post", false)[0];
                        PsiMethod method = (PsiMethod) psiElement;
                        if (postMethod == null) return;
                        PsiClass eventClass = ((PsiClassType) method.getParameterList().getParameters()[0].getTypeElement().getType()).resolve();

                        new ShowUsagesAction(new SenderFilter(eventClass)).startFindUsages(postMethod, new RelativePoint(e), PsiUtilBase.findEditor(psiElement), MAX_USAGES);
                    }
                }
            };

    private static GutterIconNavigationHandler<PsiElement> SHOW_RECEIVERS =
            new GutterIconNavigationHandler<PsiElement>() {
                @Override
                public void navigate(MouseEvent e, PsiElement psiElement) {
                    if (psiElement instanceof PsiMethodCallExpression) {
                        PsiMethodCallExpression expression = (PsiMethodCallExpression) psiElement;
                        PsiType[] expressionTypes = expression.getArgumentList().getExpressionTypes();
                        if (expressionTypes.length > 0) {
                            PsiClass eventClass = PsiUtils.getClass(expressionTypes[0]);
                            if (eventClass != null) {
                                new ShowUsagesAction(new ReceiverFilter()).startFindUsages(eventClass, new RelativePoint(e), PsiUtilBase.findEditor(psiElement), MAX_USAGES);
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

            if (PsiUtils.isEventBusPost(psiElement)) {
                LineMarkerInfo info = new LineMarkerInfo<PsiElement>(psiElement, psiElement.getTextRange(), ICON,
                        Pass.UPDATE_ALL, null, SHOW_RECEIVERS,
                        GutterIconRenderer.Alignment.LEFT);
                collection.add(info);
            } else if (PsiUtils.isEventBusReceiver(psiElement)) {
                LineMarkerInfo info = new LineMarkerInfo<PsiElement>(psiElement, psiElement.getTextRange(), ICON,
                        Pass.UPDATE_ALL, null, SHOW_SENDERS,
                        GutterIconRenderer.Alignment.LEFT);
                collection.add(info);
            }
        }

    }
}
