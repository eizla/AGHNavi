package com.aghnavi.agh_navi.dmsl.nav;


import android.app.SearchManager;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.provider.BaseColumns;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnyPlaceSearchingHelper {

    public enum SearchTypes {
        INDOOR_MODE, OUTDOOR_MODE
    }

    public static SearchTypes getSearchType(float zoomLevel) {
        if (zoomLevel >= 19) {
            return SearchTypes.INDOOR_MODE;
        } else {
            return SearchTypes.OUTDOOR_MODE;
        }
    }

    public static Cursor prepareSearchViewCursor(List<? extends IPoisClass> places) {
        String req_columns[] = { BaseColumns._ID, SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_TEXT_2, SearchManager.SUGGEST_COLUMN_INTENT_DATA };
        MatrixCursor mcursor = new MatrixCursor(req_columns);
        int i = 0;
        if (places != null) {
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(IPoisClass.class, new IPoisClass.MyInterfaceAdapter());
            Gson gson = gsonBuilder.create();
            for (IPoisClass p : places) {
                mcursor.addRow(new String[] { Integer.toString(i++), p.name(), p.description().equals("") ? "Brak opisu." : p.description(), gson.toJson(p, IPoisClass.class) });
            }
        }
        return mcursor;
    }

    public static Cursor prepareSearchViewCursor(List<? extends IPoisClass> places, String query) {
        String req_columns[] = { BaseColumns._ID, SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_INTENT_DATA };
        MatrixCursor mcursor = new MatrixCursor(req_columns);
        int i = 0;
        if (places != null) {
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(IPoisClass.class, new IPoisClass.MyInterfaceAdapter());
            Gson gson = gsonBuilder.create();

            // Better Use AndroidUtils.fillTextBox instead of regular expression
            // Regular expression
            // ?i ignore case
            Pattern patternTitle = Pattern.compile(String.format("((?i)%s)", query));
            // Matches X words before query and Y words after
            Pattern patternDescription = Pattern.compile(String.format("(([^\\s]+\\s+){0,%d}[^\\s]*)((?i)%s)([^\\s]*(\\s+[^\\s]+){0,%d})", 2, query, 2));
            for (IPoisClass p : places) {
                String name = "", description = "";
                Matcher m;
                m = patternTitle.matcher(p.name());
                // Makes matched query bold using HTML format
                // $1 returns the regular's expression outer parenthesis value
                name = m.replaceAll("<b>$1</b>");

                m = patternDescription.matcher(p.description());
                if (m.find()) {
                    // Makes matched query bold using HTML format
                    description = String.format(" %s<b>%s</b>%s", m.group(1), m.group(3), m.group(4));
                }

                mcursor.addRow(new String[] { Integer.toString(i++), name + description, gson.toJson(p, IPoisClass.class) });
            }
        }
        return mcursor;
    }

    public static IPoisClass getClassfromJson(String data) {
        // HANDLE BOTH GooglePlace AND AnyplacePoi
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(IPoisClass.class, new IPoisClass.MyInterfaceAdapter());
        Gson gson = gsonBuilder.create();
        // String name = result.getClass().toString();
        return gson.fromJson(data, IPoisClass.class);
    }

    public static class HTMLCursorAdapter extends SimpleCursorAdapter {

        private LayoutInflater mInflater;
        private int layout;
        private String[] from;
        private int[] to;

        public HTMLCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
            super(context, layout, c, from, to, 0);
            this.layout = layout;
            this.from = from;
            this.to = to;
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            for (int i = 0; i < to.length; i++) {
                TextView textView = (TextView) view.findViewById(to[i]);
                String text = cursor.getString(cursor.getColumnIndex(from[i]));
                textView.setText(Html.fromHtml(text));
            }
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return mInflater.inflate(layout, parent, false);
        }

    }
}

