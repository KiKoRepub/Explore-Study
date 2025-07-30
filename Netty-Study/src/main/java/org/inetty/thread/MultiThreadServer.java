package org.inetty.thread;

import ch.qos.logback.core.net.ssl.SSLComponent;

import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

public class MultiThreadServer {
    public static void main(String[] args) throws IOException {

        Thread.currentThread().setName("boss");
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);

        Selector serverSelector = Selector.open();


    }
}
