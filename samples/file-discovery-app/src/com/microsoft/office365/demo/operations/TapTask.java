package com.microsoft.office365.demo.operations;

import java.util.concurrent.Callable;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.widget.Toast;

public class TapTask<T> extends AsyncTask<Void, Void, T> {
    
    Activity mActivity;
    Callable<T> mAction;
    Throwable mError = null;

    private ProgressDialog mDialog;
    
    public TapTask(Activity activity, Callable<T> action) {
        mActivity = activity;
        mAction = action;
    }
    
    protected void onPreExecute() {
        mDialog = new ProgressDialog(mActivity);
        mDialog.setTitle("Working...");
        mDialog.setMessage("Please wait.");
        mDialog.setCancelable(false);
        mDialog.setIndeterminate(true);
        mDialog.show();
    }
    
    public Throwable getError() {
        return mError;
    }

    @Override
    protected T doInBackground(Void... arg0) {
        try {
            return mAction.call();
        } catch (Exception e) {
            mError = e;
            return null;
        }
    }
    
    @Override
    protected void onPostExecute(T result) {
        super.onPostExecute(result);
        
        if (mDialog.isShowing()) {
            mDialog.dismiss();
        }

        if (mError != null) {
            Toast.makeText(mActivity, "Error: " + mError.toString(), Toast.LENGTH_SHORT).show();
        } else {
            processResult(result);
        }

    }

    protected void processResult(T result) {
    }
    
    
}