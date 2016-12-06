package com.example.chen.yuankong.Utils;

/**
 * Created by Chen on 2016/7/25.
 */
public class Tb_contacts {
    private String name;
    private String number;

    public Tb_contacts(String name, String number) {
        this.name = name;
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    @Override
    public String toString() {
        return "Tb_contacts [name=" + name + ", number=" + number + "]";
    }
}
