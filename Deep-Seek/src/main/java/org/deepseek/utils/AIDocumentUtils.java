package org.deepseek.utils;

import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.reader.JsonReader;
import org.springframework.ai.reader.TextReader;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.core.io.PathResource;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>RAG 中 文档读取的工具类</p>
 * 利用 SpringAI 自带的文档阅读类，自动转换成 AI 能识别的 文档
 */
public class AIDocumentUtils {

    public static List<Document> loadTextDocuments(String documentsPath){

        PathResource resource = new PathResource(documentsPath);
        TextReader textReader = new TextReader(resource);
        return textReader.get();

    }
    public static List<Document> loadJsonDocuments(String documentsPath){
        PathResource resource = new PathResource(documentsPath);
        JsonReader jsonReader = new JsonReader(resource);
        return  jsonReader.get();
    }
}
