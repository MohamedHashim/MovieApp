package com.example.mohamedhashim.hashim;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class TrailerKey {
    public TrailerKey(String key, String name,String size) {
        this.key = key;
        this.name = name;
        this.size=size;
    }

    private String key,name,size;
    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }
    public String getName() {
        return name;
    }
    public void setName(String key) {
        this.name = name;
    }

    public String getSize() {
        return size;
    }
    public void setSize(String size) {
        this.size = size;
    }
}
