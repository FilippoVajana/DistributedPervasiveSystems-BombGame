package main.java.ring;

import main.java.socket.RingMessage;

public interface IMessageHandler
{
    void handle(RingMessage message);
}
