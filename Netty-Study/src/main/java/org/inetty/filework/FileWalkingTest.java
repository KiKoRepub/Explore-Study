package org.inetty.filework;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.atomic.AtomicInteger;

public class FileWalkingTest {

    public static void main(String[] args) throws IOException {
//        String basePath = "D:\\university\\JAVA\\Netty-Study\\src\\main\\java";
//        fileWalking(basePath);

//        String basePath = "C:\\Users\\qazxc\\Desktop\\cache\\支付记录";
//        fileWalkDelete(basePath);

        String sourcePath = "C:\\Users\\qazxc\\Desktop\\cache\\支付记录";
        String targetPath = "C:\\Users\\qazxc\\Desktop\\cache\\zfjl-lcy";
        fileWalkCopy(sourcePath, targetPath);
    }

    private static void fileWalking(String basePath) throws IOException {
        AtomicInteger dirCount = new AtomicInteger(0);
        AtomicInteger fileCount = new AtomicInteger(0);
        Files.walkFileTree(Paths.get(basePath), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                System.out.println("===>" + dir);
                dirCount.incrementAndGet();
                return super.preVisitDirectory(dir, attrs);
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                System.out.println("==+>" + file.getFileName());
                fileCount.incrementAndGet();
                return super.visitFile(file, attrs);
            }
        });

        System.out.println("dirCount=" + dirCount.get());
        System.out.println("fileCount=" + fileCount.get());
    }


    private static void fileWalkDelete(String basePath) throws IOException {
        // 在遍历的时候 删除文件

        AtomicInteger fileNums = new AtomicInteger(0);

        Files.walkFileTree(Paths.get(basePath), new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {

                System.out.println("=======>进入"+ dir );
                return super.preVisitDirectory(dir, attrs);

            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                fileNums.incrementAndGet();
                System.out.println("file name is :" + file.getFileName());
                Files.delete(file);
                return super.visitFile(file, attrs);
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {

                System.out.println("<=======离开"+ dir );
                Files.delete(dir);

                return super.postVisitDirectory(dir, exc);
            }
        });




    }

    private static void fileWalkCopy( String sourcePath,String targetPath) throws IOException {
        Path path = Paths.get(sourcePath);

        File targetDir = new File(targetPath);
        if (!targetDir.exists())
            targetDir.mkdirs();

            Files.walk(path).forEach(filePath -> {
                    try{
                        if(Files.isDirectory(filePath)){
                            // 如果是目录 就创建目录 (名字和原目录保持一致)
                            Files.createDirectory(filePath.getFileName());
                        }
                        // 如果是文件 就执行复制
                        else if (Files.isRegularFile(filePath)) {
                            Files.copy(filePath, Paths.get(targetPath + File.separator + filePath.getFileName()));
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
            });
            }


}
