package com.natochy.poyntsdksample;

import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.UUID;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.poynt.api.model.Business;
import co.poynt.api.model.Transaction;
import co.poynt.os.model.PoyntError;
import co.poynt.os.services.v1.IPoyntBusinessReadListener;
import co.poynt.os.services.v1.IPoyntTransactionServiceListener;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Bind(R.id.getTransactionBtn)
    Button getTransactionBtn;

    @Bind(R.id.getBusinessFromCacheBtn)
    Button getBusinessFromCacheBtn;

    private PoyntSdk.InitializationListener mInitializationListener = new PoyntSdk.InitializationListener() {
        @Override
        public void success() {
            sdk.getBusiness(new IPoyntBusinessReadListener.Stub() {
                @Override
                public void onResponse(Business business, PoyntError poyntError) throws RemoteException {
                    Log.d(TAG, "business: " + business);
                    Log.d(TAG, "error: " + poyntError);
                }

            });
        }

        @Override
        public void failure() {
            //TODO handle sdk intialization failure
        }
    };

    PoyntSdk sdk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        try {
            sdk = new PoyntSdk.Builder(this)
                    .initBusinessService()
                    .initTransactionService()
                    .build(mInitializationListener);
        } catch (PoyntSdkException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to initialize Poynt SDK", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sdk.cleanup();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }


    @OnClick(R.id.getTransactionBtn)
    public void getTransaction(View view) {
        sdk.getTransaction("b5670e5d-01ba-46ef-abae-6f512ce96e6c", UUID.randomUUID().toString(),
                new IPoyntTransactionServiceListener.Stub() {
                    @Override
                    public void onResponse(Transaction transaction, String s, PoyntError poyntError) throws RemoteException {
                        Log.d(TAG, "find Transaction: " + transaction);
                    }

                    @Override
                    public void onLoginRequired() throws RemoteException {

                    }

                    @Override
                    public void onLaunchActivity(Intent intent, String s) throws RemoteException {

                    }
                });
    }

    @OnClick(R.id.getBusinessFromCacheBtn)
    public void getBusinessFromCache(View view){
        Business b = sdk.getBusinessFromCache();
        Toast.makeText(this, b.getDoingBusinessAs(), Toast.LENGTH_SHORT).show();
    }
}

