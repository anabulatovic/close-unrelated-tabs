package com.anabulatovic.closeunrelatedtabs;

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

    }

    private State state = new State();

    @Override
    public CloseUnrelatedTabsSettings.State getState() {
        return state;
    }

    @Override
    public void loadState(@NotNull State state) {
        this.state = state;
    }
}
