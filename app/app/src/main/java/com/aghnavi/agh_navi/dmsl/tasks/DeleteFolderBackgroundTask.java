package com.aghnavi.agh_navi.dmsl.tasks;


import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;

public class DeleteFolderBackgroundTask extends AsyncTask<Void, Void, Void> {

    public interface DeleteFolderBackgroundTaskListener {
        void onSuccess();
    }

    private DeleteFolderBackgroundTaskListener mListener = null;
    private Boolean showDialog = true;
    private ProgressDialog dialog;
    private Context ctx;
    private File[] mParams;
    private String[] mParams2;

    public DeleteFolderBackgroundTask(Context ctx) {
        this.ctx = ctx;
    }

    public DeleteFolderBackgroundTask(DeleteFolderBackgroundTaskListener deleteFolderBackgroundTaskListener, Context ctx, Boolean showDialog) {
        this.mListener = deleteFolderBackgroundTaskListener;
        this.ctx = ctx;
        this.showDialog = showDialog;
    }

    public void setFiles(File... params) {
        this.mParams = params;
    }

    public void setFiles(String[] params) {
        this.mParams2 = params;
    }

    @Override
    protected void onPreExecute() {
        if (showDialog) {
            dialog = new ProgressDialog(ctx);
            dialog.setCancelable(false);
            dialog.setIndeterminate(true);
            dialog.setTitle("Deleting");
            dialog.setMessage("Proszę poczekać...");
            dialog.show();
        }
    }

    @Override
    protected Void doInBackground(Void... params) {
        if (mParams != null) {
            for (File f : mParams) {
                deleteDirRecursively(f);
            }
        }
        if (mParams2 != null) {
            for (String f : mParams2) {
                deleteDirRecursively(new File(f));
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void v) {
        if (showDialog)
            dialog.dismiss();
        if (mListener != null)
            mListener.onSuccess();

    }

    private void deleteDirRecursively(File f) {
        if (f.exists()) {
            for (File t : f.listFiles()) {
                if (t.isDirectory()) {
                    deleteDirRecursively(t);
                }
                if(t.delete()) {
                    Log.e("DELETE", t.getAbsolutePath() + " was deleted");
                } else {
                    Log.e("DELETE", t.getAbsolutePath() + " was NOT deleted");
                }
            }
        }
    }
}

