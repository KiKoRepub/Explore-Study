import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Flux;

import java.util.List;

public class JsonToFluxConverter {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    public Flux<ChatResponse> convertJsonToFlux(String jsonArray) {
        try {
            // 解析 JSON 数组为 List<ChatResponse>
            List<ChatResponse> responses = objectMapper.readValue(
                jsonArray,
                new TypeReference<List<ChatResponse>>() {}
            );
            
            // 转换为 Flux
            return Flux.fromIterable(responses);
            
        } catch (Exception e) {
            return Flux.error(new RuntimeException("JSON 转换失败", e));
        }
    }
    
    // 使用方法示例
    public static void main(String[] args) {
        String json = "[{...}, {...}]"; // 您的 JSON 字符串
        
        JsonToFluxConverter converter = new JsonToFluxConverter();
        Flux<ChatResponse> flux = converter.convertJsonToFlux(json);
        
        // 处理 Flux
        flux.subscribe(response -> {
            // 访问响应数据
            if (response.getResult() != null && response.getResult().getOutput() != null) {
                System.out.println("响应文本: " + response.getResult().getOutput().getText());
            }
            
            if (response.getMetadata() != null && response.getMetadata().getUsage() != null) {
                System.out.println("Token 使用: " + response.getMetadata().getUsage().getTotalTokens());
            }
        });
    }
}