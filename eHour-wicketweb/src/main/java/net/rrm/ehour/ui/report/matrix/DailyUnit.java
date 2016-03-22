package net.rrm.ehour.ui.report.matrix;

import java.util.Date;


public class DailyUnit {

    Number hours;
    Date date;

    public DailyUnit() {}

    public Number getHours() {
        return hours;
    }

    public void setHours(Number hours) {
        this.hours = hours;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "DailyUnit{" +
                "hours=" + hours +
                ", date=" + date +
                '}';
    }
}
