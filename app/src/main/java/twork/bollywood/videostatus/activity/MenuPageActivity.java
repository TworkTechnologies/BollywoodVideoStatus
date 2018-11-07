package twork.bollywood.videostatus.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import butterknife.BindView;
import butterknife.ButterKnife;
import twork.bollywood.videostatus.Globals;
import twork.bollywood.videostatus.R;
import twork.bollywood.videostatus.utils.AppUtils;

/**
 * Created by Jiyan on 5/8/2018.
 */


public class MenuPageActivity extends AppCompatActivity {
    String TAG = getClass().getSimpleName();
    Globals globals;

    @BindView(R.id.mAdView)
    AdView mAdView;
    @BindView(R.id.img_start)
    ImageView img_start;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_page);
        ButterKnife.bind(this);
        globals = ((Globals) getApplicationContext());

        admob_init();

        img_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mIntent = new Intent(getContext(), VideoListActivity.class);
                startActivity(mIntent);
            }
        });

        if (AppUtils.isInternetConnected(getContext())) {
            mAdView.setVisibility(View.VISIBLE);
        }else {
            mAdView.setVisibility(View.GONE);
        }

    }

    private void admob_init() {

        AdRequest adRequest = new AdRequest.Builder()
                //.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                // Check the LogCat to get your test device ID
                //.addTestDevice("FEC16BB7FA178FC8EE9883D651E352B3")
                .build();

        mAdView.loadAd(adRequest);
    }

    @Override
    public void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
    }

    @Override
    public void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }

    private Context getContext() {
        return this;
    }
}
