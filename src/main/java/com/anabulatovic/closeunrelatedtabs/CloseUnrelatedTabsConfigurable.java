package com.anabulatovic.closeunrelatedtabs;

import com.intellij.openapi.options.Configurable;
import com.intellij.ui.JBIntSpinner;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBList;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CloseUnrelatedTabsConfigurable implements Configurable {

    private JBCheckBox showConfirmationDialogCheckBox;
    private JBCheckBox keepModifiedTabsCheckBox;
    private JBCheckBox keepCorrespondingTestFilesCheckBox;
    private JBIntSpinner minimumTabsToKeepSpinner;
    private JBIntSpinner referenceDepthSpinner;
    private JBIntSpinner recentlyEditedMinutesSpinner;
    private JBCheckBox showPreviewBeforeClosingCheckBox;
    private DefaultListModel<String> excludePatternsModel;
    private JBList<String> excludePatternsList;
    private JBCheckBox keepPinnedFilesCheckBox;

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return MessageBundle.message("settings.display.name");
    }

    @Override
    public @Nullable JComponent createComponent() {
        CloseUnrelatedTabsSettings settings = CloseUnrelatedTabsSettings.getInstance();

        showConfirmationDialogCheckBox = new JBCheckBox(
                MessageBundle.message("settings.show.confirmation.dialog"),
                settings.isShowConfirmationDialog()
        );

        keepModifiedTabsCheckBox = new JBCheckBox(
                MessageBundle.message("settings.keep.modified.tabs"),
                settings.isKeepModifiedTabs()
        );

        keepCorrespondingTestFilesCheckBox = new JBCheckBox(
                MessageBundle.message("settings.keep.test.files"),
                settings.isKeepCorrespondingTestFiles()
        );

        showPreviewBeforeClosingCheckBox = new JBCheckBox(
                MessageBundle.message("settings.show.preview"),
                settings.isShowPreviewBeforeClosing()
        );

        keepPinnedFilesCheckBox = new JBCheckBox(
                MessageBundle.message("settings.keep.pinned"),
                settings.isKeepPinnedTabs()
        );

        recentlyEditedMinutesSpinner = new JBIntSpinner(settings.getRecentlyEditedMinutes(), 1, 120);

        minimumTabsToKeepSpinner = new JBIntSpinner(settings.getMinimumTabsToKeepOpen(), 0, 100);
        referenceDepthSpinner = new JBIntSpinner(settings.getReferenceDepth(), 1, 10);

        // Exclude patterns list
        excludePatternsModel = new DefaultListModel<>();
        for (String pattern : settings.getExcludePatterns()) {
            excludePatternsModel.addElement(pattern);
        }
        excludePatternsList = new JBList<>(excludePatternsModel);

        JPanel excludePatternsPanel = ToolbarDecorator.createDecorator(excludePatternsList)
                .setAddAction(button -> {
                    String pattern = JOptionPane.showInputDialog(
                            null,
                            MessageBundle.message("settings.exclude.pattern.prompt"),
                            MessageBundle.message("settings.exclude.pattern.title"),
                            JOptionPane.PLAIN_MESSAGE
                    );
                    if (pattern != null && !pattern.trim().isEmpty()) {
                        excludePatternsModel.addElement(pattern.trim());
                    }
                })
                .setRemoveAction(button -> {
                    int selectedIndex = excludePatternsList.getSelectedIndex();
                    if (selectedIndex >= 0) {
                        excludePatternsModel.remove(selectedIndex);
                    }
                })
                .createPanel();
        excludePatternsPanel.setPreferredSize(new Dimension(400, 150));

        //Recently edited panel with checkbox and spinner
        JPanel recentlyEditedPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        recentlyEditedPanel.add(Box.createHorizontalStrut(10));
        recentlyEditedPanel.add(recentlyEditedMinutesSpinner);
        recentlyEditedPanel.add(Box.createHorizontalStrut(5));
        recentlyEditedPanel.add(new JLabel(MessageBundle.message("settings.minutes")));

        return FormBuilder.createFormBuilder()
                .addComponent(new JBLabel("<html><b>" + MessageBundle.message("settings.section.general") + "</b></html>"))
                .addComponent(showConfirmationDialogCheckBox)
                .addComponent(showPreviewBeforeClosingCheckBox)
                .addVerticalGap(10)
                .addComponent(new JLabel("<html><b>" + MessageBundle.message("settings.section.protection") + "</b></html>"))
                .addComponent(keepModifiedTabsCheckBox)
                .addComponent(keepPinnedFilesCheckBox)
                .addComponent(keepCorrespondingTestFilesCheckBox)
                .addComponent(new JLabel("<html><small>" + MessageBundle.message("settings.keep.test.files.hint") + "</small></html>"))
                .addComponent(recentlyEditedPanel)
                .addLabeledComponent(
                        new JLabel(MessageBundle.message("settings.minimum.tabs")),
                        minimumTabsToKeepSpinner
                )
                .addVerticalGap(10)
                .addComponent(new JLabel("<html><b>" + MessageBundle.message("settings.section.scanning") + "</b></html>"))
                .addLabeledComponent(
                        new JLabel(MessageBundle.message("settings.reference.depth")),
                        referenceDepthSpinner
                )
                .addComponent(new JLabel("<html><small>" + MessageBundle.message("settings.reference.depth.hint") + "</b></html>"))
                .addVerticalGap(10)
                .addComponent(new JLabel("<html><b>" + MessageBundle.message("settings.section.exclude") + "</b></html>"))
                .addComponent(new JLabel("<html><small>" + MessageBundle.message("settings.exclude.pattern.hint") + "</small></html>"))
                .addComponent(excludePatternsPanel)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

    @Override
    public boolean isModified() {
        CloseUnrelatedTabsSettings settings = CloseUnrelatedTabsSettings.getInstance();

        if (showConfirmationDialogCheckBox.isSelected() != settings.isShowConfirmationDialog()) return true;
        if (keepModifiedTabsCheckBox.isSelected() != settings.isKeepModifiedTabs()) return true;
        if (keepCorrespondingTestFilesCheckBox.isSelected() != settings.isKeepCorrespondingTestFiles()) return true;
        if (showPreviewBeforeClosingCheckBox.isSelected() != settings.isShowPreviewBeforeClosing()) return true;
        if (keepPinnedFilesCheckBox.isSelected() != settings.isKeepPinnedTabs()) return true;
        if ((Integer) recentlyEditedMinutesSpinner.getValue() != settings.getRecentlyEditedMinutes()) return true;
        if ((Integer) minimumTabsToKeepSpinner.getValue() != settings.getMinimumTabsToKeepOpen()) return true;
        if ((Integer) referenceDepthSpinner.getValue() != settings.getReferenceDepth()) return true;

        List<String> currentPatterns = new ArrayList<>();
        for (int i = 0; i < excludePatternsModel.size(); i++) {
            currentPatterns.add(excludePatternsModel.get(i));
        }
        if (!currentPatterns.equals(settings.getExcludePatterns())) return true;

        return false;
    }

    @Override
    public void apply() {
        CloseUnrelatedTabsSettings settings = CloseUnrelatedTabsSettings.getInstance();

        settings.setShowConfirmationDialog(showConfirmationDialogCheckBox.isSelected());
        settings.setKeepModifiedTabs(keepModifiedTabsCheckBox.isSelected());
        settings.setKeepCorrespondingTestFiles(keepCorrespondingTestFilesCheckBox.isSelected());
        settings.setKeepPinnedTabs(keepPinnedFilesCheckBox.isSelected());
        settings.setRecentlyEditedMinutes((Integer) recentlyEditedMinutesSpinner.getValue());
        settings.setMinimumTabsToKeepOpen((Integer) minimumTabsToKeepSpinner.getValue());
        settings.setReferenceDepth((Integer) referenceDepthSpinner.getValue());
        settings.setShowPreviewBeforeClosing(showPreviewBeforeClosingCheckBox.isSelected());

        List<String> patterns = new ArrayList<>();
        for (int i = 0; i < excludePatternsModel.size(); i++) {
            patterns.add(excludePatternsModel.get(i));
        }
        settings.setExcludePatterns(patterns);
    }

    @Override
    public void reset() {
        CloseUnrelatedTabsSettings settings = CloseUnrelatedTabsSettings.getInstance();

        showConfirmationDialogCheckBox.setSelected(settings.isShowConfirmationDialog());
        keepModifiedTabsCheckBox.setSelected(settings.isKeepModifiedTabs());
        keepCorrespondingTestFilesCheckBox.setSelected(settings.isKeepCorrespondingTestFiles());
        showPreviewBeforeClosingCheckBox.setSelected(settings.isShowPreviewBeforeClosing());
        keepPinnedFilesCheckBox.setSelected(settings.isKeepPinnedTabs());
        recentlyEditedMinutesSpinner.setValue(settings.getRecentlyEditedMinutes());
        minimumTabsToKeepSpinner.setValue(settings.getMinimumTabsToKeepOpen());
        referenceDepthSpinner.setValue(settings.getReferenceDepth());

        excludePatternsModel.clear();
        for (String pattern : settings.getExcludePatterns()) {
            excludePatternsModel.addElement(pattern);
        }
    }
}
