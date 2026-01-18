package com.anabulatovic.closeunrelatedtabs;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

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
        public boolean keepCorrespondingTestFiles = true;
        public boolean keepRecentlyEditedTabs = false;
        public int recentlyEditedMinutes = 10;
        public List<String> excludePatterns = new ArrayList<>();
        public boolean showPreviewBeforeClosing = false;
        public boolean keepPinnedTabs = true;
    }

    private State state = new State();

    public static CloseUnrelatedTabsSettings getInstance() {
        return ApplicationManager.getApplication().getService(CloseUnrelatedTabsSettings.class);
    }

    @Override
    public @Nullable CloseUnrelatedTabsSettings.State getState() {
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

    public boolean isKeepCorrespondingTestFiles() {
        return state.keepCorrespondingTestFiles;
    }

    public void setKeepCorrespondingTestFiles(boolean shouldKeep) {
        state.keepCorrespondingTestFiles = shouldKeep;
    }

    public boolean isKeepRecentlyEditedTabs() {
        return state.keepRecentlyEditedTabs;
    }

    public void setKeepPinnedTabs(boolean shouldKeep) {
        state.keepPinnedTabs = shouldKeep;
    }

    public boolean isKeepPinnedTabs() {
        return state.keepPinnedTabs;
    }

    public void setKeepRecentlyEditedTabs(boolean shouldKeep) {
        state.keepCorrespondingTestFiles = shouldKeep;
    }

    public int getRecentlyEditedMinutes() {
        return state.recentlyEditedMinutes;
    }

    public void setRecentlyEditedMinutes(int recentlyEditedMinutes) {
        state.recentlyEditedMinutes = recentlyEditedMinutes;
    }

    public boolean isShowPreviewBeforeClosing() {
        return state.showPreviewBeforeClosing;
    }

    public void setShowPreviewBeforeClosing(boolean shouldShowPreviewBeforeClosing) {
        state.showPreviewBeforeClosing = shouldShowPreviewBeforeClosing;
    }

    public List<String> getExcludePatterns() {
        return state.excludePatterns;
    }

    public void setExcludePatterns(List<String> patterns) {
        state.excludePatterns = patterns;
    }

}
