package zzh.com.pluginframework;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;

public class PluginManager {
    public static String PLUGIN_A_PATH = "plugin_a.apk";
    public static String PLUGIN_B_PATH = "plugin_b.apk";
    public static String PLUGIN_C_PATH = "plugin_c.apk";

    private static final String PREF_TAG = "installed_plugins";

    private SharedPreferences mPref;
    private Activity mActivity;
    private HashMap<String, Plugin> mPlugins;

    static class PluginManagerHolder{
        static PluginManager instance = new PluginManager();
    }

    public static PluginManager getInstance(){
        return PluginManagerHolder.instance;
    }

    public void setContext(Activity context){
        mActivity = context;
        mPlugins = new HashMap<>();
        mPref = context.getSharedPreferences(PREF_TAG, Context.MODE_PRIVATE);
        FrameworkContext.sApp = mActivity.getApplication();
        injectInstrumentation();
    }

    public void loadPlugin(final String name){
        if(!isPluginLoaded(name)) {
            final Plugin plugin = new Plugin(mActivity, name);
            plugin.setState(Plugin.STATE_LOADING);
            new AsyncTask<String, Void, Void>(){
                @Override
                protected Void doInBackground(String... params) {
                    plugin.load();
                    mPlugins.put(plugin.getPluginPath(), plugin);
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    mPref.edit().putBoolean(name, true).apply();
                    plugin.setState(Plugin.STATE_LOADED);
                    BusProvider.getBus().post(new PluginEvent());
                }
            }.execute(name);
        }else {
            Plugin plugin = getPluginByName(name);
            plugin.setState(Plugin.STATE_LOADED);
            BusProvider.getBus().post(new PluginEvent());
        }
    }

    public boolean isPluginInstalled(String path){
        return mPref.getBoolean(path, false);
    }

    public Plugin findPluginByClassName(String name){
        for (Plugin plugin : mPlugins.values()){
            if(plugin.findComponent(name)){
                return plugin;
            }
        }
        return null;
    }

    public Collection<Plugin> getPlugins(){
        return mPlugins.values();
    }

    public boolean isPluginLoaded(String name){
        return mPlugins.containsKey(name);
    }

    public Plugin getPluginByName(String name){
        return mPlugins.get(name);
    }

    private void injectInstrumentation(){
        try{
            Field fieldInstrumentation = Activity.class.getDeclaredField("mInstrumentation");
            fieldInstrumentation.setAccessible(true);
            Instrumentation instrumentation = (Instrumentation)fieldInstrumentation.get(mActivity);
            InstrumentationHook instrumentationHook = new InstrumentationHook(instrumentation);
            fieldInstrumentation.set(mActivity, instrumentationHook);
            Field activityThread = Activity.class.getDeclaredField("mMainThread");
            activityThread.setAccessible(true);
            Object object = activityThread.get(mActivity);
            Field activityThreadInstrumentation = object.getClass().getDeclaredField("mInstrumentation");
            activityThreadInstrumentation.setAccessible(true);
            activityThreadInstrumentation.set(object, instrumentationHook);
        }catch (Exception e){
            if(PluginCfg.DEBUG){
                Log.v(PluginCfg.TAG, "injectInstrumentation error : " + e.getMessage());
            }
        }
    }

}
