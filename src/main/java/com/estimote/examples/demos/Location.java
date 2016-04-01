package com.estimote.examples.demos;

import java.util.List;

public class Location{
    private int id;
    private String floor;
    private String name;
    private String text;
    private double x;
    private double y;
    private int type;

    Location(int id,String floor, String name, String text,int type, int x, int y){
        this.id = id;
        this.floor= floor;
        this.name = name;
        this.text = text;
        this.type = type;
        this.x = x; this.y = y;
    }
    Location(int id, String name, String text, int x, int y){
        this(id,LocationMgr.defaultFloor, name, text, LocationMgr.TYPE_ROOM, x, y);
    }
    @Override
    public boolean equals(Object o) {
        return o instanceof Location && this.getId() == ((Location) o).getId();
    }

    public int getId() {
        return id;
    }

    public String getFloor() {
        return floor;
    }

    public String getName() {
        return name;
    }

    public String getText() {
        return text;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public int getType() {
        return type;
    }
    @Override
    public String toString(){
        return "ID:%s Name:%s".format(id+"", name);
    }
}