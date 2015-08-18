package com.aptitekk.binghamapp.cards;

import android.content.Context;

import com.aptitekk.binghamapp.rssGoogleCalendar.CalendarEvent;

import it.gmariotti.cardslib.library.cards.material.MaterialLargeImageCard;
import it.gmariotti.cardslib.library.cards.topcolored.TopColoredCard;
import it.gmariotti.cardslib.library.internal.Card;

/**
 * Created by kevint on 8/18/2015.
 */
public class CalendarEventCard extends MaterialLargeImageCard implements CalendarEventView{

    public CalendarEventCard(Context context) {
        super(context);
    }

    @Override
    public void setTitle(String title) {
        this.setTextOverImage(title);
    }

    public void setDuration(String formattedDuration) {
        this.setTitle(formattedDuration);
    }

    public void setLocation(String formattedLocation) {
        this.setSubTitle(formattedLocation);
    }

    @Override
    public String getTitle() {
        return this.getTextOverImage().toString();
    }

    @Override
    public String getDuration() {
        return super.getTitle();
    }

    @Override
    public String getLocation() {
        return this.getSubTitle().toString();
    }


}
