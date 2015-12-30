package zzh.com.pluginlauncher;

import android.app.Application;
import android.content.Context;
import android.os.Debug;
import android.util.Log;

import zzh.com.pluginframework.PluginCfg;
import zzh.com.pluginframework.PluginManager;

public class App extends Application{

    public App() {
        super();
        //Debug.waitForDebugger();
        if(PluginCfg.DEBUG){
            Log.v(PluginCfg.TAG, "App:init was called");
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        PluginManager.getInstance().init(this);
        PluginManager.getInstance().loadPlugin(PluginManager.PLUGIN_A_PATH);
        PluginManager.getInstance().loadPlugin(PluginManager.PLUGIN_B_PATH);
        PluginManager.getInstance().loadPlugin(PluginManager.PLUGIN_C_PATH);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if(PluginCfg.DEBUG){
            Log.v(PluginCfg.TAG, "App:onCreate  was called");
        }
    }
}
