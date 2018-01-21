package com.aghnavi.agh_navi.dmsl.cache;


import android.content.Context;
import android.widget.Toast;

import com.aghnavi.agh_navi.AnyplaceAPI;
import com.aghnavi.agh_navi.MapNavigationActivity;
import com.aghnavi.agh_navi.dmsl.nav.BuildingModel;
import com.aghnavi.agh_navi.dmsl.nav.PoisModel;
import com.aghnavi.agh_navi.dmsl.tasks.FetchBuildingsTask;
import com.aghnavi.agh_navi.dmsl.utils.NetworkUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class should provide the last fetched Buildings, Floors and POIs. At the
 * moment it's just static data structures but they should be implemented as a
 * local database and being retrieved as a ContentProvider.
 */
@SuppressWarnings("serial")
public class AnyplaceCache implements Serializable {

    private static AnyplaceCache mInstance = null;
    private static Context ctx;

    public static AnyplaceCache getInstance(Context ctx) {
        if (mInstance == null) {
            synchronized (ctx) {
                if (mInstance == null) {
                    AnyplaceCache.ctx = ctx;
                    mInstance = getObject(ctx, ctx.getCacheDir());
                }
                if (mInstance == null) {
                    AnyplaceCache.ctx = ctx;
                    mInstance = new AnyplaceCache();
                }
            }
        }
        return mInstance;
    }

    public static void saveInstance(Context ctx) {
        saveObject(ctx, ctx.getCacheDir(), getInstance(ctx));
    }

    private transient BackgroundFetch bf = null;

    private int selectedBuilding = 0;
    // last fetched Buildings
    private List<BuildingModel> mSpinnerBuildings = new ArrayList<BuildingModel>(0);
    private List<BuildingModel> mWorldBuildings = new ArrayList<BuildingModel>(0);

    // last fetched pois
    private Map<String, PoisModel> mLoadedPoisMap;
    private String poisBUID;

    private AnyplaceCache() {
        // last fetched Buildings
        this.mSpinnerBuildings = new ArrayList<BuildingModel>();
        // last fetched pois
        this.mLoadedPoisMap = new HashMap<String, PoisModel>();
    }

    // </All Buildings
    public List<BuildingModel> loadWorldBuildings(final FetchBuildingsTask.FetchBuildingsTaskListener fetchBuildingsTaskListener, final Context ctx, Boolean forceReload) {
        if ((forceReload && NetworkUtils.isOnline(ctx)) || mWorldBuildings.isEmpty()) {
            new FetchBuildingsTask(new FetchBuildingsTask.FetchBuildingsTaskListener() {

                @Override
                public void onSuccess(String result, List<BuildingModel> buildings) {
                    mWorldBuildings = buildings;
                    AnyplaceCache.saveInstance(ctx);
                    fetchBuildingsTaskListener.onSuccess(result, buildings);
                }

                @Override
                public void onErrorOrCancel(String result) {
                    fetchBuildingsTaskListener.onErrorOrCancel(result);
                }

            }, ctx).execute();
        } else {
            fetchBuildingsTaskListener.onSuccess("Successfully read from cache", mWorldBuildings);
        }
        return mWorldBuildings;
    }

    public void loadBuilding(final String buid, final BuildingModel.FetchBuildingTaskListener l, Context ctx) {

        loadWorldBuildings(new FetchBuildingsTask.FetchBuildingsTaskListener() {

            @Override
            public void onSuccess(String result, List<BuildingModel> buildings) {
                BuildingModel fcb = null;
                for (BuildingModel b : buildings) {
                    if (b.buid.equals(buid)) {
                        fcb = b;
                        break;
                    }
                }

                if (fcb != null) {
                    l.onSuccess("Success", fcb);
                } else {
                    l.onErrorOrCancel("Building not found");
                }
            }

            @Override
            public void onErrorOrCancel(String result) {
                l.onErrorOrCancel(result);
            }
        }, ctx, false);
    }

    // </Buildings Spinner in Select Building Activity
    public List<BuildingModel> getSpinnerBuildings() {

        return mSpinnerBuildings;
    }

    public void setSpinnerBuildings(List<BuildingModel> mLoadedBuildings) {
        this.mSpinnerBuildings = mLoadedBuildings;
        AnyplaceCache.saveInstance(ctx);
    }

