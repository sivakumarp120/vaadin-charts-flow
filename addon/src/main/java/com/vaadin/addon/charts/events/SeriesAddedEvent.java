package com.vaadin.addon.charts.events;

import com.vaadin.addon.charts.model.Series;

/**
 * Event for information about a new series to be added
 * 
 * @since 4.0
 *
 */
public class SeriesAddedEvent extends AbstractSeriesEvent {

    /**
     * Constructs the event.
     *
     * @param series
     *            The series that is to be added
     */
    public SeriesAddedEvent(Series series) {
        super(series);
    }

}