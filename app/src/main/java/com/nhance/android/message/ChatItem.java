package com.nhance.android.message;


public class ChatItem {

    public String firstName;
    public String lastName;
    public String id;
    public String type;
    public String thumbnail;
    public String profile;
    public String subject;
  // public String status;
    public boolean isMe;
    public long timestamp;
    public String content;
    public String sentOnTimestamp;
    public String userConversationId,conversationId,firstMessageId;
    public String isRead;

    @Override
    public String toString() {
        return "ChatItem{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", thumbnail='" + thumbnail + '\'' +
                ", profile='" + profile + '\'' +
                ", subject='" + subject + '\'' +
                ", isMe=" + isMe +
                ", timestamp=" + timestamp +
                ", content='" + content + '\'' +
                ", sentOnTimestamp='" + sentOnTimestamp + '\'' +
                ", userConversationId='" + userConversationId + '\'' +
                ", conversationId='" + conversationId + '\'' +
                ", firstMessageId='" + firstMessageId + '\'' +
                ", isRead='" + isRead + '\'' +
                ", unreadConversationCount=" + unreadConversationCount +
                '}';
    }

    public int unreadConversationCount;



}
