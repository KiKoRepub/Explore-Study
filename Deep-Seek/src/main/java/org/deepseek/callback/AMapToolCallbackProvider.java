package org.deepseek.callback;

import org.jetbrains.annotations.NotNull;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;


public class AMapToolCallbackProvider implements ToolCallbackProvider {

    private final ToolCallback[] toolCallbacks;

    @NotNull
    @Override
    public ToolCallback[] getToolCallbacks() {
        return this.toolCallbacks;
    }

    public AMapToolCallbackProvider(ToolCallback[] toolCallbacks){
        this.toolCallbacks = toolCallbacks;
    }
}
