package org.inetty.cs2;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;
import java.util.function.BiFunction;

public class NettyServer2 {
    public static void main(String[] args) throws IOException, InterruptedException {

        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress("localhost",6666));
        serverChannel.configureBlocking(false);


        Selector selector = Selector.open();

        SelectionKey serverKey = serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        while (true) {
            selector.select();
            Thread.sleep(1_000);
            Set<SelectionKey> keySet = selector.selectedKeys();
            Iterator<SelectionKey> iterator = keySet.iterator();
            while (iterator.hasNext()) {
                SelectionKey keyEvent = iterator.next();

                iterator.remove();

                if (keyEvent.isAcceptable()) {
                    // 连接
                    SocketChannel clientChannel = serverChannel.accept();

                    clientChannel.configureBlocking(false);

                    ByteBuffer buffer = ByteBuffer.allocate(16);
                    SelectionKey clientKey = clientChannel.register(selector, SelectionKey.OP_READ, buffer);


                    String message = "a".repeat(1_000_000_000);

                    ByteBuffer byteBuffer = Charset.defaultCharset().encode(message);

//                    while (byteBuffer.hasRemaining()) {
//                        int write = clientChannel.write(byteBuffer);
//                        System.out.println("write = " + write);
//                    }
//                    将循环发送 变成 等待缓冲区空闲 的时候发送
//                    先 给客户端的 selector 添加 可写事件 的关注 (TCP 窗口 是否空闲 )

                    int write = clientChannel.write(byteBuffer);
                    System.out.println("write = " + write);

                    if (byteBuffer.hasRemaining()) {
//                      表示 关注 原先的事件 也关注 可写事件
                        clientKey.interestOps(clientKey.interestOps() | SelectionKey.OP_WRITE);
                        // 挂载 缓冲区
                        keyEvent.attach(byteBuffer);

                    }



                }
                else if (keyEvent.isReadable()) {
                    // 读操作
                    SocketChannel clientChannel = (SocketChannel) keyEvent.channel();
                    ByteBuffer buffer = (ByteBuffer) keyEvent.attachment();

                    while (buffer.position() == buffer.limit()) {
                        // 考虑进行扩容


                    }



                }
                else if (keyEvent.isWritable()) {
                // 写操作
                     // 获取之前挂载的 Buffer
                    ByteBuffer buffer = (ByteBuffer) keyEvent.attachment();
                     SocketChannel clientChannel = (SocketChannel) keyEvent.channel();

                     // 写入数据
                    int write = clientChannel.write(buffer);
                    System.out.println("write = " + write);


                    // 当缓冲区没有数据了之后 清除绑定关系
                    if (!buffer.hasRemaining()) {
                        keyEvent.attach(null);

                        // 取消 关注 可写事件
                        keyEvent.interestOps(keyEvent.interestOps() & ~SelectionKey.OP_WRITE);
                    }
                }
                keyEvent.cancel();
            }



        }



    }

}
