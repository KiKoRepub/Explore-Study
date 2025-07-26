package org.inetty.filework;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.atomic.AtomicInteger;

public class FileWalkingTest {

    public static void main(String[] args) throws IOException {
        String basePath = "D:\\university\\JAVA\\Netty-Study\\src\\main\\java";

        AtomicInteger dirCount = new AtomicInteger(0);
        AtomicInteger fileCount  = new AtomicInteger(0);
        Files.walkFileTree(Paths.get(basePath), new SimpleFileVisitor<Path>(){
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                System.out.println("===>"+dir);
                dirCount.incrementAndGet();
                return super.preVisitDirectory(dir, attrs);
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                System.out.println("==+>"+file.getFileName());
                fileCount.incrementAndGet();
                return super.visitFile(file, attrs);
            }
        });

        System.out.println("dirCount="+dirCount.get());
        System.out.println("fileCount="+fileCount.get());
    }
}
