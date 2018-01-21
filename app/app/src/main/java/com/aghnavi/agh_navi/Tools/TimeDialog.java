package com.aghnavi.agh_navi.Tools;


import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.EditText;
import android.widget.TimePicker;


import com.aghnavi.agh_navi.Calendar.AddEventActivityNew;
import com.aghnavi.agh_navi.Calendar.EditEventActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TimeDialog extends DialogFragment implements TimePickerDialog.OnTimeSetListener{

    private String mEditTextName;
    private String mCallingActivity;

    public static TimeDialog newInstance() { return new TimeDialog();}



    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){

        super.onCreateDialog(savedInstanceState);
        mEditTextName = (String) getArguments().get("name");
        mCallingActivity = (String) getArguments().get("activity");
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);


        return new TimePickerDialog(getActivity(),this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }


    public void onTimeSet(TimePicker view, int hourOfDay, int minute){

        List<String> args = new ArrayList<>();
        args.add(mEditTextName);
        args.add(String.valueOf(hourOfDay) + ":" + String.valueOf(minute));

        switch (mCallingActivity){
            case "AddEventActivityNew":
                ((AddEventActivityNew)getActivity()).onTimeSet(args);
                break;
            case "EditEventActivity":
                ((EditEventActivity)getActivity()).onTimeSet(args);
                break;
        }

    }

}
