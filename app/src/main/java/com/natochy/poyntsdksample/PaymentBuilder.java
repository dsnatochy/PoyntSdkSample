package com.natochy.poyntsdksample;

import java.util.Currency;
import java.util.Locale;

import co.poynt.os.model.Payment;

/**
 * Created by dennis on 9/10/17.
 */

public class PaymentBuilder {
    private Payment mPayment;
    public PaymentBuilder(){
        mPayment = new Payment();
        mPayment.setSkipReceiptScreen(true);
        mPayment.setSkipSignatureScreen(true);
        mPayment.isDisableTip();
        // set default currency for the locale
        Locale locale = Locale.getDefault();
        mPayment.setCurrency(Currency.getInstance(locale).getCurrencyCode());
    }

    public PaymentBuilder amount(long amount){
        mPayment.setAmount(amount);
        return this;
    }

    public PaymentBuilder enableTipCapture(){
        mPayment.setDisableTip(false);
        return this;
    }
    public PaymentBuilder enableSignatureCapture(){
        mPayment.setSkipReceiptScreen(false);
        return this;
    }

    public PaymentBuilder enableReceiptSelection(){
        mPayment.setSkipReceiptScreen(false);
        return this;
    }

    public Payment create(){
        if (mPayment.getAmount() == 0){
            throw new IllegalStateException("payment amount is not set");
        }
        return mPayment;
    }
}
