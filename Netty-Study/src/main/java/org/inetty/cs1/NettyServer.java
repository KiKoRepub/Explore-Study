package org.inetty.cs1;

import lombok.extern.slf4j.Slf4j;
import org.inetty.utils.BufferStringUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@Slf4j
public class NettyServer {


    public static void main(String[] args) throws IOException {

        ServerSocketChannel  serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress("localhost",9997));
        serverClient02(serverSocketChannel);

    }

    private static void serverClient01(ServerSocketChannel serverChannel) throws IOException {
        // 非阻塞模式 ( accept 不会阻塞当前线程，在那一刻如果没有连接，就返回 null )
        serverChannel.configureBlocking(false);

        List<SocketChannel> clientChannelList = new ArrayList<>();
        ByteBuffer buffer = ByteBuffer.allocate(16);

        while (true) {
            //等待客户端 建立连接
//            System.out.println("waiting for client...");
            SocketChannel clientChannel = serverChannel.accept();
            if (clientChannel != null) {
                System.out.println("client connected, is " + clientChannel);
                // 设置成非阻塞模式 (read 不会阻塞当前线程，如果 read 的时候 buffer 中没有数据 就返回0)
                clientChannel.configureBlocking(false);
                clientChannelList.add(clientChannel);
            }
            for (SocketChannel client : clientChannelList) {

//                System.out.println("before read "+client);
                // 将数据写入 Buffer
                int read = client.read(buffer);
                // 切换到读模式
                if (read > 0) {
                    buffer.flip();
                    if (buffer.hasRemaining()) {
                        // 读取数据
                        byte[] bytes = new byte[buffer.remaining()];
                        buffer.get(bytes);
                        System.out.println("read data " + new String(bytes));
                    }

                    System.out.println("after read " + client);
                }


            }


        }
    }

    private static void serverClient02(ServerSocketChannel serverChannel) throws IOException {
        /*
        Selector
            可以监听 4种事件的发生，每个事件可以绑定一个 Channel
            事件发生的时候，会根据发生的事件 去获得 具有绑定关系的 Channel 进行返回
         */
        Selector selector = Selector.open();
        serverChannel.configureBlocking(false);

        // 将建立的client 连接 注册到 selector上
        //                  selectedKey
        // 注册 Server (server <====> selector <-> [client1,client2] )
        SelectionKey serverKey = serverChannel.register(selector,0);
        // 关注 accept 事件，
        serverKey.interestOps(SelectionKey.OP_ACCEPT);


        while (true){
            // 调用 select() 方法，阻塞等待就绪的 channel，当有事件发生时，继续
            // 会 往 keySet 中 插入事件，但是不会删除
            selector.select();
            // 如果不进行处理，它不会阻塞
            // 要不将收到的任务取消，要么将任务处理掉
            // 尝试处理事件
            // keySet 中会保存所有发生过的事件(不会删除)
            Set<SelectionKey> keySet = selector.selectedKeys();
            Iterator<SelectionKey> iterator = keySet.iterator();

            while (iterator.hasNext()){
                SelectionKey keyEvent = iterator.next();
                // 删除当前遍历的 keyEvent (后续逻辑中一定会将该事件处理完)
                keySet.remove(keyEvent);
                System.out.println("key = " + keyEvent);
            if(keyEvent.isAcceptable()) {
                ServerSocketChannel serverKeyChannel = (ServerSocketChannel) keyEvent.channel();
                SocketChannel clientChannel = serverKeyChannel.accept();
                clientChannel.configureBlocking(false);


                // 附加一个 buffer

                ByteBuffer buffer = ByteBuffer.allocate(16);
                SelectionKey clientKey = clientChannel.register(selector, 0,buffer);

                // 让它监听 READ 事件
                clientKey.interestOps(SelectionKey.OP_READ);




            } else if (keyEvent.isReadable()) {
                // clientChannel 读取 内容 (需要注意可能抛出的 IOException)
                try {
                    SocketChannel channel = (SocketChannel) keyEvent.channel();

                    //  Buffer 作为 局部的具有固定值的 对象，在接收信息的时候 可能会把信息拆分成两段
                    //  所以需要考虑其他的方案
                    ByteBuffer buffer = ByteBuffer.allocate(16);

                    // 通过获取附加值的方式，可以调整 大小
                    buffer = (ByteBuffer) keyEvent.attachment();

                    int status = channel.read(buffer); // 正常的链接断开 status == -1
                    if(status == -1){
                        // 链接断开
                        System.out.println("client closed");
                        keyEvent.cancel();
                    }else {
                        // 执行 打印
                        List<String> list = BufferStringUtils.bufferStringSplit(buffer,'+');

                        list.forEach(System.out::println);

                        if(buffer.position() == buffer.limit()){
                            // buffer 满了
                            System.out.println("buffer is full, need to expand buffer");

                          ByteBuffer biggerBuffer = ByteBuffer.allocate(buffer.capacity() * 2);

                          biggerBuffer.put(buffer); // 接收了原本的16字节
                          keyEvent.attach(biggerBuffer); // 将 buffer 重新设置成 32字节
                        }



                        buffer.clear();
                    }
                }catch (IOException e){
                    System.out.println("client read buffer error, may your client closed? ");
                    keyEvent.cancel();
                }

            }else keyEvent.cancel();


            }




        }

//        SocketChannel clientChannel = serverChannel.accept();

    }


}
