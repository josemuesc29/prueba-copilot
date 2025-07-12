package com.imaginamos.farmatodo.model.algolia;

import java.util.List;

public class AlgoliaReminder {

    private String objectID;
    private List<ReminderAlgolia> reminders;

    public String getObjectID() {
        return objectID;
    }

    public void setObjectID(String objectID) {
        this.objectID = objectID;
    }

    public List<ReminderAlgolia> getReminders() {
        return reminders;
    }

    public void setReminders(List<ReminderAlgolia> reminders) {
        this.reminders = reminders;
    }
}
