package org.inetty.utils;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BufferStringUtils {



    public static List<String> bufferStringSplit(ByteBuffer source,Character splitChar) {
        // 切换到读模式
        source.flip();

        List<String> result = new ArrayList<>();
        for (int i = 0; i < source.limit(); i++) {
            if (source.get(i) == splitChar) {
                //提取 从 source.position() 到 i 之间的数据
                int length = i - source.position();

                ByteBuffer target = ByteBuffer.allocate(length);

                // 提取 前 length 条数据
                for (int k = 0; k < length; k++) {
                    byte b = source.get();
                    // 不允许 分隔符 出现在分割后的结果中
                    if (b != splitChar)
                        target.put(b);
                }
//                result.add(Charset.defaultCharset().decode(target).toString());

                   result.add(new String(target.array(),StandardCharsets.UTF_8));
            }

        }

        // 还原状态
        source.compact();
        if (result.size() == 0) result.add(new String(source.array(),StandardCharsets.UTF_8));
        return result;
    }
}
