package com.fv.sdp.util;

import java.util.ArrayDeque;

public class ConcurrentObservableQueue<E>
{
    private ArrayDeque<E> queue;
    private Object queueToken;
    public ConcurrentObservableQueue()
    {
        queue = new ArrayDeque<E>();
        queueToken = new Object();
    }

    public ArrayDeque<E> getQueue()
    {
        return new ArrayDeque<E>(queue);
    }

    public Object getQueueLock()
    {
        return queueToken;
    }

    public boolean push(E item)
    {
        synchronized (queueToken)
        {
            queue.push(item);
            queueToken.notify();
        }
        return true;
    }

    public E pop()
    {
        synchronized (queueToken)
        {
            if (queue.size() == 0)
                return null;
            E item = queue.pop();
            return item;
        }
    }

    public int size()
    {
        synchronized (queueToken)
        {
            return queue.size();
        }
    }

    public void remove(E item)
    {
        synchronized (queueToken)
        {
            queue.remove(item);
        }
    }
}
