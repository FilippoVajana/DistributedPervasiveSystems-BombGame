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

    //add element
    public void add(E item)
    {
        synchronized (list)
        {
            list.add(item);
            //log action
        }
    }

    //remove element
    public boolean remove(E item)
    {
        synchronized (list)
        {
            for (E i : list)
                if (i.equals(item))
                {
                    list.remove(i);
                    return true;
                }
            //log action
            return false;
        }
    }

    //check existence
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

    //return element reference
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

