package com.anabulatovic.closeunrelatedtabs;

import com.intellij.openapi.options.Configurable;
import com.intellij.ui.JBIntSpinner;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class CloseUnrelatedTabsConfigurable implements Configurable {

    private JBCheckBox showConfirmationDialogCheckBox;
    private JBCheckBox keepModifiedTabsCheckBox;
    private JBCheckBox keepCorrespondingTestFilesCheckBox;
    private JBCheckBox keepRecentlyEditedTabsCheckBox;
    private JBIntSpinner minimumTabsToKeepSpinner;
    private JBIntSpinner referenceDepthSpinner;
    private JBIntSpinner recentlyEditedMinutesSpinner;

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

        keepRecentlyEditedTabsCheckBox = new JBCheckBox(
                MessageBundle.message("settings.show.confirmation.dialog"),
                settings.isKeepRecentlyEditedTabs()
        );

        recentlyEditedMinutesSpinner = new JBIntSpinner(settings.getRecentlyEditedMinutes(), 1, 120);

        minimumTabsToKeepSpinner = new JBIntSpinner(settings.getMinimumTabsToKeepOpen(), 0, 100);
        referenceDepthSpinner = new JBIntSpinner(settings.getReferenceDepth(), 1, 10);

        return FormBuilder.createFormBuilder()
                .addComponent(showConfirmationDialogCheckBox)
                .addComponent(keepModifiedTabsCheckBox)
                .addComponent(keepCorrespondingTestFilesCheckBox)
                .getPanel();
    }

    @Override
    public boolean isModified() {
        CloseUnrelatedTabsSettings settings = CloseUnrelatedTabsSettings.getInstance();

        if (showConfirmationDialogCheckBox.isSelected() != settings.isShowConfirmationDialog()) return true;
        if (keepModifiedTabsCheckBox.isSelected() != settings.isKeepModifiedTabs()) return true;
        if (keepCorrespondingTestFilesCheckBox.isSelected() != settings.isKeepCorrespondingTestFiles()) return true;
        if (keepRecentlyEditedTabsCheckBox.isSelected() != settings.isKeepRecentlyEditedTabs()) return true;
        if ((Integer) recentlyEditedMinutesSpinner.getValue() != settings.getRecentlyEditedMinutes()) return true;
        if ((Integer) minimumTabsToKeepSpinner.getValue() != settings.getMinimumTabsToKeepOpen()) return true;
        if ((Integer) referenceDepthSpinner.getValue() != settings.getReferenceDepth()) return true;

        return false;
    }

    @Override
    public void apply() {
        CloseUnrelatedTabsSettings settings = CloseUnrelatedTabsSettings.getInstance();

        settings.setShowConfirmationDialog(showConfirmationDialogCheckBox.isSelected());
        settings.setKeepModifiedTabs(keepModifiedTabsCheckBox.isSelected());
        settings.setKeepCorrespondingTestFiles(keepCorrespondingTestFilesCheckBox.isSelected());
        settings.setKeepRecentlyEditedTabs(keepRecentlyEditedTabsCheckBox.isSelected());
        settings.setRecentlyEditedMinutes((Integer) recentlyEditedMinutesSpinner.getValue());
        settings.setMinimumTabsToKeepOpen((Integer) minimumTabsToKeepSpinner.getValue());
        settings.setReferenceDepth((Integer) referenceDepthSpinner.getValue());
    }

    @Override
    public void reset() {
        CloseUnrelatedTabsSettings settings = CloseUnrelatedTabsSettings.getInstance();

        showConfirmationDialogCheckBox.setSelected(settings.isShowConfirmationDialog());
        keepModifiedTabsCheckBox.setSelected(settings.isKeepModifiedTabs());
        keepCorrespondingTestFilesCheckBox.setSelected(settings.isKeepCorrespondingTestFiles());
        keepRecentlyEditedTabsCheckBox.setSelected(settings.isKeepRecentlyEditedTabs());
        recentlyEditedMinutesSpinner.setValue(settings.getRecentlyEditedMinutes());
        minimumTabsToKeepSpinner.setValue(settings.getMinimumTabsToKeepOpen());
        referenceDepthSpinner.setValue(settings.getReferenceDepth());
    }
}
