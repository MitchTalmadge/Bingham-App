package com.aptitekk.binghamapp.Events;

public enum DayType {

    A_DAY("A Day"),
    B_DAY("B Day"),
    OTHER(null);

    private String friendlyName;

    private DayType(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    public String getFriendlyName() {
        return friendlyName;
    }
}
