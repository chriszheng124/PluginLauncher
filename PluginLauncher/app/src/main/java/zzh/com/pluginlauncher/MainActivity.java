package zzh.com.pluginlauncher;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import zzh.com.pluginframework.PluginManager;

public class MainActivity extends Activity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PluginManager.getInstance().setContext(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.plugin_a:
                PluginManager.getInstance().loadPlugin(PluginManager.PLUGIN_A_PATH);
                Intent intent_a = new Intent();
                intent_a.setClassName(MainActivity.this, "tools.haha.com.plugin_1.PluginMainActivity");
                startActivity(intent_a);
                break;
            case R.id.plugin_b:
                PluginManager.getInstance().loadPlugin(PluginManager.PLUGIN_B_PATH);
                Intent intent_b = new Intent();
                intent_b.setClassName(MainActivity.this, "com.yalantis.euclid.sample.MainActivity");
                startActivity(intent_b);
                break;
            case R.id.plugin_c:
                PluginManager.getInstance().loadPlugin(PluginManager.PLUGIN_C_PATH);
                Intent intent_c = new Intent();
                intent_c.setClassName(MainActivity.this, "tools.haha.com.androidtools.MainActivity");
                startActivity(intent_c);
                break;
            case R.id.host_a:
                Intent intent = new Intent();
                intent.setClassName(MainActivity.this, Activity_A.class.getName());
                startActivity(intent);
                break;
        }
    }
}
