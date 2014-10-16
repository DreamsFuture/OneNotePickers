package onenotepicker.demo.com.onenotepickerdemo;

import android.app.Activity;

import java.lang.ref.WeakReference;

/**
* Created by Michael.Hancock on 10/16/2014.
*/
public abstract class UICallback<T> implements Callback<T> {
    private final WeakReference<Activity> activity;

    public UICallback(Activity a) {
        activity = new WeakReference<Activity>(a);
    }

    public final void success(final T result) {
        Activity act = activity.get();
        if (act != null) {
            act.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onSuccess(result);
                }
            });
        }
    }

    public abstract void onSuccess(T result);

    public final void failure(final Throwable result) {
        Activity act = activity.get();
        if (act != null) {
            act.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onFailure(result);
                }
            });
        }
    }

    public abstract void onFailure(Throwable result);
}
