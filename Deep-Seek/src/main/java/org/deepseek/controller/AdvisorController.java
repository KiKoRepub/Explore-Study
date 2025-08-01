package org.deepseek.controller;

import com.alibaba.fastjson.JSONObject;
import org.deepseek.advisor.ReReadAdvisor;
import org.deepseek.strategy.AdvisorStrategy;
import org.jetbrains.annotations.NotNull;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.VectorStoreChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.preretrieval.query.transformation.CompressionQueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.TranslationQueryTransformer;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/advisor")
public class AdvisorController extends AIController {

    @Autowired
    private ChatMemory chatMemory;

    @Autowired
    private VectorStore vectorStore;

    private static final String DEFAULT_CONVERSATION_ID = "007";

    private static final String DEFAULT_FORBID_ERROR = "问题违规了哦，不能回答，换个话题试试吧";

    public AdvisorController(OpenAiChatModel openAiChatModel) {
        super(openAiChatModel);
    }

    @PostMapping("/")
    @NotNull
    private static Advisor getConcreteAdvisor(Integer number) {
        switch (number) {
            case 1 -> {
                return AdvisorStrategy.MESSAGE_CHAT_MEMORY_ADVISOR;
            }
            default -> {
                return AdvisorStrategy.LOGGING_ADVISOR;
            }
        }

    }

