package com.aghnavi.agh_navi.dmsl.cache;


import java.io.Serializable;

//Fetch all Floor and Radiomaps of the current building
public interface BackgroundFetchListener extends Serializable {

    public enum ErrorType {
        EXCEPTION, CANCELLED, SINGLE_INSTANCE,
    }

    public enum Status {
        RUNNING, SUCCESS, STOPPED, // STOPPED {ERROR, CANCEL}
    }

    void onProgressUpdate(int progress_current, int progress_total);

    void onErrorOrCancel(String result, ErrorType error);

    void onSuccess(String result);

    void onPrepareLongExecute();

}
