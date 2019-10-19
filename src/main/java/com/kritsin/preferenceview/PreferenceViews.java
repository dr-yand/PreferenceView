package com.kritsin.preferenceview;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.IdRes;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class PreferenceViews {

    private List<Integer> preferenceViewsId = new ArrayList<>();

    private List<String> preferenceViewsIdName = new ArrayList<>();

    private static List<WeakReference<TextView>> preferenceViews = new ArrayList<>();

    private WeakReference<Context> contextReference;

    private static PreferenceViews instance;

    public static void bind(Activity context) {

//        if (instance != null && instance.contextReference.get()!=null) {
//            return;
//        }

        instance = new PreferenceViews(context);
    }

    private PreferenceViews(Activity context) {

        contextReference = new WeakReference<>(context.getBaseContext());

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getBaseContext());

        if (instance != null) {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(instance.sharedPreferenceChangeListener);
        }
        sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);

        Class activityClass = context.getClass();

        List<Field> fields = new ArrayList<>(Arrays.asList(activityClass.getDeclaredFields()));
        Class superClass = activityClass.getClass().getSuperclass();
        while (superClass != null) {

            fields.addAll(Arrays.asList(superClass.getDeclaredFields()));

            superClass = superClass.getSuperclass();
        }

        for (Field field : fields) {
            if (field.isAnnotationPresent(PreferenceView.class)) {
                PreferenceView preferenceView = field.getAnnotation(PreferenceView.class);
                @IdRes int val = preferenceView.value();
                if (val == 0) {
                    int viewId = contextReference.get().getResources().getIdentifier(field.getName(), "id", contextReference.get().getPackageName());
                    if (viewId == 0) {
                        viewId = contextReference.get().getResources().getIdentifier(PreferenceUtils.toSnakeCase(field.getName()), "id", contextReference.get().getPackageName());
                    }
                    addView((TextView) context.findViewById(viewId), false);
                } else {
                    addView((TextView) context.findViewById(val), false);//PreferenceUtils.getResourceEntryName(context, val));
                }
            }
        }

        load();
    }

    private void load() {
        for (WeakReference<TextView> viewReference : preferenceViews) {
            TextView view = viewReference.get();
            String key = PreferenceUtils.getResourceEntryName(contextReference.get(), view.getId());
            String value = getValue(contextReference.get(), key);
            if (value != null) {
                view.setText(value);
            }
        }
        /*for (Integer viewId : preferenceViewsId) {
            String key = PreferenceUtils.getResourceEntryName(contextReference.get(), viewId);
            View view = contextReference.get().findViewById(viewId);
            if (view instanceof TextView) {
                String value = getValue(contextReference.get(), key);
                if (value != null) {
                    ((TextView) view).setText(value);
                }
            }
        }
        for (String viewIdName : preferenceViewsIdName) {
            String key = viewIdName;
            int viewId = contextReference.get().getResources().getIdentifier(viewIdName, "id", contextReference.get().getPackageName());
            View view = contextReference.get().findViewById(viewId);
            if (view instanceof TextView) {
                String value = getValue(contextReference.get(), key);
                if (value != null) {
                    ((TextView) view).setText(value);
                }
            }
        }*/

    }

    private SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Log.e(">>>>onSharedPrefe", "<onSharedPreferenceChanged");

            Log.e(">>>>contextReget()", contextReference.get() + "<");

            for (WeakReference<TextView> viewReference : preferenceViews) {
                TextView v = viewReference.get();
                if (v != null && key.equals(PreferenceUtils.getResourceEntryName(contextReference.get(), v.getId()))) {
                    v.setText(getValue(contextReference.get(), key));
                }
            }
            /*for (Integer viewId : preferenceViewsId) {
                if (key.equals(viewId)) {
                    View view = contextReference.get().findViewById(viewId);
                    if (view instanceof TextView) {
                        ((TextView) view).setText(getValue(contextReference.get(), key));
                    }
                }
            }
            for (String viewIdName : preferenceViewsIdName) {
                if (key.equals(viewIdName)) {
                    int viewId = contextReference.get().getResources().getIdentifier(viewIdName, "id", contextReference.get().getPackageName());
                    View view = contextReference.get().findViewById(viewId);
                    if (view instanceof TextView) {
                        ((TextView) view).setText(getValue(contextReference.get(), key));
                    }
                }
            }*/
        }
    };


    public static String getValue(Context context, String key, String defaultValue) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(key, defaultValue);
    }

    public static String getValue(Context context, String key) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(key, "");
    }

    public void addView(String viewIdName) {
        removeView(viewIdName);
        if (viewIdName != null) {
            preferenceViewsIdName.add(viewIdName);
        }
    }

    public void addView(Integer viewId) {
        removeView(viewId);
        if (viewId != null) {
            preferenceViewsId.add(viewId);
        }
    }

    public static void addView(TextView view) {
        addView(view, true);
    }

    public static void addView(TextView view, boolean needLoad) {
        removeView(view);
        if (view != null) {
            preferenceViews.add(new WeakReference<TextView>(view));

            if (needLoad && instance != null) {
                instance.load();
            }
        }
    }

    public void addView(TextView... views) {
        removeView(views);
        for (TextView view : views) {
            preferenceViews.add(new WeakReference<TextView>(view));
        }
    }

    public void removeView(Integer viewId) {
        Iterator<Integer> iterator = preferenceViewsId.iterator();
        while (iterator.hasNext()) {
            Integer value = iterator.next();
            if (value == viewId) {
                iterator.remove();
            }
        }
    }

    public void removeView(String viewIdName) {
        Iterator<String> iterator = preferenceViewsIdName.iterator();
        while (iterator.hasNext()) {
            String value = iterator.next();
            if (value == viewIdName) {
                iterator.remove();
            }
        }
    }

    public static void removeView(View view) {
        Iterator<WeakReference<TextView>> iterator = preferenceViews.iterator();
        while (iterator.hasNext()) {
            WeakReference<TextView> weakReference = iterator.next();
            View v = weakReference.get();
            if (v == null || v == view) {
                iterator.remove();
            }
        }
    }

    public void removeView(View... views) {
        for (View view : views) {
            removeView(view);
        }
    }
}
