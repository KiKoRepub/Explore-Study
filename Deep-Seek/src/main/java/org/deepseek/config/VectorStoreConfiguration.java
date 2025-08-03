package org.deepseek.config;

import com.baomidou.dynamic.datasource.provider.DynamicDataSourceProvider;
import com.zaxxer.hikari.util.DriverDataSource;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.embedding.BatchingStrategy;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.observation.VectorStoreObservationConvention;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.ai.vectorstore.pgvector.autoconfigure.PgVectorStoreProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
public class VectorStoreConfiguration {


    @Autowired
    DynamicDataSourceProvider provider;

    @Bean
    public PgVectorStore vectorStore(EmbeddingModel embeddingModel, PgVectorStoreProperties properties, ObjectProvider<ObservationRegistry> observationRegistry, ObjectProvider<VectorStoreObservationConvention> customObservationConvention, BatchingStrategy batchingStrategy){

        // 补充一个 JdbcTemplate
        JdbcTemplate jdbcTemplate = new JdbcTemplate(provider.loadDataSources().get("postgresql"));


        // 来自源码
        boolean initializeSchema = properties.isInitializeSchema();
        return ((PgVectorStore.PgVectorStoreBuilder)((PgVectorStore.PgVectorStoreBuilder)((PgVectorStore.PgVectorStoreBuilder)PgVectorStore.builder(jdbcTemplate, embeddingModel).schemaName(properties.getSchemaName()).idType(properties.getIdType()).vectorTableName(properties.getTableName()).vectorTableValidationsEnabled(properties.isSchemaValidation()).dimensions(properties.getDimensions()).distanceType(properties.getDistanceType()).removeExistingVectorStoreTable(properties.isRemoveExistingVectorStoreTable()).indexType(properties.getIndexType()).initializeSchema(initializeSchema).observationRegistry((ObservationRegistry)observationRegistry.getIfUnique(() -> {
            return ObservationRegistry.NOOP;
        }))).customObservationConvention((VectorStoreObservationConvention)customObservationConvention.getIfAvailable(() -> {
            return null;
        }))).batchingStrategy(batchingStrategy)).maxDocumentBatchSize(properties.getMaxDocumentBatchSize()).build();
    }
}
