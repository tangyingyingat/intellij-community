package com.intellij.codeInsight.folding.impl;

import com.intellij.injected.editor.DocumentWindow;
import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.lang.folding.FoldingBuilder;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.lang.folding.LanguageFolding;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

class FoldingUpdate {
  private static final Logger LOG = Logger.getInstance("#com.intellij.codeInsight.folding.impl.FoldingUpdate");

  private static final Key<Object> LAST_UPDATE_STAMP_KEY = Key.create("LAST_UPDATE_STAMP_KEY");
  private static final Comparator<PsiElement> COMPARE_BY_OFFSET = new Comparator<PsiElement>() {
      public int compare(PsiElement element, PsiElement element1) {
        int startOffsetDiff = element.getTextRange().getStartOffset() - element1.getTextRange().getStartOffset();
        return startOffsetDiff == 0 ? element.getTextRange().getEndOffset() - element1.getTextRange().getEndOffset() : startOffsetDiff;
      }
    };

  private FoldingUpdate() {
  }

  @Nullable
  public static Runnable updateFoldRegions(@NotNull final Editor editor, @NotNull PsiFile file, boolean applyDefaultState) {
    ApplicationManager.getApplication().assertReadAccessAllowed();

    final Project project = file.getProject();
    Document document = editor.getDocument();
    LOG.assertTrue(!PsiDocumentManager.getInstance(project).isUncommited(document));

    final long timeStamp = document.getModificationStamp();
    Object lastTimeStamp = editor.getUserData(LAST_UPDATE_STAMP_KEY);
    if (lastTimeStamp instanceof Long && ((Long)lastTimeStamp).longValue() == timeStamp && !applyDefaultState) return null;

    if (file instanceof PsiCompiledElement){
      file = (PsiFile)((PsiCompiledElement)file).getMirror();
    }

    final TreeMap<PsiElement, FoldingDescriptor> elementsToFoldMap = new TreeMap<PsiElement, FoldingDescriptor>(COMPARE_BY_OFFSET);
    getFoldingsFor(file, document, elementsToFoldMap);

    List<DocumentWindow> injectedDocuments = InjectedLanguageUtil.getCachedInjectedDocuments(file);
    for (DocumentWindow injectedDocument : injectedDocuments) {
      PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(injectedDocument);
      if (psiFile == null || !psiFile.isValid()) continue;
      getFoldingsFor(psiFile, injectedDocument, elementsToFoldMap);
    }

    final Runnable operation = new UpdateFoldRegionsOperation(editor, elementsToFoldMap, applyDefaultState);
    return new Runnable() {
      public void run() {
        editor.getFoldingModel().runBatchFoldingOperationDoNotCollapseCaret(operation);
        editor.putUserData(LAST_UPDATE_STAMP_KEY, timeStamp);
      }
    };
  }

  private static void getFoldingsFor(PsiFile file, Document document, TreeMap<PsiElement, FoldingDescriptor> elementsToFoldMap) {
    final FileViewProvider viewProvider = file.getViewProvider();
    for (final Language language : viewProvider.getLanguages()) {
      final PsiFile psi = viewProvider.getPsi(language);
      final FoldingBuilder foldingBuilder = LanguageFolding.INSTANCE.forLanguage(language);
      if (psi != null && foldingBuilder != null) {
        final ASTNode node = psi.getNode();
        for (FoldingDescriptor descriptor : foldingBuilder.buildFoldRegions(node, document)) {
          elementsToFoldMap.put(descriptor.getElement().getPsi(), descriptor);
        }
      }
    }
  }

}
