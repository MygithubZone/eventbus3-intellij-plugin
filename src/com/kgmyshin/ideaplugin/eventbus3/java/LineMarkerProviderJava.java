package com.kgmyshin.ideaplugin.eventbus3.java;

import com.intellij.codeHighlighting.Pass;
import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.find.FindManager;
import com.intellij.find.findUsages.FindUsagesHandler;
import com.intellij.find.findUsages.FindUsagesManager;
import com.intellij.find.findUsages.FindUsagesOptions;
import com.intellij.find.impl.FindManagerImpl;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiUtilBase;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.usageView.UsageInfo;
import com.intellij.util.CommonProcessors;
import com.kgmyshin.ideaplugin.eventbus3.PsiUtils;
import com.kgmyshin.ideaplugin.eventbus3.ShowUsagesAction;
import com.kgmyshin.ideaplugin.eventbus3.utils.Constants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by kgmyshin on 15/06/08.
 * <p>
 * modify by likfe ( https://github.com/likfe/ ) in 2018/03/06
 * </p>
 */
public class LineMarkerProviderJava implements com.intellij.codeInsight.daemon.LineMarkerProvider {

    /**
     * use Subscribe to find all matched post
     */

    private static GutterIconNavigationHandler<PsiElement> SHOW_SENDERS =
            new GutterIconNavigationHandler<PsiElement>() {
                @Override
                public void navigate(MouseEvent e, PsiElement psiElement) {
                    if (psiElement instanceof PsiMethod) {
                        Project project = psiElement.getProject();
                        JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);
                        PsiClass eventBusClass = javaPsiFacade.findClass(Constants.FUN_EVENT_CLASS, GlobalSearchScope.allScope(project));
                        if (eventBusClass == null) return;

                        PsiMethod method = (PsiMethod) psiElement;

                        //post
                        PsiMethod postMethod = eventBusClass.findMethodsByName(Constants.FUN_NAME, false)[0];
                        if (null != postMethod) {
                            PsiClass eventClass = ((PsiClassType) method.getParameterList().getParameters()[0].getTypeElement().getType()).resolve();
                            List<PsiClass> allClasses = searchSubClasses(eventClass);
                            allClasses.add(eventClass);
                            new ShowUsagesAction(new SenderFilterJava(allClasses)).startFindUsages(postMethod, new RelativePoint(e), PsiUtilBase.findEditor(psiElement), Constants.MAX_USAGES);
                        }

                        //postSticky
//                        PsiMethod postMethod2 = eventBusClass.findMethodsByName(Constants.FUN_NAME2, false)[0];
//                        if (null != postMethod2) {
//                            PsiClass eventClass = ((PsiClassType) method.getParameterList().getParameters()[0].getTypeElement().getType()).resolve();
//
//                            new ShowUsagesAction(new SenderFilterJava(eventClass)).startFindUsages(postMethod2, new RelativePoint(e), PsiUtilBase.findEditor(psiElement), Constants.MAX_USAGES);
//                        }

                    }


                }
            };

    /**
     * use post to find all matched Subscribe
     */

    private static GutterIconNavigationHandler<PsiElement> SHOW_RECEIVERS =
            new GutterIconNavigationHandler<PsiElement>() {
                @Override
                public void navigate(MouseEvent e, PsiElement psiElement) {
                    if (psiElement instanceof PsiMethodCallExpression) {
                        PsiMethodCallExpression expression = (PsiMethodCallExpression) psiElement;
                        try {
                            PsiType[] expressionTypes = expression.getArgumentList().getExpressionTypes();
                            if (expressionTypes.length > 0) {
                                PsiClass eventClass = PsiUtils.getClass(expressionTypes[0]);
                                if (eventClass != null) {
                                    List<PsiElement> generatedDeclarations = new ArrayList<>();
                                    generatedDeclarations.add(PsiUtils.getClass(expressionTypes[0]));
                                    for (PsiType superType : expressionTypes[0].getSuperTypes()) {
                                        generatedDeclarations.add(PsiUtils.getClass(superType));
                                    }
                                    new ShowUsagesAction(new ReceiverFilterJava()).startFindUsages(generatedDeclarations, eventClass, new RelativePoint(e), PsiUtilBase.findEditor(psiElement), Constants.MAX_USAGES);
                                }
                            }
                        } catch (Exception ee) {
                            ee.fillInStackTrace();
                        }

                    }
                }
            };

    @Nullable
    @Override
    public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement psiElement) {
        if (!PsiUtils.isJava(psiElement)) return null;
        //if (!(psiElement instanceof PsiIdentifier && psiElement.getParent() instanceof PsiMethod)) return null;
        if (PsiUtils.isEventBusPost(psiElement)) {
            LineMarkerInfo info = new LineMarkerInfo<PsiElement>(psiElement, psiElement.getTextRange(), Constants.ICON,
                    Pass.UPDATE_ALL, null, SHOW_RECEIVERS,
                    GutterIconRenderer.Alignment.LEFT);
            return info;
        } else if (PsiUtils.isEventBusReceiver(psiElement)) {
            LineMarkerInfo info = new LineMarkerInfo<PsiElement>(psiElement, psiElement.getTextRange(), Constants.ICON,
                    Pass.UPDATE_ALL, null, SHOW_SENDERS,
                    GutterIconRenderer.Alignment.LEFT);
            return info;
        }
        return null;
    }


    @Override
    public void collectSlowLineMarkers(@NotNull List<PsiElement> list, @NotNull Collection<LineMarkerInfo> collection) {
//        for (PsiElement psiElement : list) {
//
//            ProgressManager.checkCanceled();
//
//            if (PsiUtils.isJava(psiElement)) {
//
//                if (PsiUtils.isEventBusPost(psiElement)) {
//
////                    LineMarkerInfo info = new LineMarkerInfo<PsiElement>(psiElement, psiElement.getTextRange(), Constants.ICON,
////                            Pass.UPDATE_ALL, null, SHOW_RECEIVERS,
////                            GutterIconRenderer.Alignment.LEFT);
//                    collection.add(getLineMarkerInfo(psiElement));
//                } else if (PsiUtils.isEventBusReceiver(psiElement)) {
//                    LineMarkerInfo info = new LineMarkerInfo<PsiElement>(psiElement, psiElement.getTextRange(), Constants.ICON,
//                            Pass.UPDATE_ALL, null, SHOW_SENDERS,
//                            GutterIconRenderer.Alignment.LEFT);
//                    collection.add(getLineMarkerInfo(psiElement));
//                }
//            }
//        }
    }

    @NotNull
    public static Collection<UsageInfo> search(@NotNull PsiElement element) {
        FindUsagesManager findUsagesManager = ((FindManagerImpl) FindManager.getInstance(element.getProject())).getFindUsagesManager();
        FindUsagesHandler findUsagesHandler = findUsagesManager.getFindUsagesHandler(element, false);
        final FindUsagesOptions options = findUsagesHandler.getFindUsagesOptions();
        final CommonProcessors.CollectProcessor<UsageInfo> processor = new CommonProcessors.CollectProcessor<UsageInfo>();
        for (PsiElement primaryElement : findUsagesHandler.getPrimaryElements()) {
            findUsagesHandler.processElementUsages(primaryElement, processor, options);
        }
        for (PsiElement secondaryElement : findUsagesHandler.getSecondaryElements()) {
            findUsagesHandler.processElementUsages(secondaryElement, processor, options);
        }
        return processor.getResults();
    }

    @NotNull
    private static List<PsiClass> searchSubClasses(@NotNull PsiClass element) {
        Collection<UsageInfo> usageInfos = search(element);
        List<PsiClass> subClasses = new ArrayList<>();
        for (UsageInfo usageInfo : usageInfos) {
            PsiElement psiElement = usageInfo.getElement();
            if (psiElement.getParent() instanceof PsiReferenceList) {
                PsiReferenceList referenceListElement = (PsiReferenceList) psiElement.getParent();
                if (referenceListElement.getRole() == PsiReferenceList.Role.EXTENDS_LIST) {
                    if (referenceListElement.getParent() instanceof PsiClass) {
                        subClasses.add((PsiClass) referenceListElement.getParent());
                    }
                }
            }
        }
        return subClasses;
    }
}
