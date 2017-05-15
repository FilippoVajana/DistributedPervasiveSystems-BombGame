package com.fv.sdp.util;

import java.util.ArrayList;

public class ConcurrentList<E>
{
    public ConcurrentList()
    {
        _list = new ArrayList<>();
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
            _list.remove(item);
            //log action
        }
    }

    //get list
    public ArrayList<E> getList()
    {
        synchronized (_list)
        {
            ArrayList<E> listCopy = new ArrayList<>(_list);
            //log action
            return listCopy;
        }
    }
}

