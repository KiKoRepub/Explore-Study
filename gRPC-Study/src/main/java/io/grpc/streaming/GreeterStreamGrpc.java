package io.grpc.streaming;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.29.0)",
    comments = "Source: streamRequest.proto")
public final class GreeterStreamGrpc {

  private GreeterStreamGrpc() {}

  public static final String SERVICE_NAME = "GreeterStream";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<StreamRequestOuterClass.StreamRequest,
      StreamRequestOuterClass.StreamResponse> getHelloStreamMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "HelloStream",
      requestType = StreamRequestOuterClass.StreamRequest.class,
      responseType = StreamRequestOuterClass.StreamResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
  public static io.grpc.MethodDescriptor<StreamRequestOuterClass.StreamRequest,
      StreamRequestOuterClass.StreamResponse> getHelloStreamMethod() {
    io.grpc.MethodDescriptor<StreamRequestOuterClass.StreamRequest, StreamRequestOuterClass.StreamResponse> getHelloStreamMethod;
    if ((getHelloStreamMethod = GreeterStreamGrpc.getHelloStreamMethod) == null) {
      synchronized (GreeterStreamGrpc.class) {
        if ((getHelloStreamMethod = GreeterStreamGrpc.getHelloStreamMethod) == null) {
          GreeterStreamGrpc.getHelloStreamMethod = getHelloStreamMethod =
              io.grpc.MethodDescriptor.<StreamRequestOuterClass.StreamRequest, StreamRequestOuterClass.StreamResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "HelloStream"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  StreamRequestOuterClass.StreamRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  StreamRequestOuterClass.StreamResponse.getDefaultInstance()))
              .setSchemaDescriptor(new GreeterStreamMethodDescriptorSupplier("HelloStream"))
              .build();
        }
      }
    }
    return getHelloStreamMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static GreeterStreamStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<GreeterStreamStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<GreeterStreamStub>() {
        @Override
        public GreeterStreamStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new GreeterStreamStub(channel, callOptions);
        }
      };
    return GreeterStreamStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static GreeterStreamBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<GreeterStreamBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<GreeterStreamBlockingStub>() {
        @Override
        public GreeterStreamBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new GreeterStreamBlockingStub(channel, callOptions);
        }
      };
    return GreeterStreamBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static GreeterStreamFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<GreeterStreamFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<GreeterStreamFutureStub>() {
        @Override
        public GreeterStreamFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new GreeterStreamFutureStub(channel, callOptions);
        }
      };
    return GreeterStreamFutureStub.newStub(factory, channel);
  }

  /**
   */
  public static abstract class GreeterStreamImplBase implements io.grpc.BindableService {

    /**
     */
    public io.grpc.stub.StreamObserver<StreamRequestOuterClass.StreamRequest> helloStream(
        io.grpc.stub.StreamObserver<StreamRequestOuterClass.StreamResponse> responseObserver) {
      return asyncUnimplementedStreamingCall(getHelloStreamMethod(), responseObserver);
    }

    @Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getHelloStreamMethod(),
            asyncBidiStreamingCall(
              new MethodHandlers<
                StreamRequestOuterClass.StreamRequest,
                StreamRequestOuterClass.StreamResponse>(
                  this, METHODID_HELLO_STREAM)))
          .build();
    }
  }

  /**
   */
  public static final class GreeterStreamStub extends io.grpc.stub.AbstractAsyncStub<GreeterStreamStub> {
    private GreeterStreamStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
    protected GreeterStreamStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new GreeterStreamStub(channel, callOptions);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<StreamRequestOuterClass.StreamRequest> helloStream(
        io.grpc.stub.StreamObserver<StreamRequestOuterClass.StreamResponse> responseObserver) {
      return asyncBidiStreamingCall(
          getChannel().newCall(getHelloStreamMethod(), getCallOptions()), responseObserver);
    }
  }

  /**
   */
  public static final class GreeterStreamBlockingStub extends io.grpc.stub.AbstractBlockingStub<GreeterStreamBlockingStub> {
    private GreeterStreamBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
    protected GreeterStreamBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new GreeterStreamBlockingStub(channel, callOptions);
    }
  }

  /**
   */
  public static final class GreeterStreamFutureStub extends io.grpc.stub.AbstractFutureStub<GreeterStreamFutureStub> {
    private GreeterStreamFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
    protected GreeterStreamFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new GreeterStreamFutureStub(channel, callOptions);
    }
  }

  private static final int METHODID_HELLO_STREAM = 0;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final GreeterStreamImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(GreeterStreamImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }

    @Override
    @SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_HELLO_STREAM:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.helloStream(
              (io.grpc.stub.StreamObserver<StreamRequestOuterClass.StreamResponse>) responseObserver);
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class GreeterStreamBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    GreeterStreamBaseDescriptorSupplier() {}

    @Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return StreamRequestOuterClass.getDescriptor();
    }

    @Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("GreeterStream");
    }
  }

  private static final class GreeterStreamFileDescriptorSupplier
      extends GreeterStreamBaseDescriptorSupplier {
    GreeterStreamFileDescriptorSupplier() {}
  }

  private static final class GreeterStreamMethodDescriptorSupplier
      extends GreeterStreamBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    GreeterStreamMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (GreeterStreamGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new GreeterStreamFileDescriptorSupplier())
              .addMethod(getHelloStreamMethod())
              .build();
        }
      }
    }
    return result;
  }
}
