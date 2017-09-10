package com.natochy.poyntsdksample;

/**
 * Created by dennis on 9/9/17.
 */

public class PoyntSdkException extends Exception {
    private String mMessage;
    public PoyntSdkException(Throwable t, String message){
        super(t);
        this.mMessage = message;
    }

    @Override
    public String toString() {
        return super.getCause().getMessage() + ": " + mMessage;
    }
}
