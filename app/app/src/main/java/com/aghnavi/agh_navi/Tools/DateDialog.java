package com.aghnavi.agh_navi.Tools;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;

import com.aghnavi.agh_navi.Calendar.AddEventActivityNew;
import com.aghnavi.agh_navi.Calendar.EditEventActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DateDialog extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    private String mCallingActivity;
    private String mEditTextName;

    public static DateDialog newInstance() { return new DateDialog();  }

    public Dialog onCreateDialog(Bundle savedInstanceState) {

        super.onCreateDialog(savedInstanceState);
        mEditTextName = (String) getArguments().get("name");
        mCallingActivity = (String) getArguments().get("activity");

        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        return new DatePickerDialog(getActivity(), this, year, month, day);


    }

    public void onDateSet(DatePicker view, int year, int month, int day) {

        List<String> args = new ArrayList<>();
        args.add(mEditTextName);
        String date=day+"-"+(month+1)+"-"+year;
        args.add(date);

        switch (mCallingActivity){
            case "AddEventActivityNew":
                ((AddEventActivityNew)getActivity()).onDateSet(args);
                break;
            case "EditEventActivity":
                ((EditEventActivity)getActivity()).onDateSet(args);
                break;
        }

    }



}
