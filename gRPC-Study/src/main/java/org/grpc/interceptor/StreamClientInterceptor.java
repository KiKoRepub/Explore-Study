package org.grpc.interceptor;

import io.grpc.*;

public class StreamClientInterceptor implements ClientInterceptor {
    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> methodDescriptor,
                                                               CallOptions callOptions, Channel channel) {

        System.out.println("Sending request to " + methodDescriptor.getFullMethodName());

//        返回一个 在启动的时候 注册了 一个可以 监听收到的消息 的监听器 对应的 ClientCall对象
        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT,RespT>
                (channel.newCall(methodDescriptor, callOptions)){
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                super.start(new ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>(responseListener) {
                    @Override
                    public void onMessage(RespT message) {
                        System.out.println("Received response from Server : " + methodDescriptor.getFullMethodName());
                        super.onMessage(message);
                    }
                }, headers);
            }
        };




    }
}
