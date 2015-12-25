package zzh.com.pluginframework;

import android.content.Context;

import java.util.HashMap;
import java.util.HashSet;

import dalvik.system.DexClassLoader;

public class PluginClassLoader extends DexClassLoader {
    private final static String OPT_DIR = "plugin_opt_dex_dir";
    private final static String LIB_DIR = "plugin_lib_dir";

    private static HashMap<String, PluginClassLoader> sLoaders = new HashMap<>();
    private static HashSet<String> sClassNameOfLoadedClass = new HashSet<>();

    public PluginClassLoader(String dexPath, String optimizedDirectory, String libraryPath, ClassLoader parent) {
        super(dexPath, optimizedDirectory, libraryPath, parent);
    }

    public static PluginClassLoader getLoader(Context context, String dexPath, ClassLoader parentLoader){
        PluginClassLoader loader = sLoaders.get(dexPath);
        if(loader == null){
            String optimizedDirectory = context.getDir(OPT_DIR, Context.MODE_PRIVATE).getAbsolutePath();
            String libraryPath = context.getDir(LIB_DIR, Context.MODE_PRIVATE).getAbsolutePath();
            loader = new PluginClassLoader(dexPath, optimizedDirectory, libraryPath, parentLoader);
            sLoaders.put(dexPath, loader);
        }
        return loader;
    }

    public static boolean hasLoaded(String name){
        return sClassNameOfLoadedClass.contains(name);
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        Class<?> clazz;
        try {
            clazz = super.findClass(name);
            sClassNameOfLoadedClass.add(name);
        }catch (ClassNotFoundException e){
            throw new ClassNotFoundException(e.getMessage());
        }
        return clazz;
    }
}
