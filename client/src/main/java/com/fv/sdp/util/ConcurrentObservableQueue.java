package com.fv.sdp.util;

import java.util.ArrayDeque;

public class ConcurrentObservableQueue<E>
{
    private ArrayDeque<E> queue;
    private Object queueSignal;
    public ConcurrentObservableQueue()
    {
        queue = new ArrayDeque<E>();
        queueSignal = new Object();
    }

    public ArrayDeque<E> getQueue()
    {
        return new ArrayDeque<E>(queue);
    }

    public Object getQueueSignal()
    {
        return queueSignal;
    }

    public boolean push(E item)
    {
        synchronized (queueSignal)
        {
            queue.push(item);
            queueSignal.notify();
        }
        return true;
    }

    public E pop()
    {
        synchronized (queueSignal)
        {
            if (queue.size() == 0)
                return null;
            E item = queue.pop();
            return item;
        }
    }

    public int size()
    {
        synchronized (queueSignal)
        {
            return queue.size();
        }
    }

    public void remove(E item)
    {
        synchronized (queueSignal)
        {
            queue.remove(item);
        }
    }
}
