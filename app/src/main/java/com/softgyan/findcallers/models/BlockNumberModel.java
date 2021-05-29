package com.softgyan.findcallers.models;

import java.io.Serializable;

public class  BlockNumberModel implements Serializable {
    private int id;
    private String number;
    private int type;
    private String name;

    public BlockNumberModel() {
    }

    public BlockNumberModel(int id, String number, int type, String name) {
        this.id = id;
        this.number = number;
        this.type = type;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "BlockNumberModel{" +
                "id=" + id +
                ", number='" + number + '\'' +
                ", type=" + type +
                ", name='" + name + '\'' +
                '}';
    }

}
