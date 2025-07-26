package org.grpc.chat;

import chat.grpc.service.ChatServiceImpl;
import grpc.chat.ChatGrpc;
import grpc.chat.ChatServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.grpc.redis.RedisUtils;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class ChatClient {
    public static void main(String[] args) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost",9090)
                .usePlaintext()
//                .intercept()
                .build();
        
         ChatServiceGrpc.ChatServiceStub blockingStub =  ChatServiceGrpc.newStub(channel);

        Scanner sc = new Scanner(System.in);
        System.out.println("Enter your username below :");
        String username = sc.nextLine();
        System.out.println("Enter your target user below :");
        String targetUser = sc.nextLine();
         // 处理 响应(服务端 发送过来的数据 )
        List<String> historyRecord = RedisUtils.getHistoryRecord(username,targetUser);
        if (historyRecord != null && historyRecord.size() > 0) {
            for (String message : historyRecord) {
                System.out.println(message);
            }
        }


        StreamObserver<ChatGrpc. ChatMessage> responseObserver = new StreamObserver<>(){

            @Override
            public void onNext(ChatGrpc. ChatMessage  ChatMessage) {
                System.out.println( ChatMessage.getMessage());
            }

            @Override
            public void onError(Throwable throwable) {
                System.err.println("error is "+throwable.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("client onCompleted");
            }
        };

        StreamObserver<ChatGrpc.ChatRequest>  requestObserver = blockingStub.chatStream(responseObserver);


        requestObserver.onNext(ChatGrpc.ChatRequest.newBuilder()
                                .setUsername(username)
                                .setTargetUser(targetUser)
                                .build());
        while (true){
            String message = sc.nextLine();
            if ("exit".equalsIgnoreCase(message)){
                requestObserver.onCompleted();
                break;
            }
            ChatGrpc.ChatMessage response = ChatGrpc. ChatMessage.newBuilder()
                    .setUsername(username)
                    .setMessage(message)
                    .setTargetUser(targetUser)
                    .build();
            blockingStub.sendMessage(response,new StreamObserver<ChatGrpc.Empty>(){

                @Override
                public void onNext(ChatGrpc.Empty empty) {

                }

                @Override
                public void onError(Throwable throwable) {
                    System.err.println("error is "+throwable.getMessage());
                }

                @Override
                public void onCompleted() {

                }
            });
        }

        channel.shutdown();
    }
}
