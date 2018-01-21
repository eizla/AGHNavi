package com.aghnavi.agh_navi;


import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.aghnavi.agh_navi.dmsl.nav.AnyPlaceSearchingHelper;
import com.aghnavi.agh_navi.dmsl.nav.AnyUserData;
import com.aghnavi.agh_navi.dmsl.nav.IPoisClass;
import com.aghnavi.agh_navi.dmsl.nav.PoisModel;
import com.aghnavi.agh_navi.dmsl.tasks.AnyplaceSuggestionsTask;
import com.aghnavi.agh_navi.dmsl.utils.GeoPoint;
import com.aghnavi.agh_navi.dmsl.utils.PopularRecentContainer;
import com.aghnavi.agh_navi.fabric.FabricInitializer;

import java.util.ArrayList;
import java.util.List;

public class PointToPointActivity extends AppCompatActivity implements FabricInitializer {

    private AnyplaceSuggestionsTask mSuggestionsTask;
    private AnyUserData userData;
    private IPoisClass source;
    private IPoisClass destination;
    private PopularRecentContainer searchContainer;

    private LinearLayout ptpLayout;

    private SearchView sourceSearchView;
    private SearchView destinationSearchView;
    private ListView recentList;
    private ListView popularList;
    private Button navigateButton;
    private AnyPlaceSearchingHelper.SearchTypes searchType = AnyPlaceSearchingHelper.SearchTypes.INDOOR_MODE;

    private final static int USED_RECENT_OR_POPULAR = 1116;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeFabric(this);
        setContentView(R.layout.point_to_point_activity);

        //TODO: not working with parcelable
        Bundle b = this.getIntent().getExtras();
        if (b != null)
            userData = b.getParcelable("userdata");

        searchContainer = (PopularRecentContainer) this.getIntent().getSerializableExtra("search_container");
        userData = GlobalContextSingleton.getInstance(this).getUserData();

        final SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        sourceSearchView = (SearchView) findViewById(R.id.source_searchview);
        sourceSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        sourceSearchView.setQueryHint("Source");
        sourceSearchView.setAddStatesFromChildren(true);
        sourceSearchView.setIconified(false);
        sourceSearchView.setIconifiedByDefault(false);
        sourceSearchView.setSubmitButtonEnabled(false);
        sourceSearchView.setQueryRefinementEnabled(false);

        sourceSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(final String newText) {
                // return false; // false since we do not handle this call

                if (newText == null || newText.trim().length() < 1) {
                    if (mSuggestionsTask != null && !mSuggestionsTask.isCancelled()) {
                        mSuggestionsTask.cancel(true);
                    }
                    sourceSearchView.setSuggestionsAdapter(null);
                    return true;
                }
                if (mSuggestionsTask != null) {
                    mSuggestionsTask.cancel(true);
                }
                if (searchType == AnyPlaceSearchingHelper.SearchTypes.INDOOR_MODE) {
                    if (!userData.isFloorSelected()) {
                        List<IPoisClass> places = new ArrayList<>(1);
                        PoisModel pm = new PoisModel();
                        pm.name = "Load a building first ...";
                        places.add(pm);
                        Cursor cursor = AnyPlaceSearchingHelper.prepareSearchViewCursor(places);
                        showSearchResult(cursor, sourceSearchView);
                        return true;
                    }
                }
                GeoPoint gp = userData.getLatestUserPosition();

                mSuggestionsTask = new AnyplaceSuggestionsTask(new AnyplaceSuggestionsTask.AnyplaceSuggestionsListener() {
                    @Override
                    public void onSuccess(String result, List<? extends IPoisClass> pois) {
                        showSearchResult(AnyPlaceSearchingHelper.prepareSearchViewCursor(pois, newText), sourceSearchView);
                    }

                    @Override
                    public void onErrorOrCancel(String result) {
                        Log.d("AnyplaceSuggestions", result);
                    }

                    @Override
                    public void onUpdateStatus(String string, Cursor cursor) {
                        showSearchResult(cursor, sourceSearchView);
                    }
                }, PointToPointActivity.this, searchType, (gp == null) ? new GeoPoint(35.144569, 33.411107) : gp, newText, true);
                mSuggestionsTask.execute(null, null);
                // we return true to avoid caling the provider set in the xml
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
        });


        destinationSearchView = (SearchView) findViewById(R.id.destination_searchview);
        destinationSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        destinationSearchView.setQueryHint("Destination");
        destinationSearchView.setAddStatesFromChildren(true);
        destinationSearchView.setIconified(false);
        destinationSearchView.setIconifiedByDefault(false);
        destinationSearchView.setSubmitButtonEnabled(false);
        destinationSearchView.setQueryRefinementEnabled(false);

        destinationSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(final String newText) {
                // return false; // false since we do not handle this call

                if (newText == null || newText.trim().length() < 1) {
                    if (mSuggestionsTask != null && !mSuggestionsTask.isCancelled()) {
                        mSuggestionsTask.cancel(true);
                    }
                    sourceSearchView.setSuggestionsAdapter(null);
                    return true;
                }
                if (mSuggestionsTask != null) {
                    mSuggestionsTask.cancel(true);
                }
                if (searchType == AnyPlaceSearchingHelper.SearchTypes.INDOOR_MODE) {
                    if (!userData.isFloorSelected()) {
                        List<IPoisClass> places = new ArrayList<IPoisClass>(1);
                        PoisModel pm = new PoisModel();
                        pm.name = "Load a building first ...";
                        places.add(pm);
                        Cursor cursor = AnyPlaceSearchingHelper.prepareSearchViewCursor(places);
                        showSearchResult(cursor, destinationSearchView);
                        return true;
                    }
                }
                GeoPoint gp = userData.getLatestUserPosition();

                mSuggestionsTask = new AnyplaceSuggestionsTask(new AnyplaceSuggestionsTask.AnyplaceSuggestionsListener() {
                    @Override
                    public void onSuccess(String result, List<? extends IPoisClass> pois) {
                        showSearchResult(AnyPlaceSearchingHelper.prepareSearchViewCursor(pois, newText), destinationSearchView);
                    }

                    @Override
                    public void onErrorOrCancel(String result) {
                        Log.d("AnyplaceSuggestions", result);
                    }

                    @Override
                    public void onUpdateStatus(String string, Cursor cursor) {
                        showSearchResult(cursor, destinationSearchView);
                    }
                }, PointToPointActivity.this, searchType, (gp == null) ? new GeoPoint(35.144569, 33.411107) : gp, newText, false);
                mSuggestionsTask.execute(null, null);
                // we return true to avoid caling the provider set in the xml
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
        });

        recentList = (ListView) findViewById(R.id.recentList);
        popularList = (ListView) findViewById(R.id.popularList);

        recentList.setAdapter(new ArrayAdapter<>(this, R.layout.recent_listview, R.id.recentItemName, searchContainer.mapToRecentStringArray()));
        popularList.setAdapter(new ArrayAdapter<>(this, R.layout.popular_listview, R.id.popularItemName, searchContainer.mapToPopularStringArray()));


        recentList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                IPoisClass place = (IPoisClass) searchContainer.getRecent().toArray()[position];
                processFinish(place);
            }
        });

        popularList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                IPoisClass place = (IPoisClass) searchContainer.getFrequentImmutablePopular().toArray()[position];
                processFinish(place);
            }
        });


        navigateButton = (Button) findViewById(R.id.navigateButton);
        navigateButton.setEnabled(false);
        navigateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processFinish(null);
            }
        });

        ptpLayout = (LinearLayout) findViewById(R.id.ptp_layout);
        ptpLayout.requestFocus();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        String action = intent.getAction();
        String data = intent.getDataString();
        if(Intent.ACTION_VIEW.equals(action)) {
            IPoisClass point = AnyPlaceSearchingHelper.getClassfromJson(data);
            if(point.source()) {
                source = point;
                sourceSearchView.setQuery(source.name(), false);
                sourceSearchView.clearFocus();
                destinationSearchView.requestFocus();
            }
            else {
                destination = point;
                destinationSearchView.setQuery(destination.name(), false);
                destinationSearchView.clearFocus();
                ptpLayout.requestFocus();
            }

            if(source != null && destination != null) {
                navigateButton.setEnabled(true);
            }
        }
        else {
            Log.d("PTP", "You will navigate to fucking nowhere apparently");
        }
    }

    private void showSearchResult(Cursor cursor, SearchView searchView) {
        String[] from = {SearchManager.SUGGEST_COLUMN_TEXT_1
                // ,SearchManager.SUGGEST_COLUMN_TEXT_2
        };
        int[] to = {android.R.id.text1
                // ,android.R.id.text2
        };
        AnyPlaceSearchingHelper.HTMLCursorAdapter adapter = new AnyPlaceSearchingHelper.HTMLCursorAdapter(PointToPointActivity.this, R.layout.anyplace_queried_pois_item_1_searchbox, cursor, from, to);
        searchView.setSuggestionsAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private void processFinish(IPoisClass recentOrPopular) {
        Intent returnIntent = new Intent();
        if(recentOrPopular == null) {
            returnIntent.putExtra("source_point", source);
            returnIntent.putExtra("destination_point", destination);
            searchContainer.add(destination);
            returnIntent.putExtra("search_container", searchContainer);
            setResult(RESULT_OK, returnIntent);
            finish();
        }
        else {
            returnIntent.putExtra("used_rec_pop", recentOrPopular);
            searchContainer.add(recentOrPopular);
            returnIntent.putExtra("search_container", searchContainer);
            setResult(USED_RECENT_OR_POPULAR, returnIntent);
            finish();
        }
    }
}
