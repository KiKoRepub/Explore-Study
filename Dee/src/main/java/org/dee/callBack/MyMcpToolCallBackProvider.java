package org.dee.callBack;

import org.jetbrains.annotations.NotNull;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;

public class MyMcpToolCallBackProvider implements ToolCallbackProvider {

    private final ToolCallback[] toolCallbacks;
    private final int callBackNums;
    public MyMcpToolCallBackProvider(ToolCallback[] toolCallbacks){
        this.toolCallbacks = toolCallbacks;
        callBackNums = toolCallbacks.length;
    }
    @NotNull
    @Override
    public ToolCallback[] getToolCallbacks() {
        return this.toolCallbacks;
    }


    public int getCallBackNums() {
        return callBackNums;
    }
}
