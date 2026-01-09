package com.anabulatovic.closeunrelatedtabs;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;

@Service(Service.Level.APP)
@State(
      name = "CloseUnrelatedTabsSettings",
      storages = @Storage("CloseUnrelatedTabsSettings.xml")
)
public final class CloseUnrelatedTabsSettings implements PersistentStateComponent<CloseUnrelatedTabsSettings.State> {

    public static class State {
        public boolean showConfirmationDialog = true;
        public boolean keepModifiedTabs = true;
        public int minimumTabsToKeep = 1;
        public int referenceDepth = 1;
    }

    private State state = new State();

    public static CloseUnrelatedTabsSettings getInstance() {
        return ApplicationManager.getApplication().getService(CloseUnrelatedTabsSettings.class);
    }

    @Override
    public CloseUnrelatedTabsSettings.State getState() {
        return state;
    }

    @Override
    public void loadState(@NotNull State state) {
        this.state = state;
    }

    public boolean isShowConfirmationDialog() {
        return state.showConfirmationDialog;
    }

    public void setShowConfirmationDialog(boolean shouldShow) {
        state.showConfirmationDialog = shouldShow;
    }

    public boolean isKeepModifiedTabs() {
        return state.keepModifiedTabs;
    }

    public void setKeepModifiedTabs(boolean shouldKeep) {
        state.keepModifiedTabs = shouldKeep;
    }

    public int getMinimumTabsToKeepOpen() {
        return state.minimumTabsToKeep;
    }

    public void setMinimumTabsToKeepOpen(int minimumTabsToKeepOpen) {
        state.minimumTabsToKeep = minimumTabsToKeepOpen;
    }

    public int getReferenceDepth() {
        return state.referenceDepth;
    }

    public void setReferenceDepth(int referenceDepth) {
        state.referenceDepth = referenceDepth;
    }

}
