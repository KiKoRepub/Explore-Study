package org.grpc.interceptor;

import io.grpc.*;

public class StreamServerInterceptor implements io.grpc.ServerInterceptor{
    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall,
                                                                 Metadata metadata,
                                                                 ServerCallHandler<ReqT, RespT> serverCallHandler) {

        System.out.println("StreamServerInterceptor interceptCall called");

        ServerCall.Listener<ReqT> reqTListener = serverCallHandler.startCall(serverCall, metadata);

        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(reqTListener) {
            @Override
            protected ServerCall.Listener<ReqT> delegate() {
                return super.delegate();
            }

            @Override
            public void onMessage(ReqT message) {
                System.out.println("StreamServerInterceptor onMessage called");
                super.onMessage(message);
            }
            @Override
            public void onHalfClose(){
                System.out.println("Client closed the call");
                super.onHalfClose();
            }

            @Override
            public void onCancel() {
                System.out.println("StreamServerInterceptor onClose called");
                super.onCancel();
            }
            @Override
            public void onComplete() {
                System.out.println("StreamServerInterceptor onComplete called");
                super.onComplete();
            }
        };

    }
}
