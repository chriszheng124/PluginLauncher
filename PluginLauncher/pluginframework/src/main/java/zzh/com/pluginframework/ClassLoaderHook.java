package zzh.com.pluginframework;

import android.content.Context;
import android.util.Log;

import java.util.Collection;


public class ClassLoaderHook extends ClassLoader{

    public ClassLoaderHook(Context context) {
        super(context.getClassLoader());
    }

    @Override
    public Class<?> loadClass(String className) throws ClassNotFoundException {
        return super.loadClass(className);
    }

    @Override
    protected Class<?> findClass(String className) throws ClassNotFoundException {
        Collection<Plugin> plugins = PluginManager.getInstance().getPlugins();
        for (Plugin plugin : plugins){
            try {
                Class<?> clazz = plugin.getClassLoader().findClass(className);
                return clazz;
            }catch (ClassNotFoundException e){
            }
        }
        Log.v(PluginCfg.TAG, "cannot find class " + className);
        throw new ClassNotFoundException("Class " + className + " not found");
    }
}
