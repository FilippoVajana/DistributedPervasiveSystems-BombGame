package main.java.socket;

/**
 * Created by filip on 6/5/2017.
 */
public class RingMessage
{
    private MessageType type;
    private String id;
    private String content;

    public RingMessage(MessageType type, String id, String content)
    {
        this.type = type;
        this.id = id;
        this.content = content;
    }

    public MessageType getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public String getContent() {
        return content;
    }
}
