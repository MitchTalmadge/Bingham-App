package com.aptitekk.binghamapp.Events;

public enum LunchType {

    AA_AB("A Lunch on A Days, A Lunch on B Days"),
    AA_BB("A Lunch on A Days, B Lunch on B Days"),
    BA_AB("B Lunch on A Days, A Lunch on B Days"),
    BA_BB("B Lunch on A Days, B Lunch on B Days");

    private String description;

    private LunchType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
