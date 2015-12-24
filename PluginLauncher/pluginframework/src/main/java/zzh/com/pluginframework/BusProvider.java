package zzh.com.pluginframework;

import com.squareup.otto.Bus;

public class BusProvider {
    private static final Bus bus = new Bus();

    public static Bus getBus(){
        return bus;
    }

}
