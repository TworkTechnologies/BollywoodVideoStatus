package twork.bollywood.videostatus.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

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

public class VideoListActivity extends AppCompatActivity implements ItemClickListener {
    String TAG = getClass().getSimpleName();
    Globals globals;

    @BindView(R.id.rcv_video_cate)
    RecyclerView rcv_video_cate;

    @BindView(R.id.tv_load_more)
    TextView tv_load_more;

    @BindView(R.id.mAdView)
    AdView mAdView;

    List<MainSubCateDataObject> dataList = new ArrayList<>();

    CategoryVideoListAdapter adapter;

    String cate_id = "1", cate_name="Bollywood";
    int cate_page = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_video_list);
        ButterKnife.bind(this);
        globals = ((Globals) getApplicationContext());

        if (getIntent().getExtras() != null) {
            try {
                cate_id = getIntent().getStringExtra(Constant.CATEGORY_ID);
                cate_name = getIntent().getStringExtra(Constant.CATEGORY_NAME);
            } catch (Exception e) {

            }
        }


        call_category_service(false);
        admob_init();
        full_screen_ads();

        tv_load_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cate_page = cate_page + 1;
                call_category_service(true);
            }
        });

        if (AppUtils.isInternetConnected(getContext())) {
            mAdView.setVisibility(View.VISIBLE);
        }else {
            mAdView.setVisibility(View.GONE);
        }
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
            AppUtils.showAlertDialog(getContext(), "Opps", "Something wrong...");
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
            } else {
                if (dataList.size() > 0) {
                    tv_load_more.setVisibility(View.GONE);
                    globals.toastValidateMessage(getActivity(), "No More Data....");
                } else {
                    AppUtils.showAlertDialog(getContext(), "Opps", "Something wrong...");
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void set_adapter_data() {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        rcv_video_cate.setLayoutManager(layoutManager);

        adapter = new CategoryVideoListAdapter(getContext(), dataList);
        rcv_video_cate.setAdapter(adapter);

        rcv_video_cate.setNestedScrollingEnabled(false);

        adapter.setClickListener(this);
    }


    @Override
    public void onClick(View view, String val_var, int position) {
        Intent mIntent = new Intent(getContext(), VideoActivity.class);
        mIntent.putExtra(Constant.VIDEO_URL, dataList.get(position).getVideourl());
        mIntent.putExtra(Constant.CATEGORY_NAME, cate_name);
        mIntent.putExtra(Constant.PAGE, cate_page+"");
        startActivity(mIntent);
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
        }
        super.onDestroy();
    }

    private Context getContext() {
        return this;
    }

    private Activity getActivity() {
        return this;
    }

}
