package com.ericsson.component.aia.services.eps.builtin.components.mesa.event.sample;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.event.AbstractEvent;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.event.sample.KpiInterval;

public class LTE_CELL_EVENT_CNT_1MIN extends AbstractEvent implements Serializable {

	private static final long serialVersionUID = 3948838801228331500L;
	
	private long id;
    private String description;
    private int numberOfErrors;
    private int numberOfSuccesses;
    private int enodebId;
    private int cellId;
    private int year;
    private int month;
    private int day;
    private int hour;
    private int minute;
    private int impactedSubscribers;
    private KpiInterval kpiInterval;

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public int getNumberOfErrors() {
        return numberOfErrors;
    }

    public void setNumberOfErrors(final int numberOfErrors) {
        this.numberOfErrors = numberOfErrors;
    }

    public int getNumberOfSuccesses() {
        return numberOfSuccesses;
    }

    public void setNumberOfSuccesses(final int numberOfSuccesses) {
        this.numberOfSuccesses = numberOfSuccesses;
    }

    public int getEnodebId() {
        return enodebId;
    }

    public void setEnodebId(final int enodebId) {
        this.enodebId = enodebId;
    }

    public int getCellId() {
        return cellId;
    }

    public void setCellId(final int cellId) {
        this.cellId = cellId;
    }

    public int getYear() {
        if (year == 0) {
            final Calendar c = Calendar.getInstance();
            c.setTimeInMillis(getTimestamp());
            year = c.get(Calendar.YEAR);
        }
        return year;
    }

    public void setYear(final int year) {
        this.year = year;
    }

    public int getMonth() {
        if (month == 0) {
            final Calendar c = Calendar.getInstance();
            c.setTimeInMillis(getTimestamp());
            month = c.get(Calendar.MONTH);
        }
        return month;
    }

    public void setMonth(final int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(final int day) {
        this.day = day;
    }

    public int getHour() {
        if (hour == 0) {
            final Calendar c = Calendar.getInstance();
            c.setTimeInMillis(getTimestamp());
            hour = c.get(Calendar.HOUR_OF_DAY);
        }
        return hour;
    }

    public void setHour(final int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        if (minute == 0) {
            final Calendar c = Calendar.getInstance();
            c.setTimeInMillis(getTimestamp());
            minute = c.get(Calendar.MINUTE);
        }
        return minute;
    }

    public void setMinute(final int minute) {
        this.minute = minute;
    }

    public int getImpactedSubscribers() {
        return impactedSubscribers;
    }

    public void setImpactedSubscribers(final int impactedSubscribers) {
        this.impactedSubscribers = impactedSubscribers;
    }

    public KpiInterval getKpiInterval() {
        return kpiInterval;
    }

    public void setKpiInterval(final KpiInterval kpiInterval) {
        this.kpiInterval = kpiInterval;
    }

    @Override
    public String toString() {
        final SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        return "KPI [id=" + id + ", name=" + getName() + ", description=" + description + ", resourceId=" + getResourceId() + ", timestamp=" + format.format(new Date(getTimestamp())) + ", numberOfErrors="
                + numberOfErrors + ", numberOfSuccesses=" + numberOfSuccesses + ", enodebId=" + enodebId + ", cellId=" + cellId + ", year=" + getYear() + ", month=" + getMonth() + ", day=" + day
                + ", hour=" + hour + ", minute=" + getMinute() + ", impactedSubscribers=" + impactedSubscribers + ", kpiInterval=" + kpiInterval + "]";
    }
}
