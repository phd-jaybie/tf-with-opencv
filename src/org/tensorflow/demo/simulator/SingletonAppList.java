package org.tensorflow.demo.simulator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by deg032 on 9/2/18.
 */

public class SingletonAppList  {
    private static final SingletonAppList instance = new SingletonAppList();

    private List<App> list = new ArrayList<>();
    private String listText;

    // Private constructor prevents instantiation from other classes
    private SingletonAppList() {
    }

    public static SingletonAppList getInstance() {
        return instance;
    }

    public void setList(List<App> list) {
        this.list = list;
    }

    public List<App> getList() {
        return list;
    }

    public void setListText(String listText) {
        this.listText = listText;
    }

    public String getListText() {
        return listText;
    }
}
