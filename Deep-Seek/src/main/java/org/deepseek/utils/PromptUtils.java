package org.deepseek.utils;

import javax.validation.constraints.NotNull;
import java.io.*;
import java.util.Objects;

public class PromptUtils {


    private static final String BASE_PATH = "prompts/";

    public static String getCodeAssistantPrompt(){
        String filePath ="prompts/codeAssistantPrompt.txt";
//        String filePath = BASE_PATH + "codeAssistantPrompt.txt";
        return readFile(filePath);
    }


    public static String getChatAssistantPrompt(){
        String filePath = BASE_PATH + "chatAssistantPrompt.txt";
        return readFile(filePath);
    }


    private  static String readFile(@NotNull String filePath) {

            try(BufferedReader reader =  new BufferedReader(new InputStreamReader(
                    Objects.requireNonNull(PromptUtils.class.getClassLoader().getResourceAsStream(filePath))))) {
                String line = "";
                StringBuilder sb = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append(System.lineSeparator());
                }

                return sb.toString();
            } catch (IOException e) {
                if (e instanceof FileNotFoundException notFound) {
                    System.out.println("File not found: " + filePath);
                } else System.out.println("Error reading file: " + filePath);
                return "";
            }
    }

}
