package org.grpc.streaming;

import io.grpc.ServerBuilder;
import io.grpc.streaming.service.GreeterStreamImpl;
import org.grpc.interceptor.StreamServerInterceptor;

import java.io.IOException;

public class StreamServer {

    public static void main(String[] args) throws IOException, InterruptedException {
        io.grpc.Server server = ServerBuilder.forPort(9090)
                .addService(new GreeterStreamImpl())
                .intercept(new StreamServerInterceptor())
                .build();

        server.start();
        System.out.println("Server started on port 9090");
        server.awaitTermination();
    }
}
