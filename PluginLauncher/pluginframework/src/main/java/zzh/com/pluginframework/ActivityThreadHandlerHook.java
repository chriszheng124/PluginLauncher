package zzh.com.pluginframework;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class ActivityThreadHandlerHook implements Handler.Callback{
    public static final int LAUNCH_ACTIVITY = 100;
    public static final int RECEIVER = 113;
    public static final int CREATE_SERVICE = 114;

    private Handler mHandler;

    public ActivityThreadHandlerHook(Handler handler){
        mHandler = handler;
    }

    @Override
    public boolean handleMessage(Message msg) {
        if(PluginCfg.DEBUG){
            //Log.v(PluginCfg.TAG, "hook msg : " + msg.what);
        }
        switch (msg.what){
            case LAUNCH_ACTIVITY:
                break;
            case CREATE_SERVICE:
                break;
            case RECEIVER:
                break;
        }
        //mHandler.handleMessage(msg);
        return false;
    }
}
