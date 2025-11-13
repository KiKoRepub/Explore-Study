package org.dee.utlis;

public class ChatUtils {


    public static String buildConversationKey(String conversationId,String userId){
        return "chat:conversation:" + userId + ":" + conversationId;
    }

    public static String getConversationIdFromKey(String conversationKey){
        return conversationKey.split(":")[3];
    }
    public static String getUserIdFromKey(String conversationKey){
        return conversationKey.split(":")[2];
    }
}
