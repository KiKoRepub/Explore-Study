import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public class ChatResponse {
    private Result result;
    private Metadata metadata;
    private List<Result> results;

    // Getters and setters
    public Result getResult() { return result; }
    public void setResult(Result result) { this.result = result; }
    
    public Metadata getMetadata() { return metadata; }
    public void setMetadata(Metadata metadata) { this.metadata = metadata; }
    
    public List<Result> getResults() { return results; }
    public void setResults(List<Result> results) { this.results = results; }

    public static class Result {
        private Output output;
        private ResultMetadata metadata;
        private boolean empty;

        // Getters and setters
        public Output getOutput() { return output; }
        public void setOutput(Output output) { this.output = output; }
        
        public ResultMetadata getMetadata() { return metadata; }
        public void setMetadata(ResultMetadata metadata) { this.metadata = metadata; }
        
        public boolean isEmpty() { return empty; }
        public void setEmpty(boolean empty) { this.empty = empty; }
    }

    public static class Output {
        private String messageType;
        private OutputMetadata metadata;
        private List<Object> toolCalls;
        private List<Object> media;
        private String text;

        // Getters and setters
        public String getMessageType() { return messageType; }
        public void setMessageType(String messageType) { this.messageType = messageType; }
        
        public OutputMetadata getMetadata() { return metadata; }
        public void setMetadata(OutputMetadata metadata) { this.metadata = metadata; }
        
        public List<Object> getToolCalls() { return toolCalls; }
        public void setToolCalls(List<Object> toolCalls) { this.toolCalls = toolCalls; }
        
        public List<Object> getMedia() { return media; }
        public void setMedia(List<Object> media) { this.media = media; }
        
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
    }

    public static class OutputMetadata {
        private String finishReason;
        private String refusal;
        private int index;
        private String id;
        private String role;
        private String messageType;

        // Getters and setters
        public String getFinishReason() { return finishReason; }
        public void setFinishReason(String finishReason) { this.finishReason = finishReason; }
        
        public String getRefusal() { return refusal; }
        public void setRefusal(String refusal) { this.refusal = refusal; }
        
        public int getIndex() { return index; }
        public void setIndex(int index) { this.index = index; }
        
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        
        public String getMessageType() { return messageType; }
        public void setMessageType(String messageType) { this.messageType = messageType; }
    }

    public static class ResultMetadata {
        private String finishReason;
        private List<Object> contentFilters;
        private boolean empty;

        // Getters and setters
        public String getFinishReason() { return finishReason; }
        public void setFinishReason(String finishReason) { this.finishReason = finishReason; }
        
        public List<Object> getContentFilters() { return contentFilters; }
        public void setContentFilters(List<Object> contentFilters) { this.contentFilters = contentFilters; }
        
        public boolean isEmpty() { return empty; }
        public void setEmpty(boolean empty) { this.empty = empty; }
    }

    public static class Metadata {
        private String id;
        private String model;
        private RateLimit rateLimit;
        private Usage usage;
        private List<Object> promptMetadata;
        private boolean empty;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
        
        public RateLimit getRateLimit() { return rateLimit; }
        public void setRateLimit(RateLimit rateLimit) { this.rateLimit = rateLimit; }
        
        public Usage getUsage() { return usage; }
        public void setUsage(Usage usage) { this.usage = usage; }
        
        public List<Object> getPromptMetadata() { return promptMetadata; }
        public void setPromptMetadata(List<Object> promptMetadata) { this.promptMetadata = promptMetadata; }
        
        public boolean isEmpty() { return empty; }
        public void setEmpty(boolean empty) { this.empty = empty; }
    }

    public static class RateLimit {
        private int requestsRemaining;
        private String requestsReset;
        private int tokensRemaining;
        private int tokensLimit;
        private int requestsLimit;
        private String tokensReset;

        // Getters and setters
        public int getRequestsRemaining() { return requestsRemaining; }
        public void setRequestsRemaining(int requestsRemaining) { this.requestsRemaining = requestsRemaining; }
        
        public String getRequestsReset() { return requestsReset; }
        public void setRequestsReset(String requestsReset) { this.requestsReset = requestsReset; }
        
        public int getTokensRemaining() { return tokensRemaining; }
        public void setTokensRemaining(int tokensRemaining) { this.tokensRemaining = tokensRemaining; }
        
        public int getTokensLimit() { return tokensLimit; }
        public void setTokensLimit(int tokensLimit) { this.tokensLimit = tokensLimit; }
        
        public int getRequestsLimit() { return requestsLimit; }
        public void setRequestsLimit(int requestsLimit) { this.requestsLimit = requestsLimit; }
        
        public String getTokensReset() { return tokensReset; }
        public void setTokensReset(String tokensReset) { this.tokensReset = tokensReset; }
    }

    public static class Usage {
        private int completionTokens;
        private Map<String, Object> nativeUsage;
        private int promptTokens;
        private int generationTokens;
        private int totalTokens;

        // Getters and setters
        public int getCompletionTokens() { return completionTokens; }
        public void setCompletionTokens(int completionTokens) { this.completionTokens = completionTokens; }
        
        public Map<String, Object> getNativeUsage() { return nativeUsage; }
        public void setNativeUsage(Map<String, Object> nativeUsage) { this.nativeUsage = nativeUsage; }
        
        public int getPromptTokens() { return promptTokens; }
        public void setPromptTokens(int promptTokens) { this.promptTokens = promptTokens; }
        
        public int getGenerationTokens() { return generationTokens; }
        public void setGenerationTokens(int generationTokens) { this.generationTokens = generationTokens; }
        
        public int getTotalTokens() { return totalTokens; }
        public void setTotalTokens(int totalTokens) { this.totalTokens = totalTokens; }
    }
}