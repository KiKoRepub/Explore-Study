package io.grpc.streaming.service;


import io.grpc.streaming.GreeterStreamGrpc;
import io.grpc.streaming.StreamRequestOuterClass;
import io.grpc.stub.StreamObserver;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class GreeterStreamImpl extends GreeterStreamGrpc.GreeterStreamImplBase {
    @Override
    public StreamObserver<StreamRequestOuterClass.StreamRequest> helloStream(StreamObserver<StreamRequestOuterClass.StreamResponse> responseObserver) {
        return  new StreamObserver<StreamRequestOuterClass.StreamRequest>() {
            private static final Scanner sc = new Scanner(System.in);
            private Map<String,String> messageMap = new HashMap<>();
            private String message = null;
            @Override
            public void onNext(StreamRequestOuterClass.StreamRequest streamRequest) {
                System.out.println("Received request: " + streamRequest.getName());
                System.out.println("请输入要回复的内容");
                String reply = sc.nextLine();
                messageMap.put("server",reply);

            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("Error: " + throwable.getMessage());
            }

            @Override
            public void onCompleted() {
                // 构建响应
                message = messageMap.get("server");
                while (message != null) {
                    StreamRequestOuterClass.StreamResponse response = StreamRequestOuterClass.StreamResponse
                            .newBuilder()
                            .setName(message) // name is defined in the proto file
                            .build();
                    responseObserver.onNext(response);
                    message = null;
                    responseObserver.onCompleted();
                }
            }
        };

    }
}
