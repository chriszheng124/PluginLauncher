package zzh.com.pluginlauncher;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class HostFragment extends Fragment{
    private LinearLayout mRoot;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRoot = (LinearLayout)inflater.inflate(R.layout.layout_host_content, null);
        Button btn1 = (Button)mRoot.findViewById(R.id.start_local_service);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity activity = (MainActivity)getActivity();
                if(!activity.isCurrentPluginLoaded(MainActivity.TAB_C_ID)){
                    Toast.makeText(getActivity(), "Please install plugin firstly !", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent();
                try {
                    intent.setClassName(getActivity(), "tools.haha.com.androidtools.MyLocalService");
                    getActivity().startService(intent);
                }catch (Exception e){
                    throw new RuntimeException(e);
                }
            }
        });

        Button btn2 = (Button)mRoot.findViewById(R.id.bind_local_service);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        return mRoot;
    }

}
