package com.example.chen.yuankong.Utils;

import org.json.simple.JSONObject;

/**
 * Created by Chen on 2016/7/12.
 */
public class filetree {

    private int index;
    private String text;
    private String path;
    private String parent;
    private String state;

    public filetree(int index, String text, String path, String parent, String state) {
        this.index = index;
        this.text = text;
        this.path = path;
        this.parent = parent;
        this.state = state;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void tojson(JSONObject object)
    {
        object.put("index",index);
        object.put("text",text);
        object.put("path",path);
        object.put("state",state);
        object.put("parent",parent);

    }
}
