package org.deepseek.utils;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.core.io.ClassPathResource;

import java.util.List;

/**
 * RAG流程中的向量存储部分
 * 利用 SpringAI 自带的向量存储类，
 * 轻松将文档转换成想了并保存到向量数据库中
 */
public class VectorStoreUtils {

    private static final int CHUNK_SIZE = 10; // 每个块的目标大小
    private static final int MIN_CHUNK_SIZE_CHARS = 100; // 最小块字符数
    private static final int MIN_CHUNK_LENGTH_TO_EMBED = 10;// 嵌入的最小块长度
    private static final int MAX_NUM_CHUNKS = 400;// 最大块数量

    /**
 * 从classpath中加载文档并进行分块处理
 * @param documentClassPath 文档在classpath中的路径
 * @return 分块后的文档列表
 */
public static List<Document> getDocumentList(String documentClassPath) {

    // 从classpath加载文档资源
    ClassPathResource documentation = new ClassPathResource(documentClassPath);

    // 使用Tika解析器读取文档内容
    TikaDocumentReader tikaDocumentReader = new TikaDocumentReader(documentation);

    // 创建文本分割器，用于将文档切分为多个块
    TokenTextSplitter splitter = new TokenTextSplitter(
        CHUNK_SIZE,
        MIN_CHUNK_SIZE_CHARS,
        MIN_CHUNK_LENGTH_TO_EMBED,
        MAX_NUM_CHUNKS,
        true                          // 是否保留原始文档结构
    );

    // 对文档进行分块处理并返回结果
    List<Document> documents = splitter.apply(tikaDocumentReader.get());


    return documents;
}


}
