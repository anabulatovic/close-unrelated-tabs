package com.anabulatovic.closeunrelatedtabs;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.impl.EditorWindow;
import com.intellij.openapi.fileEditor.impl.FileEditorManagerImpl;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBScrollPane;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

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

        ProgressManager.getInstance().run(new Task.Backgroundable(project, MessageBundle.message("action.scanning.references"), true) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                indicator.setIndeterminate(false);
                indicator.setFraction(0.0);

                Set<VirtualFile> relatedFiles = new HashSet<>();
                relatedFiles.add(virtualFile); // The clicked file is always related

                ReadAction.run(() -> {
                    PsiManager psiManager = PsiManager.getInstance(project);

                    // Process files at each depth level
                    Set<VirtualFile> currentLevel = new HashSet<>();
                    currentLevel.add(virtualFile);

                    for (int depth = 0; depth < referenceDepth; depth++) {
                        Set<VirtualFile> nextLevel = new HashSet<>();

                        for (VirtualFile file : currentLevel) {
                            PsiFile psiFile = psiManager.findFile(file);
                            if (psiFile == null) continue;

                            indicator.setText(MessageBundle.message("action.scanning.outgoing") + " (depth " + (depth + 1) + ")");
                            indicator.setFraction(0.2 + (0.6 * depth / referenceDepth));

                            // Find files that this file references (outgoing references)
                            Set<VirtualFile> outgoing = new HashSet<>();
                            findOutgoingReferences(psiFile, outgoing);

                            for (VirtualFile vf : outgoing) {
                                if (!relatedFiles.contains(vf)) {
                                    nextLevel.add(vf);
                                    relatedFiles.add(vf);
                                }
                            }

                            indicator.setText(MessageBundle.message("action.scanning.incoming") + " (depth " + (depth + 1) + ")");

                            Set<VirtualFile> incoming = new HashSet<>();
                            findIncomingReferences(psiFile, project, incoming);

                            relatedFiles.addAll(incoming);
                        }

                        currentLevel = nextLevel;
                        if (currentLevel.isEmpty()) break; // No new files found
                    }

                    indicator.setFraction(1.0);
                });

                ApplicationManager.getApplication().invokeLater(() -> closeUnrelatedTabs(project, relatedFiles));
            }
        });
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

    private void closeUnrelatedTabs(Project project, Set<VirtualFile> relatedFiles) {
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
        FileDocumentManager documentManager = FileDocumentManager.getInstance();
        CloseUnrelatedTabsSettings settings = CloseUnrelatedTabsSettings.getInstance();

        VirtualFile[] openFiles = fileEditorManager.getOpenFiles();

        // Build set of test file names to keep if setting is enabled
        Set<String> testFileNamesToKeep = new HashSet<>();
        if (settings.isKeepCorrespondingTestFiles()) {
            for (VirtualFile relatedFile : relatedFiles) {
                testFileNamesToKeep.addAll(getCorrespondingTestFileNames(relatedFile.getNameWithoutExtension()));
            }
        }

        List<VirtualFile> filesToClose = new ArrayList<>();
        for (VirtualFile file : openFiles) {
            if (relatedFiles.contains(file)) {
                continue; // File is related, keep it
            }

            if (settings.isKeepModifiedTabs() && isFileModified(documentManager, file)) {
                continue; // File is modified, keep it
            }

            if (settings.isKeepCorrespondingTestFiles() && isCorrespondingTestFile(file, testFileNamesToKeep)) {
                continue;
            }

            if (settings.isKeepRecentlyEditedTabs() && wasRecentlyEdited(documentManager, file, settings.getRecentlyEditedMinutes())) {
                continue;
            }

            if (matchesExcludePattern(file, settings.getExcludePatterns())) {
                continue;
            }

            if (settings.isKeepPinnedTabs() && isPinned(project, file)) {
                continue;
            }

            filesToClose.add(file);

            int minimumTabs = settings.getMinimumTabsToKeepOpen();
            int tabsToKeep = openFiles.length - filesToClose.size();
            if (tabsToKeep < minimumTabs) {
                int tabsToRemoveFromClosing = minimumTabs - tabsToKeep;
                for (int i = 0; i < tabsToRemoveFromClosing; i++) {
                    // Keep the most recently added candidates
                    filesToClose.removeLast();
                }
            }

            if (filesToClose.isEmpty()) {
                Messages.showInfoMessage(project,
                        MessageBundle.message("dialog.no.tabs.to.close"),
                        MessageBundle.message("dialog.title"));
                return;
            }
        }

        // Show preview dialog if enabled
        if (settings.isShowPreviewBeforeClosing()) {
            PreviewDialog previewDialog = new PreviewDialog(project, filesToClose);
            if (!previewDialog.showAndGet()) {
                return; // user cancelled
            }
            // Get potentially modified list from preview
            filesToClose = previewDialog.getFilesToClose();
            if (filesToClose.isEmpty()) {
                return;
            }
        }

        // Show confirmation dialog if enabled
        if (settings.isShowConfirmationDialog()) {
            ConfirmCloseDialog dialog = new ConfirmCloseDialog(project, filesToClose.size());

            if (!dialog.showAndGet()) {
                return; // user cancelled
            }

            if (dialog.isDontShowAgain()) {
                settings.setShowConfirmationDialog(false);
            }
        }

        for (VirtualFile file : filesToClose) {
            fileEditorManager.closeFile(file);
        }

    }

    private boolean isFileModified(FileDocumentManager documentManager, VirtualFile virtualFile) {
        Document document = documentManager.getDocument(virtualFile);
        if (document == null) {
            return false;
        }

        return document.getModificationStamp() != virtualFile.getModificationStamp();
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

    private boolean isPinned(Project project, VirtualFile file) {
        FileEditorManager manager = FileEditorManager.getInstance(project);

        if (!(manager instanceof FileEditorManagerImpl impl)) {
            return false;
        }

        for (EditorWindow window : impl.getWindows()) {
            if (window.isFilePinned(file)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesExcludePattern(VirtualFile file, List<String> patterns) {
        String fileName = file.getName();
        String filePath = file.getPath();

        for (String pattern : patterns) {
            if (matchesPattern(fileName, pattern) || matchesPattern(filePath, pattern)) {
                return true;
            }
        }

        return false;
    }

    private boolean matchesPattern(String text, String pattern) {
        String regex = pattern
                .replace(".", "\\.")
                .replace("*", ".*")
                .replace("?", ".");
        try {
            return text.matches(regex) || text.matches(".*" + regex);
        } catch (Exception e) {
            return text.contains(pattern.replace("*", "").replace("?", ""));
        }
    }

    private Set<String> getCorrespondingTestFileNames(String baseFileName) {
        Set<String> testNames = new HashSet<>();
        // Common test file naming conventions
        testNames.add(baseFileName + "Test");
        testNames.add(baseFileName + "Tests");
        testNames.add(baseFileName + "Spec");
        testNames.add("Test" + baseFileName);
        testNames.add(baseFileName + "_test");
        testNames.add("test_" + baseFileName);

        // Also handle if the file itself is a test file - keep the source
        if (baseFileName.endsWith("Test") || baseFileName.endsWith("Tests")) {
            testNames.add(baseFileName.replaceAll("Tests?$", ""));
        }

        if (baseFileName.endsWith("Spec")) {
            testNames.add(baseFileName.replaceAll("Spec$", ""));
        }

        if (baseFileName.startsWith("Test")) {
            testNames.add(baseFileName.substring(4));
        }

        if (baseFileName.endsWith("_test")) {
            testNames.add(baseFileName.replace("_test", ""));
        }

        if (baseFileName.startsWith("test_")) {
            testNames.add(baseFileName.substring(5));
        }

        return testNames;
    }

    private boolean isCorrespondingTestFile(VirtualFile virtualFile, Set<String> testFileNamesToKeep) {
        String nameWithoutExtension = virtualFile.getNameWithoutExtension();
        return testFileNamesToKeep.contains(nameWithoutExtension);
    }

    // Preview Dialog
    private static class PreviewDialog extends DialogWrapper {
        private final List<VirtualFile> files;
        private final Map<VirtualFile, JBCheckBox> checkBoxes = new LinkedHashMap<>();

        public PreviewDialog(@Nullable Project project, List<VirtualFile> filesToClose) {
            super(project);
            this.files = new ArrayList<>(filesToClose);
            setTitle(MessageBundle.message("preview.dialog.title"));
            init();
        }

        @Override
        protected @Nullable JComponent createCenterPanel() {
            JPanel panel = new JPanel(new BorderLayout(0, 10));

            JLabel messageLabel = new JLabel(MessageBundle.message("preview.dialog.message", files.size()));
            panel.add(messageLabel, BorderLayout.NORTH);

            JPanel listPanel = new JPanel();
            listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));

            for (VirtualFile file : files) {
                JBCheckBox checkBox = new JBCheckBox(file.getName(), true);
                checkBox.setToolTipText(file.getPath());
                checkBoxes.put(file, checkBox);
                listPanel.add(checkBox);
            }

            JBScrollPane scrollPane = new JBScrollPane(listPanel);
            scrollPane.setPreferredSize(new Dimension(400, 300));
            panel.add(scrollPane, BorderLayout.CENTER);

            // Select all/deselect all buttons
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JButton selectAll = new JButton("Select All");
            JButton deselectAll = new JButton("Deselect All");

            selectAll.addActionListener(e -> checkBoxes.values().forEach(cb -> cb.setSelected(true)));
            deselectAll.addActionListener(e -> checkBoxes.values().forEach(cb -> cb.setSelected(false)));

            buttonPanel.add(selectAll);
            buttonPanel.add(deselectAll);
            panel.add(buttonPanel, BorderLayout.SOUTH);

            return panel;
        }

        public List<VirtualFile> getFilesToClose() {
            List<VirtualFile> result = new ArrayList<>();
            for (Map.Entry<VirtualFile, JBCheckBox> entry : checkBoxes.entrySet()) {
                if (entry.getValue().isSelected()) {
                    result.add(entry.getKey());
                }
            }
            return result;
        }

        @Override
        protected Action @NotNull [] createActions() {
            return new Action[]{getOKAction(), getCancelAction()};
        }
    }

    // Confirmation Dialog
    private static class ConfirmCloseDialog extends DialogWrapper {
       private final int tabCount;
       private JBCheckBox dontShowAgainCheckBox;

       public ConfirmCloseDialog(@Nullable Project project, int tabCount) {
           super(project);
           this.tabCount = tabCount;
           setTitle(MessageBundle.message("dialog.title"));
           init();
       }

       @Override
       protected @Nullable JComponent createCenterPanel() {
           JPanel panel = new JPanel(new BorderLayout(0, 10));

           // Warning message with icon
           JPanel messagePanel = new JPanel(new BorderLayout(10, 0));
           JLabel iconLabel = new JLabel(Messages.getQuestionIcon());
           messagePanel.add(iconLabel, BorderLayout.WEST);

           JLabel messageLabel = new JLabel(MessageBundle.message("dialog.confirm.message", tabCount));
           messagePanel.add(messageLabel, BorderLayout.CENTER);

           panel.add(messagePanel, BorderLayout.NORTH);

           // Don't show this again checkbox
           dontShowAgainCheckBox = new JBCheckBox(MessageBundle.message("dialog.dont.show.again"));
           panel.add(dontShowAgainCheckBox, BorderLayout.SOUTH);

           return panel;
       }

       public boolean isDontShowAgain() {
           return dontShowAgainCheckBox.isSelected();
       }

       @Override
       protected Action @NotNull [] createActions() {
           return new Action[]{getOKAction(), getCancelAction()};
       }
    }
}
