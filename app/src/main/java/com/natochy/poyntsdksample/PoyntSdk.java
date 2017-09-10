package com.natochy.poyntsdksample;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import co.poynt.api.model.Business;
import co.poynt.os.model.Intents;
import co.poynt.os.model.PoyntError;
import co.poynt.os.services.v1.IPoyntBusinessReadListener;
import co.poynt.os.services.v1.IPoyntBusinessService;
import co.poynt.os.services.v1.IPoyntTransactionService;
import co.poynt.os.services.v1.IPoyntTransactionServiceListener;

/**
 * Created by dennis on 9/9/17.
 */

public class PoyntSdk {

    private static final String TAG = PoyntSdk.class.getSimpleName();
    private Business mBusiness;

    private Context mContext;
    private CountDownLatch mLatch;

    private IPoyntBusinessService mBusinessService;
    private ServiceConnection businessServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mBusinessService = IPoyntBusinessService.Stub.asInterface(iBinder);
                mLatch.countDown();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBusinessService = null;
            Log.d(TAG, "Business Service disconnected");
        }
    };
    private synchronized void bindBusinessService(){
        if (mBusinessService == null) {
            mContext.bindService(Intents.getComponentIntent(Intents.COMPONENT_POYNT_BUSINESS_SERVICE),
                    businessServiceConnection, mContext.BIND_AUTO_CREATE);
        }
    }


    private void checkBusinessService() {
        if (mBusinessService == null) {
            bindBusinessService();
        }
    }

    //region Transaction Service API
    @Nullable
    public Business getBusinessFromCache(){
        return mBusiness;
    }

    public void getBusiness(final IPoyntBusinessReadListener listener){
        checkBusinessService();
        if (mBusinessService != null) {
            try {
                mBusinessService.getBusiness(new IPoyntBusinessReadListener.Stub() {
                    @Override
                    public void onResponse(Business business, PoyntError poyntError) throws RemoteException {
                        // cache business
                        mBusiness = business;
                        listener.onResponse(business, poyntError);
                    }
                });
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }else {
            Handler h = new Handler();
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    getBusiness(listener);
                }
            }, 1000);
        }
    }
    //endregion


    private IPoyntTransactionService mTransactionService;
    private ServiceConnection transactionServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mTransactionService = IPoyntTransactionService.Stub.asInterface(iBinder);
            mLatch.countDown();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mTransactionService = null;
        }
    };
    private synchronized void bindTransactionService(){
        if (mTransactionService == null) {
            mContext.bindService(Intents.getComponentIntent(Intents.COMPONENT_POYNT_TRANSACTION_SERVICE),
                    transactionServiceConnection, mContext.BIND_AUTO_CREATE);
        }
    }

    private void checkTransactionService(){
        if (mTransactionService == null){
            bindTransactionService();
        }
    }

    //region Transaction Service API

    public void getTransaction(final String transactionId, final String requestId,
                               final IPoyntTransactionServiceListener callback){
        checkTransactionService();
        if (mTransactionService != null){
            try {
                mTransactionService.getTransaction(transactionId, requestId, callback);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }else{
            Handler h = new Handler();
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    getTransaction(transactionId, requestId, callback);
                }
            }, 1000);
        }
    }

    //endregion


    public static class Builder{
        private Context mContext;

        private boolean connectBusinessService = false;
        private boolean connectTransactionService = false;
        private int serviceCounter = 0;
        public Builder(Context context){
            mContext = context;
        }
        public Builder initBusinessService(){
            connectBusinessService = true;
            serviceCounter++;
            return this;
        }
        public Builder initTransactionService(){
            connectTransactionService = true;
            serviceCounter++;
            return this;
        }

        public PoyntSdk build(final InitializationListener listener) throws PoyntSdkException{
            return new PoyntSdk(this, serviceCounter, listener);
        }

    }

    /**
     *
     * @param b Builder object
     * @param counter number of AIDL services SDK object binds to
     * @param listener to notify the client when initialization has been successful.
     * @throws PoyntSdkException
     */
    private PoyntSdk (Builder b, int counter, final InitializationListener listener) throws PoyntSdkException{
        this.mContext = b.mContext;
        this.mLatch = new CountDownLatch(counter);
        if (b.connectBusinessService){
            try {
                bindBusinessService();
            }catch (SecurityException se){
                throw new PoyntSdkException(se, "Android Manifest is missing: <uses-permission android:name=\"poynt.permission.BUSINESS_SERVICE\" />");
            }
        }
        if (b.connectTransactionService){
            try {
                bindTransactionService();
            } catch (SecurityException se) {
                throw new PoyntSdkException(se, "Android Manifest is missing: <uses-permission android:name=\"poynt.permission.TRANSACTION_SERVICE\" />");
            }
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mLatch.await(2, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    if (listener!= null){
                        listener.failure();
                    }
                }
                if (mLatch.getCount() > 0){
                    if (listener!=null){
                        listener.failure();
                    }
                }else{
                    listener.success();
                }
            }
        }).start();
    }



    public void cleanup(){
        mContext.unbindService(transactionServiceConnection);
        mContext.unbindService(businessServiceConnection);
    }

    public interface InitializationListener{
        void success();
        void failure();
    }

}
