package com.anabulatovic.closeunrelatedtabs;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.searches.ReferencesSearch;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class CloseUnrelatedTabsAction extends AnAction {

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        VirtualFile virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE);
        e.getPresentation().setEnabledAndVisible(project != null && virtualFile != null);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();

        if (project == null) return;

        VirtualFile virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE);

        if (virtualFile == null) return;

        CloseUnrelatedTabsSettings settings = CloseUnrelatedTabsSettings.getInstance();
        int referenceDepth = settings.getReferenceDepth();

        // todo: add progress bar
    }

    private void findOutgoingReferences(PsiFile psiFile, Set<VirtualFile> relatedFiles) {
        psiFile.accept(new PsiRecursiveElementVisitor() {
            @Override
            public void visitElement(@NotNull PsiElement element) {
                super.visitElement(element);

                // Get all references from this element
                for (PsiReference reference : element.getReferences()) {
                    PsiElement resolved = reference.resolve();
                    if (resolved != null && resolved.getContainingFile() != null) {
                        VirtualFile vf = resolved.getContainingFile().getVirtualFile();
                        if (vf != null && !vf.getPath().contains(".jar!") && !vf.getPath().contains(".class")) {
                            relatedFiles.add(vf);
                        }
                    }
                }
            }
        });
    }

    private void findIncomingReferences(PsiFile psiFile, Project project, Set<VirtualFile> relatedFiles) {
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
        VirtualFile[] openFiles = fileEditorManager.getOpenFiles();
        PsiManager psiManager = PsiManager.getInstance(project);

        // Check each open file to see if it references the target file
        for (VirtualFile openFile : openFiles) {
            if (openFile.equals(psiFile.getVirtualFile())) continue;

            PsiFile openPsiFile = psiManager.findFile(openFile);

            if (openPsiFile == null) continue;

            // Check if this open file references our target file
            boolean[] hasReference = {false};
            openPsiFile.accept(new PsiRecursiveElementVisitor() {
                @Override
                public void visitElement(@NotNull PsiElement element) {
                    if (hasReference[0]) return; // Early exit if a reference is found
                    super.visitElement(element);

                    for (PsiReference reference : element.getReferences()) {
                        PsiElement resolved = reference.resolve();
                        if (resolved != null && resolved.getContainingFile() != null) {
                            if (psiFile.getVirtualFile().equals(resolved.getContainingFile().getVirtualFile())) {
                                hasReference[0] = true;
                                return;
                            }
                        }
                    }
                }
            });

            if (hasReference[0]) {
                relatedFiles.add(openFile);
            }
        }

        // Also use ReferencesSearch to find references to elements declared in this file
        for (PsiElement child : psiFile.getChildren()) {
            if (child instanceof PsiNamedElement) {
                try {
                    for (PsiReference ref : ReferencesSearch.search(child).findAll()) {
                        PsiFile containingFile = ref.getElement().getContainingFile();
                        if (containingFile != null) {
                            VirtualFile vf = containingFile.getVirtualFile();
                            if (vf != null && !vf.getPath().contains(".jar!") && !vf.getPath().contains(".class")) {
                                relatedFiles.add(vf);
                            }
                        }
                    }
                } catch (Exception e) {
                    // Ignore unsearchable files
                }
            }
        }
    }

    // todo: close tabs method

    private boolean isFileModified(FileDocumentManager documentManager, VirtualFile virtualFile) {
        Document document = documentManager.getDocument(virtualFile);
        return document != null && documentManager.isDocumentUnsaved(document);
    }

    private boolean wasRecentlyEdited(FileDocumentManager documentManager, VirtualFile virtualFile, int minutes) {
        Document document = documentManager.getDocument(virtualFile);

        if (document == null) {
            return false;
        }

        long modificationStamp = document.getModificationStamp();
        // If document has been modified, consider it recently edited.
        // This is a simplified check, todo: track actual times
        return documentManager.isDocumentUnsaved(document) || modificationStamp > 0;
    }
}
