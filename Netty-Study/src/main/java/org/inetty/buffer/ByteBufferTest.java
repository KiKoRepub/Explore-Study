package org.inetty.buffer;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.FileChannel;

public class ByteBufferTest {


    public static void main(String[] args) throws IOException {
            String basePath = "D:\\university\\JAVA\\Netty-Study\\src\\main\\java\\org\\inetty\\buffer\\";

        RandomAccessFile bufferTestFile = new RandomAccessFile(basePath + "ByteBufferTest.java", "rw");

        // channel 是 流数据流动的通道，
        // 文件channel 是文件中的字符，字节流流动的通道
        FileChannel fileChannel = bufferTestFile.getChannel();

        // 分配一个10个字节的缓冲区，用来临时存储数据(后续可以选择 读出或者继续写)
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        buffer.clear(); // 清空缓存，并切换到写模式
        int bytes = -1;
        while (( bytes = fileChannel.read(buffer)) != -1) {
            System.out.println(bytes);
            // 写入成功
            buffer.flip(); // 切换到读模式

            while (buffer.hasRemaining()) {
                System.out.print((char) buffer.get());
            }

            buffer.clear(); // 清空缓存，并切换到写模式
        }




    }
}
