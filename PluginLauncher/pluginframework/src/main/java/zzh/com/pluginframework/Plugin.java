package zzh.com.pluginframework;

import android.app.Application;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class Plugin {
    public static final int STATE_LOADING = 0;
    public static final int STATE_LOADED = 1;
    public static final String PLUGIN_DIR = "plugindir";

    private String mPluginPath;
    private PluginClassLoader mClassLoader;
    private ResourcesHook mResource;
    private int mState;
    private HashSet<String> mComponents = new HashSet<>();

    public Plugin(String dexPath){
        mPluginPath = dexPath;
    }

    public void load(){
        Log.v(PluginCfg.TAG, ">>>>>>begin loading");
        long startTime = System.currentTimeMillis();

        installIfNeeded();
        mClassLoader = PluginClassLoader.getLoader(FrameworkContext.sApp, mPluginPath,
                Object.class.getClassLoader());

        Log.v(PluginCfg.TAG, "end loading <<<<<< " + (System.currentTimeMillis() - startTime) + "MS");
        try{
            makeComponentInfo();
            injectResources();
            newApplication();
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public String getPluginPath(){
        return mPluginPath;
    }

    public PluginClassLoader getClassLoader(){
        return mClassLoader;
    }

    public ResourcesHook getResources(){
        return mResource;
    }

    public int getState(){
        return mState;
    }

    public void setState(int state){
        mState = state;
    }

    public boolean findComponent(String className){
        return mComponents.contains(className);
    }

    private void makeComponentInfo(){
        try {
            PackageInfo packageInfo = getPackageInfo(FrameworkContext.sApp, mPluginPath);
            for (ActivityInfo info : packageInfo.activities){
                mComponents.add(info.name);
            }
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public static PackageInfo getPackageInfo(Context cxt, String apkPath)
            throws PackageManager.NameNotFoundException {
        PackageManager packageManager = cxt.getPackageManager();
        return packageManager.getPackageArchiveInfo(apkPath,
                PackageManager.GET_ACTIVITIES | PackageManager.GET_SERVICES | PackageManager.GET_RECEIVERS);
    }

    private void newApplication(){
        try {
            PackageInfo packageInfo = getPackageInfo(FrameworkContext.sApp, mPluginPath);
            ApplicationInfo applicationInfo = packageInfo.applicationInfo;
            if(applicationInfo.className == null){
                applicationInfo.className = "android.app.Application";
            }
            Class<?> appClazz = mClassLoader.loadClass(applicationInfo.className);
            Object app = appClazz.newInstance();
            Method attachMethod = Application.class.getDeclaredMethod("attach", Context.class);
            attachMethod.setAccessible(true);
            attachMethod.invoke(app, FrameworkContext.sApp);
            ((Application)app).onCreate();
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    private void injectResources(){
        try {
            Method method = AssetManager.class.getDeclaredMethod("addAssetPath", String.class);
            AssetManager assetManager = AssetManager.class.newInstance();
            Set<String> pathSet = generateNewAssetPaths(FrameworkContext.sApp, getPluginPath());
            for (String path : pathSet){
                method.invoke(assetManager, path);
            }
            mResource = new ResourcesHook(assetManager,
                    FrameworkContext.sApp.getResources().getDisplayMetrics(),
                    FrameworkContext.sApp.getResources().getConfiguration());

            Field loadedApkResourceField = FrameworkContext.sLoadedApk.getClass().getDeclaredField("mResources");
            loadedApkResourceField.setAccessible(true);
            loadedApkResourceField.set(FrameworkContext.sLoadedApk, mResource);

            Class<?> clazz = Class.forName("android.app.ContextImpl");
            Field contextImplResourceField = clazz.getDeclaredField("mResources");
            contextImplResourceField.setAccessible(true);
            contextImplResourceField.set(FrameworkContext.sApp.getBaseContext(), mResource);

            Field themeField = clazz.getDeclaredField("mTheme");
            themeField.setAccessible(true);
            themeField.set(FrameworkContext.sApp.getBaseContext(), null);

        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    private static Set<String> generateNewAssetPaths(Application application, String newPath) {
        Set<String> mGenerateNewSet = new LinkedHashSet<>();
        try {
            if (Build.VERSION.SDK_INT > 0) {
                List<String> mOriginAssetsPath = getOriginAssetsPath(application.getResources().getAssets());
                mGenerateNewSet.addAll(mOriginAssetsPath);
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
        if (newPath != null) {
            mGenerateNewSet.add(newPath);
            mGenerateNewSet.add(application.getApplicationInfo().sourceDir);
        }
        return mGenerateNewSet;
    }

    public static List<String> getOriginAssetsPath(AssetManager assetManager) {
        List<String> arrayList = new ArrayList<>();
        try {
            Method declaredMethod = assetManager.getClass().getDeclaredMethod("getStringBlockCount");
            declaredMethod.setAccessible(true);
            int intValue = (int)declaredMethod.invoke(assetManager);
            for (int i = 0; i < intValue; i++) {
                String cookieName = (String) assetManager.getClass().getMethod("getCookieName",
                        new Class[]{Integer.TYPE}).invoke(assetManager, i + 1);
                if (!TextUtils.isEmpty(cookieName)) {
                    arrayList.add(cookieName);
                }
            }
            return arrayList;
        } catch (Exception th) {
            th.printStackTrace();

            arrayList.clear();
            return arrayList;
        }
    }

    private void installIfNeeded(){
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            File dir = FrameworkContext.sApp.getDir(PLUGIN_DIR, Context.MODE_PRIVATE);
            String newFilePath = dir.getAbsolutePath() + File.separator+mPluginPath;
            if(new File(newFilePath).exists()){
                mPluginPath = newFilePath;
                return;
            }
            outputStream = new FileOutputStream(newFilePath);
            inputStream = FrameworkContext.sApp.getAssets().open(mPluginPath);
            byte[] buf = new byte[1024];
            while (-1 != inputStream.read(buf)){
                outputStream.write(buf);
            }
            mPluginPath = newFilePath;
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("copy file " + mPluginPath + " failed!");
        }finally {
            try {
                if(inputStream != null){
                    inputStream.close();
                }
                if(outputStream != null){
                    outputStream.close();
                }
            }catch (Exception e){
                Log.v(PluginCfg.TAG, "close file " + mPluginPath + " error !");
            }
        }
    }
}
