package org.deepseek.controller;

import lombok.AllArgsConstructor;
import org.deepseek.service.MessageService;
import org.deepseek.service.VectorStoreService;
import org.deepseek.utils.VectorStoreUtils;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.milvus.MilvusVectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import io.milvus.client.MilvusServiceClient;
import io.milvus.param.ConnectParam;
import io.milvus.param.collection.DropCollectionParam;
import io.milvus.param.R;
import io.milvus.param.RpcStatus;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/*
* RAG
*   准备部分： 创建向量存储，向量存储中添加数据，创建索引
*       1. 分片
*       2. 向量化
*       3. 索引
*   回答部分： 获取用户问题，向向量存储中搜索最相似的文档，将文档内容作为提示，将用户问题作为输入，生成答案
*       1. 召回
*       2. 重排
*       3. 生成
* */
@RestController
@RequestMapping( "/vector")
public class VectorStoreController extends AIController {
    @Autowired
    private VectorStoreService vectorStoreService;





    @PostMapping("/add")
    public void addVectorStore() {
        vectorStoreService.addVectorStore();
    }



    @GetMapping("/chat")
    public String chatVector(@RequestParam("message")String message){
        List<Document> documents = vectorStoreService.searchVector(message,1);

        Prompt chatPrompt = messageService.createRAGPrompt(message, documents, 1);

        ChatResponse response = chatClient.prompt(chatPrompt).call().chatResponse();

        String result = response.getResult().getOutput().getText();

        System.out.println("result = " + result);
        return result;
    }
}