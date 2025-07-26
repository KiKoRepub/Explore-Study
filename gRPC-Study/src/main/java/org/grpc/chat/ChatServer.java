package org.grpc.chat;

import chat.grpc.service.ChatServiceImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class ChatServer {

    public static void main(String[] args) throws IOException, InterruptedException {
        Server server = ServerBuilder.forPort(8080)
                .addService(new ChatServiceImpl())
//                .intercept()
                .build();
        server.start();
        server.awaitTermination();
    }
}
