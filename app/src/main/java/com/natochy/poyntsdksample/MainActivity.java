package com.natochy.poyntsdksample;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.UUID;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.poynt.api.model.Business;
import co.poynt.api.model.Card;
import co.poynt.api.model.CardType;
import co.poynt.api.model.FundingSourceAccountType;
import co.poynt.api.model.Transaction;
import co.poynt.os.model.Intents;
import co.poynt.os.model.Payment;
import co.poynt.os.model.PaymentStatus;
import co.poynt.os.model.PoyntError;
import co.poynt.os.services.v1.IPoyntBusinessReadListener;
import co.poynt.os.services.v1.IPoyntTransactionServiceListener;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int COLLECT_PAYMENT_REQUEST = 43123;

    @Bind(R.id.getTransactionBtn)
    Button getTransactionBtn;

    @Bind(R.id.getBusinessFromCacheBtn)
    Button getBusinessFromCacheBtn;

    @Bind(R.id.startPaymentBtn)
    Button startPaymentBtn;

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

    @OnClick(R.id.startPaymentBtn)
    public void startPayment(View view){
        Payment payment = new PaymentBuilder()
                .amount(10000)
                .enableReceiptSelection()
                .create();
        try {
            Intent collectPaymentIntent = new Intent(Intents.ACTION_COLLECT_PAYMENT);
            collectPaymentIntent.putExtra(Intents.INTENT_EXTRAS_PAYMENT, payment);
            startActivityForResult(collectPaymentIntent, COLLECT_PAYMENT_REQUEST);
        } catch (ActivityNotFoundException ex) {
            Log.e(TAG, "Poynt Payment Activity not found - did you install PoyntServices?", ex);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "Received onActivityResult (" + requestCode + ")");
        // Check which request we're responding to
        if (requestCode == COLLECT_PAYMENT_REQUEST) {
            // Make sure the request was successful
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    Payment payment = data.getParcelableExtra(Intents.INTENT_EXTRAS_PAYMENT);
                    Log.d(TAG, "onActivityResult: " + payment);
                }
            }

        }
    }

}

