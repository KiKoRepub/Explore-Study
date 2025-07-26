package org.deepseek.utils;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;

import java.util.List;

/**
 * RAG流程中的向量存储部分
 * 利用 SpringAI 自带的向量存储类，
 * 轻松将文档转换成想了并保存到向量数据库中
 */
public class VectorStoreUtils {


    public SimpleVectorStore doVectorStore(String documentPath,EmbeddingModel model){
        SimpleVectorStore vectorStore = SimpleVectorStore.builder(model).build();
        List<Document> documents = AIDocumentUtils.loadTextDocuments(documentPath);
        vectorStore.add(documents);
        return vectorStore;
    }

}
