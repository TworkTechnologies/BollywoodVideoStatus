package twork.bollywood.videostatus.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import twork.bollywood.videostatus.Globals;
import twork.bollywood.videostatus.R;
import twork.bollywood.videostatus.adapter.CategoryVideoListAdapter;
import twork.bollywood.videostatus.interf.ItemClickListener;
import twork.bollywood.videostatus.object.subcate.MainSubCateDataObject;
import twork.bollywood.videostatus.object.subcate.MainSubCateObject;
import twork.bollywood.videostatus.utils.AppUtils;
import twork.bollywood.videostatus.utils.Constant;
import twork.bollywood.videostatus.utils.DownloadTask;
import uk.co.jakelee.vidsta.VidstaPlayer;
import uk.co.jakelee.vidsta.listeners.VideoStateListeners;

public class VideoActivity extends AppCompatActivity implements ItemClickListener {
    String TAG = getClass().getSimpleName();
    Globals globals;

    @BindView(R.id.vdo_view)
    VidstaPlayer vdo_view;

    @BindView(R.id.img_download)
    ImageView img_download;
    @BindView(R.id.img_share)
    ImageView img_share;

    @BindView(R.id.tv_title)
    TextView tv_title;

    @BindView(R.id.rcv_video_cate)
    RecyclerView rcv_video_cate;
    List<MainSubCateDataObject> dataList = new ArrayList<>();

    CategoryVideoListAdapter adapter;

    String cate_id = "1";
    String cate_page = "1";

    private int WRITE_EXTERNAL_STORAGE = 23;

    String video_url = "", cate_name = "";

    @BindView(R.id.mAdView)
    AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        ButterKnife.bind(this);
        globals = ((Globals) getApplicationContext());

        if (getIntent().getExtras() != null) {
            try {
                video_url = getIntent().getStringExtra(Constant.VIDEO_URL);
                cate_name = getIntent().getStringExtra(Constant.CATEGORY_NAME);
                cate_page = getIntent().getStringExtra(Constant.PAGE);
                tv_title.setText(cate_name);
            } catch (Exception e) {
            }
        }
        admob_init();
        full_screen_ads();
        loadVideo();
        click_listener();
        call_category_service(false);

        if (AppUtils.isInternetConnected(getContext())) {
            mAdView.setVisibility(View.VISIBLE);
        } else {
            mAdView.setVisibility(View.GONE);
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        vdo_view.stop();
        finish();
    }

