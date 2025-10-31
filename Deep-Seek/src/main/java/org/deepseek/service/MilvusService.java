package org.deepseek.service;

import io.milvus.client.MilvusServiceClient;
import io.milvus.param.ConnectParam;
import io.milvus.param.R;
import io.milvus.param.RpcStatus;
import io.milvus.param.collection.DropCollectionParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.DeleteMapping;

@Service
public class MilvusService {
    @Value("${spring.ai.vectorstore.milvus.client.host}")
    private String milvusHost;

    @Value("${spring.ai.vectorstore.milvus.client.port}")
    private int milvusPort;

    @Value("${spring.ai.vectorstore.milvus.client.username}")
    private String milvusUsername;

    @Value("${spring.ai.vectorstore.milvus.client.password}")
    private String milvusPassword;

    @Value("${spring.ai.vectorstore.milvus.collectionName}")
    private String collectionName;

    private MilvusServiceClient milvusClient;


    /**
     * 删除Milvus集合（用于修复schema不匹配问题）
     * 警告：这会删除所有已存储的向量数据
     * 删除后需要重启应用，Spring AI会自动创建正确的schema
     */
    public String dropCollection() {
        MilvusServiceClient milvusClient = null;
        try {
            // 连接到Milvus
            milvusClient = connectMilvus();

            // 删除集合
            R<RpcStatus> response = milvusClient.dropCollection(
                    DropCollectionParam.newBuilder()
                            .withCollectionName(collectionName)
                            .build()
            );

            if (response.getStatus() == 0) {
                return String.format("✓ 集合 '%s' 已成功删除。请重启应用以让Spring AI自动创建正确的schema。", collectionName);
            } else {
                return String.format("删除失败: %s", response.getMessage());
            }

        } catch (Exception e) {
            return String.format("删除集合时出错: %s", e.getMessage());
        } finally {
            if (milvusClient != null) {
                milvusClient.close();
            }
        }
    }
    private  MilvusServiceClient connectMilvus(){
        if (milvusClient == null) {
            milvusClient = new MilvusServiceClient(
                    ConnectParam.newBuilder()
                            .withHost(milvusHost)
                            .withPort(milvusPort)
                            .withAuthorization(milvusUsername, milvusPassword)
                            .build()
            );
        }

        return milvusClient;
    }
}
