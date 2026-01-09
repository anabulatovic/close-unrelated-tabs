package com.anabulatovic.closeunrelatedtabs;

import com.intellij.openapi.options.Configurable;
import com.intellij.ui.JBIntSpinner;
import com.intellij.ui.components.JBCheckBox;
import com.q.L.L.A.J;
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

        return new JComponent() {
        };
    }

    @Override
    public boolean isModified() {
        return true;
    }

    @Override
    public void apply() {
        // impl
    }
}
