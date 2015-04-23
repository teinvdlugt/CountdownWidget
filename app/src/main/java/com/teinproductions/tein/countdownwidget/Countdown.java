package com.teinproductions.tein.countdownwidget;


public class Countdown {

    private String name;
    private boolean showName, showDays, showHours, showMinutes, useCapitals;
    private long millis;

    public Countdown(String name, boolean showName, boolean showDays, boolean showHours, boolean showMinutes, boolean useCapitals, long millis) {
        this.name = name;
        this.showName = showName;
        this.showDays = showDays;
        this.showHours = showHours;
        this.showMinutes = showMinutes;
        this.useCapitals = useCapitals;
        this.millis = millis;
    }

    public boolean isUseCapitals() {
        return useCapitals;
    }

    public void setUseCapitals(boolean useCapitals) {
        this.useCapitals = useCapitals;
    }

    public boolean isShowDays() {
        return showDays;
    }

    public void setShowDays(boolean showDays) {
        this.showDays = showDays;
    }

    public boolean isShowHours() {
        return showHours;
    }

    public void setShowHours(boolean showHours) {
        this.showHours = showHours;
    }

    public boolean isShowMinutes() {
        return showMinutes;
    }

    public void setShowMinutes(boolean showMinutes) {
        this.showMinutes = showMinutes;
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
