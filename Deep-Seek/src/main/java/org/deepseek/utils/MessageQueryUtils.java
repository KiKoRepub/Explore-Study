package org.deepseek.utils;

import org.springframework.ai.chat.client.ChatClient;
import java.util.List;

public class MessageQueryUtils {

//    /**
//     *  重写提示词
//     * @param prompt 需要重写的 提示词
//     * @param builder 用来重写提示词的model对应的builder
//     * @return 重写后的提示词
//     */
//    public String doQueryRewrite(String prompt, ChatClient.Builder builder){
//
//        QueryTransformer queryTransformer = RewriteQueryTransformer.builder()
//                .chatClientBuilder(builder)
//                .build();
//        Query query = new Query(prompt);
//
//        Query transformedQuery = queryTransformer.transform(query);
//
//        return transformedQuery.text();
//    }
//
//    /**
//     *  扩展提示词
//     * @param prompt 需要扩展的提示词
//     * @param needNums 需要扩展的个数
//     * @param builder 用来扩展提示词的model对应的builder
//     * @return 扩展后的提示词查询序列
//     */
//    public List<Query> doQueryExpand(String prompt,Integer needNums,ChatClient.Builder builder){
//        MultiQueryExpander queryExpander = MultiQueryExpander.builder()
//                .chatClientBuilder(builder)
//                .numberOfQueries(needNums)
//                .build();
//        return  queryExpander.expand(new Query(prompt));
//    }

}
