package org.example.workloadms.exceptions;

public class MonthNotFoundException extends RuntimeException{
    public MonthNotFoundException(String message){
        super(message);
    }
}
