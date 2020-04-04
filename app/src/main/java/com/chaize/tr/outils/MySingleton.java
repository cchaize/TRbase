package com.chaize.tr.outils;

import android.content.Context;
import com.android.volley.RequestQueue;

import com.android.volley.Request;
import com.android.volley.toolbox.Volley;

public class MySingleton {
    private static MySingleton mInstance;
    private RequestQueue requestQueue;
    private static Context mCtx;

    private MySingleton(Context context) {
        mCtx = context;
        requestQueue = getRequestQueue();
    }

    private RequestQueue getRequestQueue() {
        if (requestQueue==null) {
            requestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
            requestQueue.start();
        }
        return requestQueue;
    }

    public static synchronized MySingleton getInstance(Context context) {
        if (mInstance==null)
        {
            mInstance = new MySingleton(context);
        }
        return mInstance;
    }

    public <T> void addToRequestQueue(Request<T> request) {
        request.setTag(this);
        getRequestQueue().add(request);
    }

    public void cancelAll() {
        requestQueue.cancelAll(this);
    }
}
