package util;

import com.fv.sdp.socket.ISocketObserver;
import com.fv.sdp.socket.RingMessage;
import com.fv.sdp.util.PrettyPrinter;

public class MockSocketObserver implements ISocketObserver
{
    private int observerId;

    public MockSocketObserver(int id)
    {
        observerId = id;
    }

    @Override
    public void pushMessage(RingMessage message)
    {
        //log
        PrettyPrinter.printTimestampLog(String.format("[Observer#%d] Received: %s", observerId, message.getContent()));
    }
}
