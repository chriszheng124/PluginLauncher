package zzh.com.pluginframework;

import android.app.Application;
import android.app.Instrumentation;
import android.content.pm.ApplicationInfo;
import android.content.res.Resources;
import android.os.Handler;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class FrameworkContext {
    public static Application sApp;
    public static Object sLoadedApk;
    public static ClassLoader sClassLoader;

    public void init(Application app){
        sClassLoader = app.getClassLoader();
        sApp = app;
        hookActivityThreadH();
        hookInstrumentation();
        createLoadedApk();
        hookClassLoader();
    }

    private void hookInstrumentation(){
        try{
            Object activityThread = getActivityThread();
            Class<?> activityThreadClazz = Class.forName("android.app.ActivityThread");
            Field fieldInstrumentation = activityThreadClazz.getDeclaredField("mInstrumentation");
            fieldInstrumentation.setAccessible(true);
            Instrumentation instrumentation = (Instrumentation)fieldInstrumentation.get(activityThread);
            InstrumentationHook instrumentationHook = new InstrumentationHook(instrumentation);
            fieldInstrumentation.set(activityThread, instrumentationHook);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    private void hookActivityThreadH(){
        try {
            Object activityThread = getActivityThread();
            Field mHField = activityThread.getClass().getDeclaredField("mH");
            mHField.setAccessible(true);
            Handler handler = (Handler)mHField.get(activityThread);
            Handler.Callback callback = new ActivityThreadHandlerHook();
            Field callbackField = Handler.class.getDeclaredField("mCallback");
            callbackField.setAccessible(true);
            callbackField.set(handler, callback);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    private void createLoadedApk(){
        try {
            Method getCompatibilityInfoMethod = Resources.class.getDeclaredMethod("getCompatibilityInfo");
            getCompatibilityInfoMethod.setAccessible(true);
            Object compatibilityInfo = getCompatibilityInfoMethod.invoke(sApp.getResources());

            Object activityThreadInstance = getActivityThread();
            Class cls = Class.forName("android.content.res.CompatibilityInfo");
            Method getPackageInfoNoCheckMethod = Class.forName("android.app.ActivityThread").
                    getDeclaredMethod("getPackageInfoNoCheck", ApplicationInfo.class, cls);
            getPackageInfoNoCheckMethod.setAccessible(true);
            sLoadedApk = getPackageInfoNoCheckMethod.invoke(activityThreadInstance,
                    sApp.getApplicationInfo(), compatibilityInfo);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void hookClassLoader(){
        try {
            Field mClassLoaderField = sLoadedApk.getClass().getDeclaredField("mClassLoader");
            mClassLoaderField.setAccessible(true);
            ClassLoaderHook classLoaderHook = new ClassLoaderHook(sApp);
            mClassLoaderField.set(sLoadedApk, classLoaderHook);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    private static Object getActivityThread(){
        try {
            Class<?> activityThreadClazz = Class.forName("android.app.ActivityThread");
            Method method = activityThreadClazz.getDeclaredMethod("currentActivityThread");
            return method.invoke(activityThreadClazz);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
