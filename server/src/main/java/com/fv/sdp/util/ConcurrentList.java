package com.fv.sdp.util;

import com.fv.sdp.model.Player;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.Collection;

@XmlSeeAlso(Player.class)
public class ConcurrentList<E>
{
    public ConcurrentList()
    {
        list = new ArrayList<>();
    }
    public ConcurrentList(Collection collection)
    {
        list = new ArrayList<E>(collection);
    }

    private ArrayList<E> list;
//////////////////////////////////////////////////////////////////////////////////////
    @XmlAnyElement(lax = true)
    public ArrayList<E> getList()
    {
        synchronized (list)
        {
            ArrayList<E> listCopy = new ArrayList<>(list);
            //log action
            return listCopy;
        }
    }

    public void setList(ArrayList<E> list)
    {
        this.list = list;
    }

    public void add(E item)
    {
        synchronized (list)
        {
            list.add(item);
            //log action
        }
    }

    //remove element
    public void remove(E item)
    {
        synchronized (list)
        {
            for (E i : list)
                if (i.equals(item))
                {
                    list.remove(i);
                    return;
                }
            //log action
        }
    }

    //get copy
    /*
    public ArrayList<E> getList()
    {
        synchronized (list)
        {
            ArrayList<E> listCopy = new ArrayList<>(list);
            //log action
            return listCopy;
        }
    }
    */
    public boolean contain(E item)
    {
        synchronized (list)
        {
            for(E e : list)
                if (e.equals(item))
                    return true;
            return false;
        }
    }

    public E getElement(E item)
    {
        synchronized (list)
        {
            for(E e : list)
                if (e.equals(item))
                    return e;
            return null;
        }
    }
}

