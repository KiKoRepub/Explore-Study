package io.grpc.hello.service;

import io.grpc.hello.GreeterGrpc;
import io.grpc.hello.HelloReply;
import io.grpc.hello.HelloRequest;
import io.grpc.stub.StreamObserver;

public class GreeterImpl extends GreeterGrpc.GreeterImplBase {
    @Override
    public void sayHello(HelloRequest req, StreamObserver<HelloReply> responseObserver) {
        HelloReply reply = HelloReply.newBuilder().setMessage("Hello " + req.getName()).build();
        System.out.println("=====server=====");
        System.out.println("server: Hello " + req.getName());
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }
}
