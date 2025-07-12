package com.imaginamos.farmatodo.model.algolia;

import java.util.List;

public class TimeFinalizeOrders {
    private List<String> possiblesTimeVariables;
    private String timeVariable;
    private int time;


    public TimeFinalizeOrders() {
    }

    public TimeFinalizeOrders(List<String> possiblesTimeVariables, String timeVariable, int time) {
        this.possiblesTimeVariables = possiblesTimeVariables;
        this.timeVariable = timeVariable;
        this.time = time;
    }

    public List<String> getPossiblesTimeVariables() {
        return possiblesTimeVariables;
    }

    public void setPossiblesTimeVariables(List<String> possiblesTimeVariables) {
        this.possiblesTimeVariables = possiblesTimeVariables;
    }

    public String getTimeVariable() {
        return timeVariable;
    }

    public void setTimeVariable(String timeVariable) {
        this.timeVariable = timeVariable;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }
}
