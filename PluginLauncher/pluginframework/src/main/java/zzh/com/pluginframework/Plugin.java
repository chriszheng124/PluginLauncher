package zzh.com.pluginframework;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class Plugin {
    private Activity mActivity;
    private String mPluginPath;
    private PluginClassLoader mClassLoader;
    private Object mLoadedApk;
    private ResourcesHook mResource;
    private HashSet<String> mComponents = new HashSet<>();

    public Plugin(Activity activity, String dexPath){
        mActivity = activity;
        Log.v(PluginCfg.TAG, ">>>>>>begin loading");
        long startTime = System.currentTimeMillis();
        //TODO optdex cannot on UI thread
        mClassLoader = PluginClassLoader.getLoader(activity, dexPath, Object.class.getClassLoader());
        long endTime = System.currentTimeMillis();
        long delta = endTime-startTime;
        Log.v(PluginCfg.TAG, "end loading <<<<<< " + delta);
        mPluginPath = dexPath;
    }

    public void load(){
        try{
            makeComponentInfo();
            createLoadedApk();
            injectClassLoader();
            injectResources();
            newApplication();
        }catch (Exception e){
            if(PluginCfg.DEBUG){
                Log.v(PluginCfg.TAG, "loadPlugin failed " + e.getMessage());
            }
        }
    }

    public String getPluginPath(){
        return mPluginPath;
    }

    public Object getLoadedApk(){
        return mLoadedApk;
    }

    public PluginClassLoader getClassLoader(){
        return mClassLoader;
    }

    public ResourcesHook getResources(){
        return mResource;
    }

    public boolean findComponent(String className){
        return mComponents.contains(className);
    }

    private void makeComponentInfo(){
        try {
            PackageInfo packageInfo = getPackageInfo(mActivity, mPluginPath);
            for (ActivityInfo info : packageInfo.activities){
                mComponents.add(info.name);
            }
        }catch (Exception e){
            if(PluginCfg.DEBUG){
                e.printStackTrace();
            }
        }
    }

    public static PackageInfo getPackageInfo(Context cxt, String apkPath)
            throws PackageManager.NameNotFoundException {
        return cxt.getPackageManager().getPackageArchiveInfo(apkPath,
                PackageManager.GET_ACTIVITIES | PackageManager.GET_SERVICES | PackageManager.GET_RECEIVERS);
    }

    private void createLoadedApk(){
        try {
            Method getCompatibilityInfoMethod = Resources.class.getDeclaredMethod("getCompatibilityInfo");
            getCompatibilityInfoMethod.setAccessible(true);
            Object compatibilityInfo = getCompatibilityInfoMethod.invoke(mActivity.getResources());

            Field activityThread = Activity.class.getDeclaredField("mMainThread");
            activityThread.setAccessible(true);
            Object activityThreadInstance = activityThread.get(mActivity);
            Class cls = Class.forName("android.content.res.CompatibilityInfo");
            Method getPackageInfoNoCheckMethod = Class.forName("android.app.ActivityThread").
                    getDeclaredMethod("getPackageInfoNoCheck", ApplicationInfo.class, cls);
            getPackageInfoNoCheckMethod.setAccessible(true);
            mLoadedApk = getPackageInfoNoCheckMethod.invoke(activityThreadInstance,
                    mActivity.getApplicationInfo(), compatibilityInfo);
        } catch (Exception e) {
            if(PluginCfg.DEBUG){
                e.printStackTrace();
            }
        }
    }

    private void injectClassLoader(){
        try {
            Field mClassLoaderField = mLoadedApk.getClass().getDeclaredField("mClassLoader");
            mClassLoaderField.setAccessible(true);
            ClassLoaderHook classLoaderHook = new ClassLoaderHook(mActivity);
            mClassLoaderField.set(mLoadedApk, classLoaderHook);
        }catch (Exception e){
            if(PluginCfg.DEBUG){
                e.printStackTrace();
            }
        }
    }

    private void newApplication(){
        try {
            String applicationName = getPackageInfo(mActivity, mPluginPath).applicationInfo.className;
            Class<?> appClazz = mClassLoader.loadClass(applicationName);
            Object app = appClazz.newInstance();
            Method attachMethod = Application.class.getDeclaredMethod("attach", Context.class);
            attachMethod.setAccessible(true);
            attachMethod.invoke(app, mActivity.getApplication());
            ((Application)app).onCreate();
        }catch (Exception e){
            if(PluginCfg.DEBUG){
                e.printStackTrace();
            }
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

            Field loadedApkResourceField = getLoadedApk().getClass().getDeclaredField("mResources");
            loadedApkResourceField.setAccessible(true);
            loadedApkResourceField.set(getLoadedApk(), mResource);

            Class<?> clazz = Class.forName("android.app.ContextImpl");
            Field contextImplResourceField = clazz.getDeclaredField("mResources");
            contextImplResourceField.setAccessible(true);
            contextImplResourceField.set(FrameworkContext.sApp.getBaseContext(), mResource);

            Field themeField = clazz.getDeclaredField("mTheme");
            themeField.setAccessible(true);
            themeField.set(FrameworkContext.sApp.getBaseContext(), null);

        }catch (Exception e){
            if(PluginCfg.DEBUG){
                Log.v(PluginCfg.TAG, "hackContextThemeWrapper error : " + e.getMessage());
            }
        }
    }

    private static Set<String> generateNewAssetPaths(Application application, String newPath) {
        Set<String> mGenerateNewSet = new LinkedHashSet<>();
        try {
            if (Build.VERSION.SDK_INT > 20) {
                List<String> mOriginAssetsPath = getOriginAssetsPath(application.getResources().getAssets());
                mGenerateNewSet.addAll(mOriginAssetsPath);
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
        if (newPath != null) {
            mGenerateNewSet.add(newPath);
            mGenerateNewSet.add(application.getApplicationInfo().sourceDir);//TODO???
        }
        return mGenerateNewSet;
    }

    public static List<String> getOriginAssetsPath(AssetManager assetManager) {
        List<String> arrayList = new ArrayList<>();
        try {
            Method declaredMethod = assetManager.getClass().getDeclaredMethod("getStringBlockCount");
            declaredMethod.setAccessible(true);
            int intValue = ((Integer) declaredMethod.invoke(assetManager)).intValue();
            for (int i = 0; i < intValue; i++) {
                String cookieName = (String) assetManager.getClass().getMethod("getCookieName",
                        new Class[]{Integer.TYPE}).invoke(assetManager, Integer.valueOf(i + 1));
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
}
