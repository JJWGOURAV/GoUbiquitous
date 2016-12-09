package com.example.android.sunshine.app;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import static com.example.android.sunshine.app.DataLayerListenerService.LOGD;

public class MainActivity extends WearableActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,DataApi.DataListener{

    private static final String TAG = MainActivity.class.getSimpleName();
    private GoogleApiClient mGoogleApiClient;
    private TextView highTempText,lowTempText,dateTime,dateTimeText;
    private ImageView icon;
    private LinearLayout parentView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpViews();
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                setUpViews(stub);
                refreshData();
            }
        });

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        setAmbientEnabled();

//        refreshData();
//        mGoogleApiClient.connect();
    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);

        dateTime.getPaint().setAntiAlias(false);
        dateTimeText.getPaint().setAntiAlias(false);
        highTempText.getPaint().setAntiAlias(false);
        lowTempText.getPaint().setAntiAlias(false);
        parentView.setBackgroundColor(getResources().getColor(R.color.black));
        icon.setImageBitmap(null);
        icon.setVisibility(View.GONE);
    }

    @Override
    public void onExitAmbient() {
        super.onExitAmbient();

        icon.setVisibility(View.VISIBLE);
        dateTime.getPaint().setAntiAlias(true);
        dateTimeText.getPaint().setAntiAlias(true);
        highTempText.getPaint().setAntiAlias(true);
        lowTempText.getPaint().setAntiAlias(true);
        parentView.setBackgroundColor(getResources().getColor(R.color.blue));
        refreshData();
    }

    @Override
    public void onUpdateAmbient() {
        super.onUpdateAmbient();

        // Update the content
        refreshData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        if ((mGoogleApiClient != null) && mGoogleApiClient.isConnected()) {
            Wearable.DataApi.removeListener(mGoogleApiClient, this);
//            Wearable.MessageApi.removeListener(mGoogleApiClient, this);
//            Wearable.CapabilityApi.removeListener(mGoogleApiClient, this);
//            mGoogleApiClient.disconnect();
        }

        super.onPause();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        LOGD(TAG, "onConnected(): Successfully connected to Google API client");
        Wearable.DataApi.addListener(mGoogleApiClient, this);

    }

    @Override
    public void onConnectionSuspended(int i) {
        LOGD(TAG, "onConnectionSuspended(): Connection to Google API client was suspended");

    }

    private void setUpViews(WatchViewStub stub){
        highTempText = (TextView) stub.findViewById(R.id.list_item_high_textview);
        lowTempText = (TextView) stub.findViewById(R.id.list_item_low_textview);
        icon = (ImageView) stub.findViewById(R.id.list_item_icon);
        dateTime = (TextView) stub.findViewById(R.id.dateTime);
        dateTimeText = (TextView) stub.findViewById(R.id.dateTimeText);
        parentView = (LinearLayout) stub.findViewById(R.id.parentView);
    }

    private void setUpViews(){
        highTempText = (TextView)findViewById(R.id.list_item_high_textview);
        lowTempText = (TextView)findViewById(R.id.list_item_low_textview);
        icon = (ImageView)findViewById(R.id.list_item_icon);
        dateTime = (TextView)findViewById(R.id.dateTime);
        dateTimeText = (TextView)findViewById(R.id.dateTimeText);
        parentView = (LinearLayout)findViewById(R.id.parentView);
    }

    private void refreshData(){
        Temperature temperature = Temperature.getInstance();
        highTempText.setText(String.valueOf(temperature.getHighTemp()));
        lowTempText.setText(String.valueOf(temperature.getLowTemp()));
        icon.setImageResource(Utility.getIconResourceForWeatherCondition(temperature.getWeatherId()));

        Calendar calendar = Calendar.getInstance();
        dateTime.setText(calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE));

        SimpleDateFormat dayFormat = new SimpleDateFormat("E , MMM dd");
//        dateTimeText.setText(calendar.get(Calendar.DAY_OF_WEEK) + "," + calendar.get(Calendar.MONTH) + " " + calendar.get(Calendar.DATE));
        dateTimeText.setText(dayFormat.format(calendar.getTime()));
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed(): Failed to connect, with result: " + connectionResult);

    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        LOGD(TAG, "onDataChanged(): " + dataEvents);

        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                String path = event.getDataItem().getUri().getPath();
                if (DataLayerListenerService.UPDATE_TEMP.equals(path)) {
                    DataMap dataMapItem = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
                    Temperature.getInstance().updateTemp(dataMapItem.getInt("high_temp"), dataMapItem.getInt("low_temp"), dataMapItem.getInt("weather_id"));
                    refreshData();
                } else {
                    LOGD(TAG, "Unrecognized path: " + path);
                }
            }

        }
    }
}