    private void click_listener() {
        img_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isReadStorageAllowed()) {
                    new DownloadTask(getContext(), globals, video_url);
                } else {
                    //If the app has not the permission then asking for the permission
                    requestStoragePermission();
                }
            }
        });

        img_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Video Status");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, video_url + "\n" + "https://play.google.com/store/apps/details?id=" + getPackageName());
                startActivity(Intent.createChooser(sharingIntent, "Share Using.."));
            }
        });
    }

    private void call_category_service(final Boolean isLoad) {
        if (AppUtils.isInternetConnected(getContext())) {
            globals.dialogShow(getContext());
            String mStringUrl = "http://mirchikart.com/videostatus/api/api.php?req=statuslist&category_id=" + cate_id + "&page=" + cate_page;
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(mStringUrl)
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    //do failure stuff
                    globals.dialogDismiss();
                    AppUtils.showAlertDialog(getContext(), "Opps", "Something wrong...");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    //do success stuff
                    globals.dialogDismiss();
                    parseDetailResponse(response.body().string(), isLoad);
                }
            });
        } else {
            AppUtils.showAlertDialog(getContext(), "Opps", "No Internet Connection...");
        }
    }

    private void parseDetailResponse(String responce, boolean isLoad) {
        Log.e(TAG, responce);
        try {
            JsonElement json = new JsonParser().parse(responce);
            Type type = new TypeToken<MainSubCateObject>() {
            }.getType();
            MainSubCateObject mainCateObject = new Gson().fromJson(json, type);
            if (mainCateObject.getSuccess() == 1) {
                if (isLoad) {
                    dataList.addAll(mainCateObject.getData());
                    adapter.notifyDataSetChanged();
                } else {
                    dataList = mainCateObject.getData();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            set_adapter_data();
                        }
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void set_adapter_data() {
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        rcv_video_cate.setLayoutManager(layoutManager);

        adapter = new CategoryVideoListAdapter(getContext(), dataList);
        rcv_video_cate.setAdapter(adapter);

        rcv_video_cate.setNestedScrollingEnabled(false);

        adapter.setClickListener(this);
    }

    @Override
    public void onClick(View view, String val_var, int position) {
        video_url = dataList.get(position).getVideourl();
        vdo_view.restart();
        loadVideo();
    }

    //We are calling this method to check the permission status
    private boolean isReadStorageAllowed() {
        //Getting the permission status
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        //If permission is granted returning true
        if (result == PackageManager.PERMISSION_GRANTED)
            return true;

        //If permission is not granted returning false
        return false;
    }

    //Requesting permission
    private void requestStoragePermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            //If the user has denied the permission previously your code will come to this block
            //Here you can explain why you need this permission
            //Explain here why you need this permission
        }

        //And finally ask for the permission
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE);
    }

    //This method will be called when the user will tap on allow or deny
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        //Checking the request code of our request
        if (requestCode == WRITE_EXTERNAL_STORAGE) {
            //If permission is granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                new DownloadTask(getContext(), globals, video_url);
            } else {
                //Displaying another toast if permission is not granted
                Toast.makeText(this, "Oops you just denied the permission", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void loadVideo() {
        //globals.dialogShow(getContext());

        vdo_view.setVideoSource(video_url);
        vdo_view.setAutoLoop(true);
        vdo_view.setAutoPlay(true);
        vdo_view.controlsShowing();
        vdo_view.setFullScreenButtonVisible(false);

        // Setting up custom listeners for events
        vdo_view.setOnVideoBufferingListener(new VideoStateListeners.OnVideoBufferingListener() {
            @Override
            public void OnVideoBuffering(VidstaPlayer evp, int buffPercent) {
                //globals.dialogShow(getContext(), buffPercent);

            }
        });

        vdo_view.setOnVideoErrorListener(new VideoStateListeners.OnVideoErrorListener() {
            @Override
            public void OnVideoError(int what, int extra) {
                //globals.dialogDismiss();
            }
        });

        vdo_view.setOnVideoFinishedListener(new VideoStateListeners.OnVideoFinishedListener() {
            @Override
            public void OnVideoFinished(VidstaPlayer evp) {
                //globals.dialogDismiss();
            }
        });

        vdo_view.setOnVideoPausedListener(new VideoStateListeners.OnVideoPausedListener() {
            @Override
            public void OnVideoPaused(VidstaPlayer evp) {

            }
        });

        vdo_view.setOnVideoRestartListener(new VideoStateListeners.OnVideoRestartListener() {
            @Override
            public void OnVideoRestart(VidstaPlayer evp) {

            }
        });

        vdo_view.setOnVideoStartedListener(new VideoStateListeners.OnVideoStartedListener() {
            @Override
            public void OnVideoStarted(VidstaPlayer evp) {
                //globals.dialogDismiss();
            }
        });

        vdo_view.setOnVideoStoppedListener(new VideoStateListeners.OnVideoStoppedListener() {
            @Override
            public void OnVideoStopped(VidstaPlayer evp) {
                //globals.dialogDismiss();
            }
        });
    }

    private void admob_init() {

        AdRequest adRequest = new AdRequest.Builder()
                //.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                // Check the LogCat to get your test device ID
                //.addTestDevice("FEC16BB7FA178FC8EE9883D651E352B3")
                .build();

        mAdView.loadAd(adRequest);
    }

    //full screen ads
    private void full_screen_ads() {
        final InterstitialAd mInterstitialAd;

        mInterstitialAd = new InterstitialAd(getContext());

        // set the ad unit ID
        mInterstitialAd.setAdUnitId(getString(R.string.interstitial_full_screen));

        AdRequest adRequest = new AdRequest.Builder()
                .build();

        // Load ads into Interstitial Ads
        mInterstitialAd.loadAd(adRequest);

        mInterstitialAd.setAdListener(new AdListener() {
            public void onAdLoaded() {
                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                }
            }
        });
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
            vdo_view.stop();
        }
        super.onDestroy();
    }

    private Context getContext() {
        return this;
    }
}
