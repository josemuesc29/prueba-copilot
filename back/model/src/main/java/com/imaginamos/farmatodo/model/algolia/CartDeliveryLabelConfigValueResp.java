package com.imaginamos.farmatodo.model.algolia;

import org.apache.http.impl.cookie.DateUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.logging.Logger;

public class CartDeliveryLabelConfigValueResp{
    private static final Logger LOG = Logger.getLogger(CartDeliveryLabelConfigValueResp.class.getName());

    private CartDeliveryLabelConfigValue config;
    private HighDemand highDemand;

    public CartDeliveryLabelConfigValueResp(){}

    public CartDeliveryLabelConfigValueResp(CartDeliveryLabelConfigValue value){
        this.config = value;
        this.highDemand = new HighDemand(this.config.getMandatory(), this.config.getHighDemandMessage(), this.config.getGeneralMessage(), this.config.getDateUntil(), this.config.getScheduleOrderStart(), this.config.getScheduleDuration());
    }

    public HighDemand getHighDemand() {
        return highDemand;
    }

    public void setHighDemand(HighDemand highDemand) {
        this.highDemand = highDemand;
    }

    class HighDemand {
        private List<Range> dates;
        private Boolean mandatory;
        private String highDemandMessage;
        private String generalMessage;
        private Long dateUntil;
        private Integer scheduleOrderStart;
        private Integer scheduleDuration;

        public HighDemand(){}

        public HighDemand(Boolean mandatory, String highDemandMessage, String generalMessage, Long dateUntil, Integer scheduleOrderStart, Integer scheduleDuration){
            this.mandatory = mandatory;
            this.highDemandMessage = highDemandMessage;
            this.generalMessage = generalMessage;
            this.dateUntil = dateUntil;
            this.scheduleOrderStart = scheduleOrderStart;
            this.scheduleDuration = scheduleDuration;
        }

        public List<Range> getDates() {
            long futureDates = dateUntil;
            int hourAdded = 0;
            if(Objects.nonNull(dateUntil)){
                dates = new ArrayList<>();
                do {
                    // Suma las horas a la hora final de la programación
                    Range range = new Range(addHoursToDate(futureDates, scheduleOrderStart),
                            scheduleDuration - hourAdded);
                    hourAdded += range.getHours().size();
                    dates.add(range);
                    DateTime newDate = new DateTime(futureDates, DateTimeZone.forID("America/Bogota"));
                    int hourOfDay = newDate.getHourOfDay();
                    int plus = (hourOfDay * (-1));
                    newDate = newDate.plusHours(plus);
                    futureDates = newDate.plusDays(1).getMillis();
                }while (verifyTotalHours(dates, scheduleDuration));
            }
            return dates;
        }

        public void setDates(List<Range> dates) {
            this.dates = dates;
        }

        public Boolean getMandatory() {
            return mandatory;
        }

        public void setMandatory(Boolean mandatory) {
            this.mandatory = mandatory;
        }

        public String getHighDemandMessage() {
            return highDemandMessage;
        }

        public void setHighDemandMessage(String highDemandMessage) {
            this.highDemandMessage = highDemandMessage;
        }

        public String getGeneralMessage() {
            return generalMessage;
        }

        public void setGeneralMessage(String generalMessage) {
            this.generalMessage = generalMessage;
        }

        public Long getDateUntil() { return dateUntil; }

        public void setDateUntil(Long dateUntil) { this.dateUntil = dateUntil; }
    }

    class Range {
        private Long date;
        private List<Long> hours;

        public Range(Long date, Integer scheduleDuration){
            LOG.info("add new RATE: date ----> " + date + " scheduleDuration ----> " + scheduleDuration);
            this.date = date;
            hours = new ArrayList<>();
            for(int i = 0; i <= scheduleDuration; i++){
                Long newTimestamp = addHoursToDate(date, i);
                hours.add(newTimestamp);
                DateTime newDate = new DateTime(newTimestamp, DateTimeZone.forID("America/Bogota"));
                if (newDate.getHourOfDay() == 23)
                    return;
            }
        }

        public Long getDate() {
            return date;
        }

        public void setDate(Long date) {
            this.date = date;
        }

        public List<Long> getHours() {
            return hours;
        }

        public void setHours(List<Long> hours) {
            this.hours = hours;
        }
    }

    private Long addHoursToDate(Long date, Integer hour){
        try {
            LOG.info("method: addHoursToDate date "+date+" hour:"+hour);
            DateTime endHighDemandDate = new DateTime(date, DateTimeZone.forID("America/Bogota"));
            endHighDemandDate = endHighDemandDate.plusHours(hour);
            return endHighDemandDate.getMillis();
        }catch(Exception ex){
            LOG.warning("Error:"+ex.fillInStackTrace());
            return null;
        }
    }

    private boolean verifyTotalHours(final List<Range> dates, Integer scheduleDuration) {
        Integer hours = 0;
        for (Range range: dates) {
            hours += range.getHours().size();
        }
        LOG.info("varifyTotalHours: hours ------------->" + hours + " scheduleDuration: -----> " + scheduleDuration );
        return hours < scheduleDuration;
    }

}
