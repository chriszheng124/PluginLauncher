package zzh.com.pluginlauncher;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import zzh.com.pluginframework.FrameworkContext;
import zzh.com.pluginframework.PluginCfg;
import zzh.com.pluginframework.PluginManager;

public class PluginLauncherApp extends Application{
    private FrameworkContext mFramework;

    public PluginLauncherApp() {
        super();
        mFramework = new FrameworkContext();
        //Debug.waitForDebugger();
        if(PluginCfg.DEBUG){
            Log.v(PluginCfg.TAG, "PluginLauncherApp.init was called");
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        mFramework.init(this);
        PluginManager.getInstance().init(this);
        if(inChildProcess()) {
            /**
             * TODO: move below code to ActivityThreadHandlerHook
             */
            PluginManager.getInstance().loadPlugin(PluginManager.PLUGIN_A_PATH);
            PluginManager.getInstance().loadPlugin(PluginManager.PLUGIN_B_PATH);
            PluginManager.getInstance().loadPlugin(PluginManager.PLUGIN_C_PATH);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if(PluginCfg.DEBUG){
            Log.v(PluginCfg.TAG, "PluginLauncherApp:onCreate  was called ");
        }
    }

    public static String getProcessName() {
        File f = new File("/proc/self/cmdline");
        InputStream reader = null;
        String name = "";
        try {
            reader = new FileInputStream(f);
            byte[] buffer = new byte[256];
            int length = reader.read(buffer);
            if (length > 0) {
                name = new String(buffer, 0, length).trim();
            }
        } catch (Exception e) {
            name = "";
        } finally {
            if (reader != null)
                try {
                    reader.close();
                } catch (IOException e) {
                    name = "";
                }
        }
        return name;
    }

    private boolean inChildProcess(){
        return !(getPackageName().equalsIgnoreCase(getProcessName()));
    }
}
