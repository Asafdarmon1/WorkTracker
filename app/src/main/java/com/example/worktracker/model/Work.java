package com.example.worktracker.model;

import java.util.Date;

//containing the extracted data from firebase
public class Work {

    private String date;
    private String start;
    private String end;
    private String hours;
    private String salary;

    public Work(){}
    public Work(String date,String start,String end,String hours,String salary){
        this.date = date;
        this.start = start;
        this.end = end;
        this.hours = hours;
        this.salary = salary;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public String getHours() {
        return hours;
    }

    public void setHours(String hours) {
        this.hours = hours;
    }

    public String getSalary() {
        return salary;
    }

    public void setSalary(String salary) {
        this.salary = salary;
    }
}
