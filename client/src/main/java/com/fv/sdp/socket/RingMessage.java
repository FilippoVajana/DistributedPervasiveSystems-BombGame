package com.fv.sdp.socket;

/**
 * Created by filip on 6/5/2017.
 */
public class RingMessage
{
    private MessageType type;
    private String sourceAddress; //set on socket receive
    private String id;
    private String content;

    public RingMessage()
    {    }
    public RingMessage(MessageType type, String id)
    {
        this.type = type;
        this.id = id;
    }
    public RingMessage(MessageType type, String id, String content)
    {
        this.type = type;
        this.id = id;
        this.content = content;
    }
    public RingMessage(MessageType type, String source, String id, String content)
    {
        this.type = type;
        this.sourceAddress = source;
        this.id = id;
        this.content = content;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public String getSourceAddress() {
        return sourceAddress;
    }

    public void setSourceAddress(String sourceAddress) {
        this.sourceAddress = sourceAddress;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString()
    {
        String messageString = String.format("Type: %s\t Id: %s\t Source: %s\t Content: %s", this.getType(), this.getId(), this.getSourceAddress(), this.getContent());
        return messageString;
    }
}
