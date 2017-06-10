package main.java.util;

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

    public Object getQueueToken()
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
}
