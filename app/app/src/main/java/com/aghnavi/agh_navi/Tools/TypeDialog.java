package com.aghnavi.agh_navi.Tools;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.ArrayAdapter;

import com.aghnavi.agh_navi.Calendar.AddEventActivityNew;
import com.aghnavi.agh_navi.Calendar.EditEventActivity;

import java.util.Arrays;
import java.util.List;


public class TypeDialog extends DialogFragment implements DialogInterface.OnClickListener {

    String[] eventTypesArray = new String[] {"LABORATORIUM", "ĆWICZENIA", "WYKŁAD", "EGZAMIN", "KOLOKWIUM", "KARTKÓWKA", "INNE"};
    private List<String> mEventTypes = Arrays.asList(eventTypesArray);
    private int mSelectedItem = 0;
    private String mCallingActivity;

    public static TypeDialog newInstance(){
        return new TypeDialog();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        super.onCreateDialog(savedInstanceState);
        mCallingActivity = (String) getArguments().get("activity");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle("Typ wydarzenia");

        builder.setPositiveButton(
                "OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (mCallingActivity){
                            case "AddEventActivityNew":
                                ((AddEventActivityNew)getActivity()).doPositiveClick(eventTypesArray[mSelectedItem]);
                                break;
                            case "EditEventActivity":
                                ((EditEventActivity)getActivity()).doPositiveClick(eventTypesArray[mSelectedItem]);
                                break;
                        }

                    }
                }
        );

        builder.setNegativeButton(
                "ANULUJ",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getDialog().cancel();
                    }
                }
        );

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity().getApplicationContext(), android.R.layout.simple_list_item_1, mEventTypes);
        builder.setSingleChoiceItems(eventTypesArray, 0, this);

        return builder.create();
    }


    @Override
    public void onClick(DialogInterface dialog, int which) {
        this.mSelectedItem = which;
    }
}
