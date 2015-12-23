package zzh.com.pluginframework;

import android.app.Activity;
import android.app.Instrumentation;
import android.os.Environment;
import android.util.ArrayMap;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;

public class PluginManager {
//    public static final String PLUGIN_A_PATH = Environment.getExternalStorageDirectory().
//            getPath().concat("/app-debug.apk");
//    public static final String PLUGIN_B_PATH = Environment.getExternalStorageDirectory().
//            getPath().concat("/plugin_b.apk");
//    public static final String PLUGIN_C_PATH = Environment.getExternalStorageDirectory().
//            getPath().concat("/plugin_c.apk");

    public static final String PLUGIN_A_PATH = ("/sdcard/plugin_a.apk");
    public static final String PLUGIN_B_PATH = "/sdcard/plugin_b.apk";
    public static final String PLUGIN_C_PATH = "/sdcard/plugin_c.apk";

    private Activity mActivity;
    private ArrayMap<String, Plugin> mPlugins = new ArrayMap<>();

    static class PluginManagerHolder{
        static PluginManager instance = new PluginManager();
    }

    public static PluginManager getInstance(){
        return PluginManagerHolder.instance;
    }

    public void setContext(Activity context){
        mActivity = context;
        FrameworkContext.sApp = mActivity.getApplication();
        injectInstrumentation();
    }

    public void loadPlugin(String name){
        if(!mPlugins.containsKey(name)) {
            Plugin plugin = new Plugin(mActivity, name);
            mPlugins.put(name, plugin);
            plugin.load();
        }
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
