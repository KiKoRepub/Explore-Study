package org.deepseek.service.impl;

import org.deepseek.service.MilvusService;
import org.deepseek.service.VectorStoreService;
import org.deepseek.utils.LoggerUtils;
import org.deepseek.utils.VectorStoreUtils;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MilvusVectorStoreService extends MilvusService implements VectorStoreService {

    @Autowired
    private VectorStore vectorStore;

    @Override
    public boolean addVectorStore() {
        try {
            List<Document> documents = VectorStoreUtils.getDocumentList("knowledge/杭州凤凰中心附近餐饮推荐.md");

            documents.forEach(document -> {
                vectorStore.add(List.of(new Document(document.getFormattedContent())));
            });


            return true;
        }catch (Exception e){
            e.printStackTrace();
            LoggerUtils.error(e,"添加文档数据失败");
            return false;
        }

    }

    @Override
    public List<Document> searchVector(String message, int topK) {
        return vectorStore.similaritySearch(SearchRequest.builder()
                .query(message)
                .topK(topK)
                .build());
    }
}
