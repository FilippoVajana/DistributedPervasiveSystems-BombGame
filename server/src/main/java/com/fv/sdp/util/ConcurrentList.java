package com.fv.sdp.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;

public class ConcurrentList<E>
{
    public ConcurrentList()
    {
        _list = new ArrayList<>();
    }
    public ConcurrentList(Collection collection)
    {
        _list = new ArrayList<E>(collection);
    }
    private ArrayList<E> _list = null;

    //add element
    public void add(E item)
    {
        synchronized (_list)
        {
            _list.add(item);
            //log action
        }
    }

    //remove element
    public void remove(E item)
    {
        synchronized (_list)
        {
            for (E i : _list)
                if (i.equals(item))
                {
                    _list.remove(i);
                    return;
                }
            //log action
        }
    }

    //get copy
    public ArrayList<E> getList()
    {
        synchronized (_list)
        {
            ArrayList<E> listCopy = new ArrayList<>(_list);
            //log action
            return listCopy;
        }
    }

    public boolean contain(E item)
    {
        synchronized (_list)
        {
            for(E e : _list)
                if (e.equals(item))
                    return true;
            return false;
        }
    }

    public E getElement(E item)
    {
        synchronized (_list)
        {
            for(E e : _list)
                if (e.equals(item))
                    return e;
            return null;
        }
    }
}

