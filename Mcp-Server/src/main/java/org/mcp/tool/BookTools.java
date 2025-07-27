package org.mcp.tool;

import org.mcp.annotion.MyTool;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.context.annotation.Description;

import java.util.ArrayList;
import java.util.List;
@MyTool("租借图书的 MCP 工具")
public class BookTools {

    @Tool(description = "获取书籍的详细信息")
    public String getBookInfo(@ToolParam(description = "需要获取信息的书的名字") String bookName) {

        String resFormat = """
                书名：%s
                作者：%s
                出版社：%s
                出版日期：%s
             """;
        return String.format(resFormat, bookName, "Alonet", "KiKo 出版社", "2023-01-01");
    }

    @Tool(description = "获取包含特定书籍的书店信息")
    public List<String> getBookStoreInfo(@ToolParam(description = "需要获取信息的书的名字") String bookName) {
        List<String> result = new ArrayList<>();
        String resFormat = """
                店铺名字: %s
                店铺地址: %s
                店铺电话: %s
            """;

        result.add(String.format(resFormat, "StavyTaff Shop", "1234 Street, LoShanJi,America", "18270670891"));
        result.add(String.format(resFormat,"KiKoRepub Shop", "1234 Street,XiNi,Australia", "19290543065"));


        return result;
    }

    @Tool(description = "获取特定店铺特定书籍的租借信息")
    public String getBookRentInfo(@ToolParam(description = "需要获取信息的书的名字") String bookName, @ToolParam(description = "需要获取信息的店铺名字") String storeName) {
        String resFormat = """
                书名：%s
                店铺：%s
                存量：%d
                租借价格：%s
                可租借时长：%s
             """;

        return String.format(resFormat,bookName,storeName,5,"free","3个月");
    }


}
