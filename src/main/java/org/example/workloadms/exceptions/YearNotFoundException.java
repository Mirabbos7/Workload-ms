package org.example.workloadms.exceptions;

public class YearNotFoundException extends RuntimeException{
    public YearNotFoundException(String message){
        super(message);
    }
}
