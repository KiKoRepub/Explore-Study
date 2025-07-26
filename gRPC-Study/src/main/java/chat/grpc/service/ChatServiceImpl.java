package chat.grpc.service;
import grpc.chat.ChatGrpc;
import grpc.chat.ChatServiceGrpc;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.grpc.redis.RedisUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@GrpcService
public class ChatServiceImpl extends ChatServiceGrpc.ChatServiceImplBase {

    @Override
    public StreamObserver<ChatGrpc.ChatRequest> chatStream(StreamObserver<ChatGrpc. ChatMessage> responseObserver) {

        return new StreamObserver<ChatGrpc.ChatRequest>() {
            private String username;
            private String targetUser;
            @Override
            public void onNext(ChatGrpc.ChatRequest  request) {
                // 保存 信息
                username = request.getUsername();
//                targetUser = "lcy";
                targetUser = request.getTargetUser();
                userObservers.put(request.getUsername(),responseObserver);
                // 广播 消息给所有连接的客户端
                broadcastMessage(request.getUsername(),username + " has joined the chat",targetUser);
            }

            @Override
            public void onError(Throwable throwable) {
                System.err.println("Error: " + throwable.getMessage());
                userObservers.remove(username);
                broadcastMessage(username,username + "has left the chat",targetUser);
            }

            @Override
            public void onCompleted() {
                System.out.println("Stream completed");

                userObservers.remove(username);
                broadcastMessage(username,username + "has left the chat",targetUser);

                responseObserver.onCompleted();

            }
        };
    }


    @Override
    public void sendMessage(ChatGrpc.ChatMessage request, StreamObserver<ChatGrpc.Empty> responseObserver){
            String message ="["+request.getUsername()+"]: "+ request.getMessage();

            broadcastMessage(request.getUsername(),message,request.getTargetUser());

            responseObserver.onNext(ChatGrpc.Empty.getDefaultInstance());
            responseObserver.onCompleted();
    }
    private static final Map<String,StreamObserver<ChatGrpc.ChatMessage>> userObservers = new HashMap<>();

    public static final Map<String, List<String>> historyUserMessage = new HashMap<>();
    private void broadcastMessage(String username,String message,String targetUser) {
        // 广播 消息给所有连接的客户端
        ChatGrpc.ChatMessage chatMessage = ChatGrpc.ChatMessage.newBuilder()
                .setUsername(username)
                .setTargetUser(targetUser)
                .setMessage(message)
                .build();
        // 执行广播
        if (userObservers.containsKey(targetUser)) {
            userObservers.get(targetUser).onNext(chatMessage);
        }else {
            // 保存历史记录
            RedisUtils.setHistoryRecord(username,targetUser,message);
        }

//        for (StreamObserver<ChatGrpc.ChatMessage> value : userObservers.values()) {
//            value.onNext(chatMessage);
//        }
    }
}
