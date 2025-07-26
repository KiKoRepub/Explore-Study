package org.grpc.streaming;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.streaming.GreeterStreamGrpc;
import io.grpc.streaming.StreamRequestOuterClass;
import io.grpc.stub.StreamObserver;
import org.grpc.interceptor.StreamClientInterceptor;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

public class StreamClient {

    public static void main(String[] args) {

        // 每次收到信息 或者 发送信息的时候 阻塞 等待用户输入，之后再进行发送
        Scanner sc = new Scanner(System.in);
        Map<String,String> messageMap = new HashMap<>();

        messageMap.put("client",null);
        String message = messageMap.get("client");

        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 9090)
                .usePlaintext()
                .intercept(new StreamClientInterceptor())
                .build();

            // Create a new client stub
            GreeterStreamGrpc.GreeterStreamStub stub = GreeterStreamGrpc.newStub(channel);


            // Create a new request
        StreamObserver<StreamRequestOuterClass.StreamResponse> responseObserver = new StreamObserver<StreamRequestOuterClass.StreamResponse>() {

            @Override
            public void onNext(StreamRequestOuterClass.StreamResponse streamResponse) {
                System.out.println("received response from server "+ streamResponse.getName());

                if (streamResponse.getName().equals("exit")){
                    messageMap.put("client","exit");
                }
                else {
                    System.out.println("请输入你要发送的信息");
                    String reply = sc.nextLine();
                    messageMap.put("client", reply);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                System.err.println("Error occurred: " + throwable.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("Server stream completed");
            }
        };
        // 通过 stub 将 响应流 和 请求流 对应起来
        StreamObserver<StreamRequestOuterClass.StreamRequest> requestObserver = stub.helloStream(responseObserver);
        System.out.println("请输入你要发送的信息");
        message = sc.nextLine();

        // 持续 监测
          while (!Objects.equals(message, "exit")) {
            message = messageMap.get("client");
              if (message != null) {
                  StreamRequestOuterClass.StreamRequest request = StreamRequestOuterClass.StreamRequest
                          .newBuilder()
                          .setName(message)
                          .build();
                  requestObserver.onNext(request);
                  message = null;
                  // 模拟发送间隔
              }
          }
        // 表示 完成了 所有请求的发送
        requestObserver.onCompleted();
        //  模拟等待 请求完成
        try{
            Thread.sleep(1000);
        }catch (Exception e){
            e.printStackTrace();
        }

        // 关闭连接
        channel.shutdownNow();
    }
}
