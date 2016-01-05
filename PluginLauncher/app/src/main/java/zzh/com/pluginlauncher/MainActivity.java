package zzh.com.pluginlauncher;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.widget.TabHost;

import com.squareup.otto.Subscribe;

import zzh.com.pluginframework.BusProvider;
import zzh.com.pluginframework.PluginEvent;
import zzh.com.pluginframework.PluginManager;

public class MainActivity extends FragmentActivity
        implements TabHost.OnTabChangeListener{
    public final static String TAB_HOST_ID = "host";
    public final static String TAB_A_ID = "plugin_a";
    public final static String TAB_B_ID = "plugin_b";
    public final static String TAB_C_ID = "plugin_c";

    private final static String TAB_HOST_INDICATOR = "HOST";
    private final static String TAB_A_INDICATOR = "Plugin_A";
    private final static String TAB_B_INDICATOR = "Plugin_B";
    private final static String TAB_C_INDICATOR = "Plugin_C";

    private FragmentTabHost mTabHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTabHost = (FragmentTabHost)findViewById(R.id.tab_host);
        mTabHost.setup(this, getSupportFragmentManager(), R.id.tab_content);

        mTabHost.addTab(mTabHost.newTabSpec(TAB_HOST_ID).setIndicator(TAB_HOST_INDICATOR),
                HostFragment.class, null);
        mTabHost.addTab(mTabHost.newTabSpec(TAB_A_ID).setIndicator(TAB_A_INDICATOR),
                PluginFragment.class, null);
        mTabHost.addTab(mTabHost.newTabSpec(TAB_B_ID).setIndicator(TAB_B_INDICATOR),
                PluginFragment.class, null);
        mTabHost.addTab(mTabHost.newTabSpec(TAB_C_ID).setIndicator(TAB_C_INDICATOR),
                PluginFragment.class, null);

        mTabHost.setOnTabChangedListener(this);
        BusProvider.getBus().register(this);
    }

    public boolean isCurrentPluginLoaded(){
        String tabId = mTabHost.getCurrentTabTag();
        return isCurrentPluginLoaded(tabId);
    }

    public boolean isCurrentPluginLoaded(String tabId){
        if(tabId.equalsIgnoreCase(TAB_A_ID)){
            return PluginManager.getInstance().isPluginInstalled(PluginManager.PLUGIN_A_PATH);
        }else if (tabId.equalsIgnoreCase(TAB_B_ID)){
            return PluginManager.getInstance().isPluginInstalled(PluginManager.PLUGIN_B_PATH);
        }else if(tabId.equalsIgnoreCase(TAB_C_ID)) {
            return PluginManager.getInstance().isPluginInstalled(PluginManager.PLUGIN_C_PATH);
        }
        return false;
    }

    @Override
    public void onTabChanged(String tabId) {
        if(tabId.equalsIgnoreCase(TAB_A_ID)){
            PluginManager.getInstance().loadPluginAsync(PluginManager.PLUGIN_A_PATH);
        }else if (tabId.equalsIgnoreCase(TAB_B_ID)){
            PluginManager.getInstance().loadPluginAsync(PluginManager.PLUGIN_B_PATH);
        }else if(tabId.equalsIgnoreCase(TAB_C_ID)) {
            PluginManager.getInstance().loadPluginAsync(PluginManager.PLUGIN_C_PATH);
        }else {
//            Intent intent = new Intent();
//            intent.setClassName(MainActivity.this, Activity_A.class.getName());
//            startActivity(intent);
        }
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void onReceivePluginEvent(PluginEvent event){
        String tabId = mTabHost.getCurrentTabTag();
        if(tabId.equalsIgnoreCase(TAB_A_ID)){
            Intent intent_a = new Intent();
            //intent_a.setClassName(MainActivity.this, "tools.haha.com.plugin_1.PluginMainActivity");
            intent_a.setClassName(MainActivity.this, "com.gc.materialdesigndemo.ui.MainActivity");
            startActivity(intent_a);
        }else if (tabId.equalsIgnoreCase(TAB_B_ID)){
            Intent intent_b = new Intent();
            intent_b.setClassName(MainActivity.this, "com.yalantis.euclid.sample.MainActivity");
            startActivity(intent_b);
        }else if(tabId.equalsIgnoreCase(TAB_C_ID)) {
            Intent intent_c = new Intent();
            intent_c.setClassName(MainActivity.this, "tools.haha.com.androidtools.MainActivity");
            startActivity(intent_c);
        }
    }
}
