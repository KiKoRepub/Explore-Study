package org.inetty.cs1;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Scanner;

public class NettyClient {

    public static void main(String[] args) throws IOException, InterruptedException {
        SocketChannel client = SocketChannel.open();
        client.connect(new InetSocketAddress("localhost", 9997));
//        Thread.sleep(1000);
        System.out.println("waiting !!!");

//        client.write(Charset.defaultCharset().encode("hello+world+"));

        Scanner sc = new Scanner(System.in);

        String line = "";
        while (!"666".equals(line)){
            System.out.println("输入想要传递的内容");
             line = sc.nextLine();

            // 防止用户输入不规范
            if (!line.endsWith("+")) line += '+';
            client.write(Charset.defaultCharset().encode(line));
        }

    }
}
