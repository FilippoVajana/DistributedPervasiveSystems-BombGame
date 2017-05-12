package com.fv.sdp.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;

/**
 * Created by filip on 12/05/2017.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class TestModel
{
    private String id;
    private ArrayList<Integer> list;

    public TestModel() {
    }

    public TestModel(String id, ArrayList<Integer> list) {
        this.id = id;
        this.list = list;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ArrayList<Integer> getList() {
        return list;
    }

    public void setList(ArrayList<Integer> list) {
        this.list = list;
    }
}
