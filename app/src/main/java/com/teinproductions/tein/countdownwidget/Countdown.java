package com.teinproductions.tein.countdownwidget;


public class Countdown {

    private String name;
    private boolean showName;
    private long millis;

    public Countdown(String name, boolean showName, long millis) {
        this.name = name;
        this.showName = showName;
        this.millis = millis;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isShowName() {
        return showName;
    }

    public void setShowName(boolean showName) {
        this.showName = showName;
    }

    public long getMillis() {
        return millis;
    }

    public void setMillis(long millis) {
        this.millis = millis;
    }
}
