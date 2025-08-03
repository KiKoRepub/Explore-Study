package org.deepseek.controller;

import lombok.AllArgsConstructor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping( "/vector")
public class VectorStoreController extends AIController {
    @Autowired
    private VectorStore vectorStore;

    public  static Map<String,List<Document>> vectorStoreMap = new HashMap<>();


    @PostMapping("/add")
    public void addVectorStore() {

        // 使用 TikaDocumentReader 对文档内容 进行读取，
        // 并 使用 TokenTextSplitter
        File documentation = new File( "杭州凤凰中心附近餐饮推荐.md" );
        TikaDocumentReader tikaDocumentReader = new TikaDocumentReader(new FileSystemResource(documentation));
        TokenTextSplitter splitter = new TokenTextSplitter(100,200,10,400,
                true);
        List<Document> documents = splitter.apply(tikaDocumentReader.get());
        List<Document> vectorList = new ArrayList<>();
        documents.forEach( document -> {

//            vectorStoreMap.put("杭州凤凰中心附近餐饮推荐", List.of(new Document(document.getFormattedContent())));
            vectorStore.add(List.of(new Document(document.getFormattedContent())));
        });

        vectorStoreMap.forEach((k,v) -> {
            System.out.println(k + ":  " + v);
        });
    }

    @GetMapping("/chat")
    public String chatVector(@RequestParam("message")String message){
        List<Document> documents = vectorStore.similaritySearch(SearchRequest.builder()
                .query(message)
                .topK(1)
                .build());

        String prompt = """
                你是一个智能助手，你可以根据下面搜索到的内容回复用户
                ### 用户的问题是
                %s
                ### 搜索到的内容是
                %s
               """;
        prompt = String.format(prompt,message,documents.get(0).getText());

        Prompt chatPrompt = new Prompt(prompt);

        ChatResponse response = chatClient.prompt(chatPrompt).call().chatResponse();

        String result = response.getResult().getOutput().getText();

        System.out.println("result = " + result);
        return result;
    }
}