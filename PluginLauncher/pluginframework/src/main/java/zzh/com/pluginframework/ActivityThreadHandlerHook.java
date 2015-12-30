package zzh.com.pluginframework;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class ActivityThreadHandlerHook implements Handler.Callback{

    @Override
    public boolean handleMessage(Message msg) {
        if(PluginCfg.DEBUG){
            Log.v(PluginCfg.TAG, "hook msg : " + msg.what);
        }
        switch (msg.what){
            case 114:
                break;
        }
        return false;
    }
}
