package com.aptitekk.binghamapp.cards;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.aptitekk.binghamapp.R;

import it.gmariotti.cardslib.library.cards.topcolored.TopColoredCard;

/**
 * Created by kevint on 8/18/2015.
 */
public class MarkedCalendarEventCard extends TopColoredCard implements CalendarEventView {

    TextView location;

    public MarkedCalendarEventCard(Context context) {
        super(context);
        this.with(context)
                .setColorResId(R.color.marked_event)
                .setupSubLayoutId(R.layout.card_calendar_event)
                .setupInnerElements(new TopColoredCard.OnSetupInnerElements() {
                    @Override
                    public void setupInnerViewElementsSecondHalf(View secondHalfView) {
                        secondHalfView.findViewById(R.id.title).setVisibility(View.GONE);
                        secondHalfView.findViewById(R.id.duration).setVisibility(View.GONE);
                        location = (TextView) secondHalfView.findViewById(R.id.location);
                    }
                })
                .build();

        setTitleOverColorResId(R.color.inverse_primary_text);
        setSubTitleOverColorResId(R.color.inverse_secondary_text);
    }

    @Override
    public void setTitle(String title) {
        this.setTitleOverColor(title);
    }

    @Override
    public void setDuration(String formattedDuration) {
        this.setSubTitleOverColor(formattedDuration);
    }

    @Override
    public void setLocation(String location) {
        this.location.setText(location);
    }

    @Override
    public String getTitle() {
        return this.getTitleOverColor().toString();
    }

    @Override
    public String getDuration() {
        return this.getSubTitleOverColor().toString();
    }

    @Override
    public String getLocation() {
        return this.location.getText().toString();
    }
}
