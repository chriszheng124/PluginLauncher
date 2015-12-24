package zzh.com.pluginframework;

import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


public class InstrumentationHook extends Instrumentation {
    private Instrumentation mInstrumentation;

    public InstrumentationHook(Instrumentation instrumentation){
        mInstrumentation = instrumentation;
    }

    @SuppressWarnings("unused")
    public ActivityResult execStartActivity(
            Context who, IBinder contextThread, IBinder token, Activity target,
            Intent intent, int requestCode, Bundle options) {
        try {
            Method method = Instrumentation.class.getDeclaredMethod("execStartActivity",
                    Context.class, IBinder.class, IBinder.class,
                    Activity.class, Intent.class, int.class, Bundle.class);
            return (ActivityResult)method.invoke(mInstrumentation, who,
                    contextThread, token, target, intent, requestCode, options);
        }catch (Exception e){
            return null;
        }
    }

    public Activity newActivity(ClassLoader cl, String className, Intent intent)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        try{
            Method method = Instrumentation.class.getDeclaredMethod(
                    "newActivity", ClassLoader.class, String.class, Intent.class);
            Activity activity = (Activity)method.invoke(mInstrumentation, cl, className, intent);
            if(PluginClassLoader.hasLoaded(activity.getClass().getCanonicalName())) {
                injectResources(activity);
            }
            return activity;
        }catch (Exception e){
            if(PluginCfg.DEBUG){
                Log.v(PluginCfg.TAG, "newActivity error : " + e.getMessage());
            }
            return null;
        }
    }

    public void callActivityOnCreate(Activity activity, Bundle icicle) {
        try {
            mInstrumentation.callActivityOnCreate(activity, icicle);
        }catch (Exception e){
            if(PluginCfg.DEBUG){
                e.printStackTrace();
                Log.v(PluginCfg.TAG, "callActivityOnCreate error : " + e.getMessage());
            }
        }
    }

    public void callActivityOnCreate(Activity activity, Bundle icicle,
                                     PersistableBundle persistentState) {
        mInstrumentation.callActivityOnCreate(activity, icicle, persistentState);
    }

    @Override
    public void callActivityOnNewIntent(Activity activity, Intent intent) {
        mInstrumentation.callActivityOnNewIntent(activity, intent);
    }

    @Override
    public void callActivityOnResume(Activity activity) {
        mInstrumentation.callActivityOnResume(activity);
    }

    private void injectResources(Activity activity){
        try {
            Plugin plugin = PluginManager.getInstance().findPluginByClassName(activity.getClass().getCanonicalName());
            Field resourceField = ContextThemeWrapper.class.getDeclaredField("mResources");
            resourceField.setAccessible(true);
            resourceField.set(activity, plugin.getResources());
            Log.v(PluginCfg.TAG, "hackContextThemeWrapper inject resource for " + activity.toString());
        }catch (Exception e){
            if(PluginCfg.DEBUG){
                Log.v(PluginCfg.TAG, "hackContextThemeWrapper error : " + e.getMessage());
            }
        }
    }
}
