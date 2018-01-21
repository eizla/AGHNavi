package com.aghnavi.agh_navi.anyplace.old.activity;


import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.Html;
import android.text.Spanned;
import android.text.TextPaint;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.aghnavi.agh_navi.R;
import com.aghnavi.agh_navi.dmsl.nav.AnyPlaceSearchingHelper;
import com.aghnavi.agh_navi.dmsl.nav.IPoisClass;
import com.aghnavi.agh_navi.dmsl.tasks.AnyplaceSuggestionsTask;
import com.aghnavi.agh_navi.dmsl.utils.AndroidUtils;
import com.aghnavi.agh_navi.dmsl.utils.GeoPoint;
import com.aghnavi.agh_navi.fabric.FabricInitializer;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchPOIActivity extends FragmentActivity implements FabricInitializer {

    private TextView txtResultsFound;
    private ListView lvResultPois;

    private List<Spanned> mQueriedPoisStr;
    private List<? extends IPoisClass> mQueriedPois;

    private AnyPlaceSearchingHelper.SearchTypes mSearchType;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeFabric(this);

        setTitle("Miejsca w budynku");
        setContentView(R.layout.anyplace_activity_search_poi);

        txtResultsFound = (TextView) findViewById(R.id.txtResultsFound);

        lvResultPois = (ListView) findViewById(R.id.lvResultPois);
        lvResultPois.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    // handle the case of Google Place
                    IPoisClass place = null;
                    if (mSearchType == AnyPlaceSearchingHelper.SearchTypes.INDOOR_MODE) {
                        place = mQueriedPois.get(position);
                    } else if (mSearchType == AnyPlaceSearchingHelper.SearchTypes.OUTDOOR_MODE) {
                        place = mQueriedPois.get(position);
                    }
                    finishSearch("Success!", place);
                } catch (ArrayIndexOutOfBoundsException e) {
                    Toast.makeText(getApplicationContext(), "Something went wrong with your selection!", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        });
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            // get the search type
            mSearchType = (AnyPlaceSearchingHelper.SearchTypes) intent.getSerializableExtra("searchType");
            if (mSearchType == null)
                finishSearch("No search type provided!", null);

            // get the query string
            final String query = intent.getStringExtra("query");
            double lat = intent.getDoubleExtra("lat", 0);
            double lng = intent.getDoubleExtra("lng", 0);

            AnyplaceSuggestionsTask mSuggestionsTask = new AnyplaceSuggestionsTask(new AnyplaceSuggestionsTask.AnyplaceSuggestionsListener() {
                @Override
                public void onSuccess(String result, List<? extends IPoisClass> pois) {

                    // we have pois to query for a match
                    mQueriedPoisStr = new ArrayList<Spanned>();
                    mQueriedPois = pois;

                    // Display part of Description Text Only
                    // Make an approximation of available space based on map size
                    final int viewWidth = (int) (findViewById(R.id.txtResultsFound).getWidth() * 2);
                    View infoWindow = getLayoutInflater().inflate(R.layout.anyplace_queried_pois_item_1_searchactivity, null);
                    TextView infoSnippet = (TextView) infoWindow;
                    TextPaint paint = infoSnippet.getPaint();

                    // Regular expression
                    // ?i ignore case
                    Pattern pattern = Pattern.compile(String.format("((?i)%s)", query));

                    for (IPoisClass pm : pois) {
                        String name = "", description = "";
                        Matcher m;
                        m = pattern.matcher(pm.name());
                        // Makes matched query bold using HTML format
                        // $1 returns the regular's expression outer parenthesis value
                        name = m.replaceAll("<b>$1</b>");

                        m = pattern.matcher(pm.description());
                        if (m.find()) {
                            // Makes matched query bold using HTML format
                            // $1 returns the regular's expression outer parenthesis value
                            int startIndex = m.start();
                            description = m.replaceAll("<b>$1</b>");
                            description = AndroidUtils.fillTextBox(paint, viewWidth, description, startIndex + 3);
                        }
                        mQueriedPoisStr.add(Html.fromHtml(name + "<br>" + description));
                    }

                    ArrayAdapter<Spanned> mAdapter = new ArrayAdapter<Spanned>(
                            // getBaseContext(), R.layout.queried_pois_item_1,
                            getBaseContext(), R.layout.anyplace_queried_pois_item_1_searchactivity, mQueriedPoisStr);
                    lvResultPois.setAdapter(mAdapter);
                    txtResultsFound.setText("Znalezione miejsca [ " + mQueriedPoisStr.size() + " ]");

                }

                @Override
                public void onErrorOrCancel(String result) {
                    // no pois exist
                    finishSearch("Brak pasujących miejsc!", null);
                }

                @Override
                public void onUpdateStatus(String string, Cursor cursor) {
                    SimpleCursorAdapter adapter = new SimpleCursorAdapter(getBaseContext(), R.layout.anyplace_queried_pois_item_1_searchactivity, cursor, new String[] { SearchManager.SUGGEST_COLUMN_TEXT_1 }, new int[] { android.R.id.text1 });
                    lvResultPois.setAdapter(adapter);
                    txtResultsFound.setText("Znalezione miejsca [ " + cursor.getCount() + " ]");
                }

            }, this, mSearchType, new GeoPoint(lat, lng), query, false);
            mSuggestionsTask.execute();
        }
    }

    /**
     * Returns an IAnyPlace object to the activity that initiated this search We use an IAnyPlace in order to allow the searching between GooglePlaces and AnyPlace POIs at the same time.
     *
     * @param result
     * @param place
     */
    private void finishSearch(String result, IPoisClass place) {
        if (place == null) {
            // we have an error
            Intent returnIntent = new Intent();
            returnIntent.putExtra("message", result);
            setResult(RESULT_CANCELED, returnIntent);
            finish();
        } else {
            Intent returnIntent = new Intent();
            returnIntent.putExtra("ianyplace", place);
            returnIntent.putExtra("message", result);
            setResult(RESULT_OK, returnIntent);
            finish();
        }
    }
}
