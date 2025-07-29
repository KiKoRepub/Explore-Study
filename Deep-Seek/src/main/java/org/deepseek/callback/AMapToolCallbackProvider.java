package org.deepseek.callback;

import org.jetbrains.annotations.NotNull;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("aMapToolCallbackProvider")
public class AMapToolCallbackProvider implements ToolCallbackProvider {
    @Autowired
    @Qualifier("aMapTools")
    private ToolCallback[] toolCallbacks;

    @NotNull
    @Override
    public ToolCallback[] getToolCallbacks() {
        return this.toolCallbacks;
    }


}
