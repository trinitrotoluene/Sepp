package is.trinit.sepp.commands;

import discord4j.core.object.entity.Message;

public class MessageCommandSource {
    private final String content;
    private final Message message;

    public MessageCommandSource(String content, Message message) {
        this.content = content;
        this.message = message;
    }

    public Message getMessage() {
        return message;
    }

    public String getContent() {
        return content;
    }
}
