package zzh.com.pluginframework;

import android.util.Log;

import java.util.Collection;


public class ClassLoaderHook extends ClassLoader{

    public ClassLoaderHook(ClassLoader parent) {
        super(parent);
    }

    @Override
    public Class<?> loadClass(String className) throws ClassNotFoundException {
        if(PluginCfg.DEBUG){
            Log.v(PluginCfg.TAG, "class " + className + " was loaded");
        }
        return super.loadClass(className);
    }

    @Override
    protected Class<?> findClass(String className) throws ClassNotFoundException {
        Collection<Plugin> plugins = PluginManager.getInstance().getPlugins();
        for (Plugin plugin : plugins){
            try {
                return plugin.getClassLoader().findClass(className);
            }catch (ClassNotFoundException e){
            }
        }
        Log.v(PluginCfg.TAG, "cannot find class " + className);
        throw new ClassNotFoundException("Class " + className + " not found");
    }
}
