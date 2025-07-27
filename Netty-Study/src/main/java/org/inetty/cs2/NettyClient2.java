package org.inetty.cs2;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class NettyClient2 {

    public static void main(String[] args) throws IOException, InterruptedException {

        SocketChannel clientChannel = SocketChannel.open();


        clientChannel.connect(new InetSocketAddress("localhost", 6666));
        clientChannel.configureBlocking(false);

//        clientChannel01(clientChannel);
        clientChannel02(clientChannel);

    }

    private static void clientChannel02(SocketChannel clientChannel) throws IOException {

        int totalCount = 0;

        while (totalCount < 1_000_000_000) {
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            int hasReadCount = clientChannel.read(buffer);
            if (hasReadCount != -1) {
                totalCount += hasReadCount;
                System.out.println("totalCount = " + totalCount);
            }
            buffer.clear();
        }



    }
    private static void clientChannel01(SocketChannel clientChannel) throws IOException {
        Selector selector = Selector.open();
        clientChannel.register(selector, SelectionKey.OP_WRITE);

        int totalCount = 0;
        while (true) {
            selector.select();
//            不使用 Selector
//            直接接收数据
            Set<SelectionKey> selectionKeys = selector.selectedKeys();

            Iterator<SelectionKey> iterator = selectionKeys.iterator();


            while (iterator.hasNext()) {
//
//
                SelectionKey key = iterator.next();
//
                System.out.println(key.isReadable());
                System.out.println(key.isAcceptable());
                System.out.println(key.isConnectable());
                System.out.println(key.isWritable());
                System.out.println(key.isValid());


                iterator.remove();

                if (key.isReadable()) {

                    ByteBuffer buffer = ByteBuffer.allocate(1024);

                    int hasReadCount = clientChannel.read(buffer);

                    if (hasReadCount != -1) {
                        // 统计数量
                        totalCount += hasReadCount;
                        System.out.println("totalCount = " + totalCount);
                    }
                    buffer.clear();
                }else key.cancel();
            }
        }
    }
}
