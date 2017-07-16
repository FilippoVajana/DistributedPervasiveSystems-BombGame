package com.fv.sdp.ring;

import com.fv.sdp.socket.RingMessage;

public interface IMessageHandler
{
    void handle(RingMessage receivedMessage);
}