    // Use nav/AnyUserData for the loaded building in Navigator
    public BuildingModel getSelectedBuilding() {
        BuildingModel b = null;
        try {
            b = mSpinnerBuildings.get(selectedBuilding);
        } catch (IndexOutOfBoundsException ex) {

        }

        return b;
    }

    public int getSelectedBuildingIndex() {
        if (!(selectedBuilding < mSpinnerBuildings.size()))
            selectedBuilding = 0;

        return selectedBuilding;
    }

    public void setSelectedBuildingIndex(int selectedBuildingIndex) {
        this.selectedBuilding = selectedBuildingIndex;
    }

    // />

    // /< POIS
    public Collection<PoisModel> getPois() {
        return this.mLoadedPoisMap.values();
    }

    public Map<String, PoisModel> getPoisMap() {
        return this.mLoadedPoisMap;
    }

    public void setPois(Map<String, PoisModel> lpID, String poisBUID) {
        this.mLoadedPoisMap = lpID;
        this.poisBUID = poisBUID;
        AnyplaceCache.saveInstance(ctx);
    }

    // Check the loaded pois if match the Building ID
    public boolean checkPoisBUID(String poisBUID) {
        if (this.poisBUID != null && this.poisBUID.equals(poisBUID))
            return true;
        else
            return false;
    }

    // />POIS

    public void fetchAllFloorsRadiomapsRun(BackgroundFetchListener l, final BuildingModel build) {
        if (bf == null) {
            l.onPrepareLongExecute();
            bf = new BackgroundFetch(l, build, ctx);
            bf.run();
        } else if (!bf.build.buid.equals(build.buid)) {
            // Navigated to another building
            bf.cancel();
            l.onPrepareLongExecute();
            bf = new BackgroundFetch(l, build, ctx);
            bf.run();
        } else if (bf.status == BackgroundFetchListener.Status.SUCCESS) {
            // Previously finished for the current building
            l.onSuccess("Already Downloaded");
        } else if (bf.status == BackgroundFetchListener.Status.STOPPED) {
            // Task Download Error Occurred
            l.onErrorOrCancel("Task Failed", BackgroundFetchListener.ErrorType.EXCEPTION);
        } else {
            l.onErrorOrCancel("Another instance is running", BackgroundFetchListener.ErrorType.SINGLE_INSTANCE);
        }
    }

    public void fetchAllFloorsRadiomapReset() {
        if (bf != null)
            bf = null;
    }

    public BackgroundFetchListener.Status fetchAllFloorsRadiomapStatus() {
        return bf.status;
    }

    // />Fetch all Floor and Radiomaps of the current building

    // </SAVE CACHE
    public static boolean saveObject(Context ctx, File cacheDir, AnyplaceCache obj) {

        final File suspend_f = new File(cacheDir, "AnyplaceCache");

        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        boolean keep = true;

        try {
            fos = new FileOutputStream(suspend_f);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(obj);
        } catch (Exception e) {
            keep = false;
            if (AnyplaceAPI.DEBUG_MESSAGES)
                Toast.makeText(ctx, "AnyplaceCache: saveObject :" + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            try {
                if (oos != null)
                    oos.close();
                if (fos != null)
                    fos.close();
                if (keep == false)
                    suspend_f.delete();
            } catch (Exception e) { /* do nothing */
            }
        }

        return keep;
    }

    public static AnyplaceCache getObject(Context ctx, File cacheDir) {
        final File suspend_f = new File(cacheDir, "AnyplaceCache");

        AnyplaceCache simpleClass = null;
        FileInputStream fis = null;
        ObjectInputStream is = null;

        try {
            fis = new FileInputStream(suspend_f);
            is = new ObjectInputStream(fis);
            simpleClass = (AnyplaceCache) is.readObject();
        } catch (Exception e) {
            if (AnyplaceAPI.DEBUG_MESSAGES)
                Toast.makeText(ctx, "AnyplaceCache: getObject :" + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            try {
                if (fis != null)
                    fis.close();
                if (is != null)
                    is.close();
            } catch (Exception e) {
            }
        }

        return simpleClass;
    }
}

