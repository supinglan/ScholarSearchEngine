package org.example;

import java.util.HashMap;
import java.util.Map;

public class Paper {
    private String title;
    private String author;
    private String abstractText;
    private int year;
    private String conference;
    private Map<String, String> info;

    private String path;
    private String text;

    public Paper(String title, String author, String abstractText, int year, String conference, Map<String, String> info, String path,String text) {
        this.title = title;
        this.author = author;
        this.abstractText = abstractText;
        this.year = year;
        this.conference = conference;
        this.info = info;
        this.path = path;
        this.text = text;
    }

    // Getter and Setter methods for the attributes

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAbstractText() {
        return abstractText;
    }

    public void setAbstractText(String abstractText) {
        this.abstractText = abstractText;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getConference() {
        return conference;
    }

    public void setConference(String conference) {
        this.conference = conference;
    }

    public Map<String, String> getInfo() {
        return info;
    }

    public void setInfo(Map<String, String> info) {
        this.info = info;
    }

    // Additional methods to modify info map

    public void addInfo(String key, String value) {
        if (info == null) {
            info = new HashMap<>();
        }
        info.put(key, value);
    }

    public void removeInfo(String key) {
        if (info != null) {
            info.remove(key);
        }
    }
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text= text;
    }
}
