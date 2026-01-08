package com.anabulatovic.closeunrelatedtabs;

import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class CloseUnrelatedTabsConfigurable implements Configurable {

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "";
    }

    @Override
    public @Nullable JComponent createComponent() {
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
