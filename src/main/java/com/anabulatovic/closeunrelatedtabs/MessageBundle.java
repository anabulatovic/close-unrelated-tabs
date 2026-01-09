package com.anabulatovic.closeunrelatedtabs;

import com.intellij.DynamicBundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

public final class MessageBundle {
    private static final String BUNDLE = "messages.MessageBundle";
    private static final DynamicBundle INSTANCE = new DynamicBundle(MessageBundle.class, BUNDLE);

    @NotNull
    @Nls
    public static String message(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key, Object... params) {
        return INSTANCE.getMessage(key, params);
    }
}