    @PostMapping("/prompt-push")
    public ResponseEntity<String> promptPush(@RequestParam("prompt") String prompt,
                                             @RequestParam("message") String message) {
//        SystemPromptTemplate


        String content = chatClient.prompt(prompt)
                .advisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, DEFAULT_CONVERSATION_ID))
                .user(message)
                .call()
                .content();

        System.out.println(content);
        return ResponseEntity.ok(content);

    }

    //    菜品
    @PostMapping("/prompt-push/cook")
    public ResponseEntity<String> promptCookPush(@RequestParam("dish_name") String dishName,
                                                 @RequestParam("message") String message) {
        String templateStr = """
                接下来询问的是 菜品：{dish_name}的问题，请根据相关知识，正确地回复用户
                """;


        PromptTemplate promptTemplate = SystemPromptTemplate.builder()
                .template(templateStr)
                .build();
        Prompt prompt = promptTemplate.create(Map.of("dish_name", dishName));


        String content = chatClient.prompt(prompt)
                .advisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, DEFAULT_CONVERSATION_ID))
                .user(message)
                .call()
                .content();

        System.out.println(content);

        return ResponseEntity.ok(content);

    }

    // 设置 禁止回答的内容
    @PostMapping("/forbid-push")
    public ResponseEntity<String> forbidPush(@RequestParam("forbid") String forbid,
                                             @RequestParam("message") String message) {
        String content = chatClient.prompt()
                .advisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .advisors(new SafeGuardAdvisor(List.of(forbid), DEFAULT_FORBID_ERROR, 0))
                .user(message)
                .call()
                .content();
        System.out.println(content);
        return ResponseEntity.ok(content);

    }

    // 结合 知识库
    @PostMapping("/vector-push")
    public ResponseEntity<String> vectorPush(@RequestParam("message") String message) {

        String content = chatClient.prompt()
                .advisors(VectorStoreChatMemoryAdvisor.builder(vectorStore).build())
                .user(message)
                .call()
                .content();

        System.out.println(content);

        return ResponseEntity.ok(content);
    }

    @PostMapping("/vector-push/question")
    public ResponseEntity<String> vectorQuestionPush(@RequestParam("user_context") String userContext,
                                                     @RequestParam("message") String message) {

        // 向 vectorStore 中添加 用户上下文
        List<Document> documents = List.of(new Document(userContext));
        vectorStore.add(documents);


        // 这个 question_answer_context 是 QuestionAnswerAdvisor 中会自动添加的 上下文提示内容
        // 这个上下文提示内容可能会包含 用户输入的上下文(前提是 存入了vectorStore )
        PromptTemplate template = PromptTemplate.builder()
                .template("请根据提示回答用户的问题，提示内容为：{question_answer_context}")
                .build();

        String content = chatClient.prompt()
                .advisors(QuestionAnswerAdvisor.builder(vectorStore)
                        .promptTemplate(template)
                        .build())
                .user(message)
                .call()
                .content();

        System.out.println(content);

        return ResponseEntity.ok(content);

    }


    // 自定义 Advisor
    @PostMapping("/reread-push")
    public ResponseEntity<String> rereadPush(@RequestParam("message") String message) {
        String content = chatClient.prompt()
                .advisors(new ReReadAdvisor())
                .user(message)
                .call()
                .content();

        System.out.println(content);

        return ResponseEntity.ok(content);
    }


    // 检索增强
    @PostMapping("/retrieve-push")
    public ResponseEntity<String> retrievePush(@RequestParam("message") String message) {
        Advisor advisor = RetrievalAugmentationAdvisor.builder()
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                        .similarityThreshold(0.50)
                        .vectorStore(vectorStore)
                        .build())
                .build();
        String content = chatClient.prompt()
                .advisors(advisor)
                .user(message)
                .call()
                .content();
        System.out.println(content);

        return ResponseEntity.ok(content);
    }

    @PostMapping("/retrieve-push/query")
    public ResponseEntity<String> retrieveQueryPush(@RequestParam("message") String message) {
        String templateStr = """
                You are a helpful assistant that answers questions about the following documents:
                {documents}
                                
                {query}
                """;
        PromptTemplate template = PromptTemplate.builder()
                .template(templateStr)
                .build();

        Advisor advisor = RetrievalAugmentationAdvisor.builder()
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                        .similarityThreshold(0.50)
                        .vectorStore(vectorStore)
                        .build())
                .queryAugmenter(ContextualQueryAugmenter.builder()
                        .allowEmptyContext(true)
                        .promptTemplate(template)
                        .build())
                .build();

        String content = chatClient.prompt()
                .advisors(advisor)
                .user(message)
                .call()
                .content();

        System.out.println(content);

        return ResponseEntity.ok(content);
    }

    @PostMapping("/retrieve-push/time")
    public ResponseEntity<String> retrieveTimePush(@RequestParam(value = "create_time", required = false, defaultValue = "2025-01-01") String beginTime,
                                                   @RequestParam("message") String message) {
        Advisor advisor = RetrievalAugmentationAdvisor.builder()
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                        .similarityThreshold(0.70)
                        .vectorStore(vectorStore)
                        .build())
                .build();


        String templateStr = "creativetime == '${createTime}'";

        String content = chatClient.prompt()
                .advisors(advisor)
                .advisors(a -> a.param(VectorStoreDocumentRetriever.FILTER_EXPRESSION, templateStr.replace("${createTime}", beginTime)))
                .user(message)
                .call()
                .content();

        System.out.println(content);

        return ResponseEntity.ok(content);


    }

    @PostMapping("/retrieve-push/transformer")
    public ResponseEntity<String> retrieveTransformerPush(@RequestParam("message") String message) {
        Advisor advisor = RetrievalAugmentationAdvisor.builder()
                .queryTransformers(RewriteQueryTransformer.builder()
                        .chatClientBuilder(chatClient.mutate())
                        .build())
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                        .similarityThreshold(0.50)
                        .vectorStore(vectorStore)
                        .build())
                .build();

        String content = chatClient.prompt()
                .advisors(advisor)
                .user(message)
                .call()
                .content();

        System.out.println(content);
        return ResponseEntity.ok(content);


    }


    @PostMapping("/retrieve-push/document")
    public ResponseEntity<String> retrieveDocumentPush(@RequestParam("message") String message) {

        DocumentRetriever retriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .similarityThreshold(0.73)
                .topK(5)
                .filterExpression(new FilterExpressionBuilder()
                        .eq("createtime", "2023-01-01")
                        .build())
                .build();
        List<Document> documents = retriever.retrieve(new Query(message));

        String result = documents.stream().map(document -> document.getText())
                .collect(Collectors.joining(";"));
        return ResponseEntity.ok(result);
    }


    @PostMapping("/queryTransformer")
    public ResponseEntity<String> queryTransformer(@RequestParam("message") String message) {

        String text = "And what is its second largest city?";
        JSONObject obj = new JSONObject();
        obj.put("origin-text:", text);
        // 给出模拟的历史数据
        Query query = Query.builder()
                .text(text)
                .history(new UserMessage("What is the capital of Denmark?"),
                        new AssistantMessage("Copenhagen is the capital of Denmark."))
                .build();

//        对数据进行转换
        QueryTransformer queryTransformer = RewriteQueryTransformer.builder()
                .chatClientBuilder(chatClient.mutate())
                .build();
        Query transformedQuery = queryTransformer.transform(query);
        text = transformedQuery.text();


        // 写入重写的分析过程
        obj.put("RewriteQueryTransformer:", text);

        queryTransformer = CompressionQueryTransformer.builder()
                .chatClientBuilder(chatClient.mutate())
                .build();

        // 写入 针对上下文拓展压缩后的 内容
        transformedQuery = queryTransformer.transform(query);
        text = transformedQuery.text();
        obj.put("CompressionQueryTransformer:", text);

        queryTransformer = TranslationQueryTransformer.builder()
                .chatClientBuilder(chatClient.mutate())
                .targetLanguage("中文")
                .build();

        // 写入 被翻译后的 内容
        transformedQuery = queryTransformer.transform(query);
        text = transformedQuery.text();
        obj.put("TranslationQueryTransformer:", text);

        String result = obj.toString();

        return ResponseEntity.ok(result);
    }

    String aaa = """
            **Rewritten query:**
                Second largest city in [country/region]?
            **Explanation:**
              - Removed the vague pronoun (its)
                 and replaced (it) with a placeholder for specificity
                    (e.g., "France,"California").
              - Made the query concise and direct, focusing only on the core intent
                (ranking cities by population).
              - Assumes the user will fill in the bracketed term with the relevant location.
                If the context is already known (e.g., from prior conversation),
                replace [country/region] with the actual name
                    (e.g., Second largest city in Canada?).
            **Alternative (if context is unclear):**
                List the largest cities in [country/region] by population.
            *(This retrieves a ranked list, allowing the user to identify the second largest.)*
            """;

}
