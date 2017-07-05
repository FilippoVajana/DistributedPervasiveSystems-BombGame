package com.fv.sdp.socket;

/**
 * Created by filip on 6/5/2017.
 */
public class RingMessage
{
    private MessageType type;
    private String sourceAddress; //set on socket sent
    private String id;
    private String content;
    private boolean needToken = true;

    public RingMessage()
    {    }
    public RingMessage(MessageType type, String id)
    {
        this.type = type;
        this.id = id;

        //type check
        if (type == MessageType.ACK)
            needToken = false;
    }
    public RingMessage(MessageType type, String id, String content)
    {
        this.type = type;
        this.id = id;
        this.content = content;

        //type check
        if (type == MessageType.ACK)
            needToken = false;
    }
    public RingMessage(MessageType type, String source, String id, String content)
    {
        this.type = type;
        this.sourceAddress = source;
        this.id = id;
        this.content = content;

        //type check
        if (type == MessageType.ACK)
            needToken = false;
    }

    public MessageType getType() {
        return type;
    }

    public String getSourceAddress() {
        return sourceAddress;
    }
    public void setSourceAddress(String sourceAddress) {
        this.sourceAddress = sourceAddress;
    }

    public String getId()
    {
        return id;
    }

    public String getContent()
    {
        return content;
    }

    public void setNeedToken(boolean needToken)
    {
        this.needToken = needToken;
    }

    @Override
    public String toString()
    {
        String messageString = String.format("Type: %s\t Id: %s\t Source: %s\t Content: %s", this.getType(), this.getId(), this.getSourceAddress(), this.getContent());
        return messageString;
    }
}
