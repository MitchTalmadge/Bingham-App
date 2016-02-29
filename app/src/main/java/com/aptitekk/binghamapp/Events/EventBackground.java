package com.aptitekk.binghamapp.Events;

import com.aptitekk.binghamapp.R;

public enum EventBackground {

    FOOTBALL("football", R.drawable.event_football),
    TENNIS("tennis", R.drawable.event_tennis),
    DANCE("dance", R.drawable.event_dance),
    MINERETTES("minerette", R.drawable.event_dance),
    VOLLEYBALL("volleyball", R.drawable.event_volleyball),
    BASKETBALL("basketball", R.drawable.event_basketball),
    BASKETBALL2("bball", R.drawable.event_basketball),
    BASEBALL("baseball", R.drawable.event_baseball),
    SOCCER("soccer", R.drawable.event_soccer),
    WRESTLING("wrestling", R.drawable.event_wrestling),
    SWIMMING("swimming", R.drawable.event_swimming),
    TRACK("track", R.drawable.event_track),
    CHOIR("choir", R.drawable.event_choir),
    SYMPHONY("symphony", R.drawable.event_symphony),
    CONCERT("concerto", R.drawable.event_symphony),
    ORCHESTRA("orchestra", R.drawable.event_symphony),
    BAND("band", R.drawable.event_symphony),
    JAZZ("jazz", R.drawable.event_jazz),
    CANDLE("candle", R.drawable.event_candle),
    PLAY("play", R.drawable.event_play),
    COUNCIL("council", R.drawable.event_council),
    ZEROFATALITIES("fatalities", R.drawable.event_zerofatalities),
    PTC("parent teacher conference", R.drawable.event_grades),
    SPIRIT("spirit", R.drawable.event_spirit),
    TEST("test", R.drawable.event_test),
    FCCLA("fccla", R.drawable.event_fccla),
    ART("art show", R.drawable.event_art),
    ASSEMBLY("assembly", R.drawable.event_assembly),
    TALENT_SHOW("talent show", R.drawable.event_assembly),
    HOLIDAY("no school", R.drawable.event_noschool),
    HOLIDAY2("no students attend", R.drawable.event_noschool),
    LAST_DAY("last day of school", R.drawable.event_lastday);

    String value;
    int drawableId;

    EventBackground(String value, int drawableId) {
        this.value = value;
        this.drawableId = drawableId;
    }

    public String getValue() {
        return value;
    }

    public int getImageDrawableId() {
        return drawableId;
    }

}
