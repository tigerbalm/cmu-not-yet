package com.lge.notyet.server.proxy;

import com.lge.notyet.server.exception.InvalidCardInformationException;
import io.vertx.core.AsyncResult;
import io.vertx.core.AsyncResultHandler;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class CreditCardProxy {
    private static CreditCardProxy instance = null;

    private Logger logger;

    private CreditCardProxy() {
        this.logger = LoggerFactory.getLogger(DatabaseProxy.class);
    }

    public static CreditCardProxy getInstance() {
        synchronized (CreditCardProxy.class) {
            if (instance == null) {
                instance = new CreditCardProxy();
            }
            return instance;
        }
    }

    // TODO: implement a real thing
    public void verify(String cardNumber, String cardExpiration, Handler<AsyncResult<Boolean>> handler) {
        if (cardNumber == null || cardExpiration == null || cardNumber.isEmpty() || cardExpiration.isEmpty()) {
            handler.handle(Future.failedFuture(new InvalidCardInformationException()));
        } else {
            boolean verified = true;
            handler.handle(Future.succeededFuture(verified));
        }
    }

    // TODO: implement a real thing
    public void makePayment(String cardNumber, String cardExpiration, int amount, Handler<AsyncResult<Long>> handler) {
        if (amount <= 0) {
            handler.handle(Future.failedFuture(new IllegalArgumentException()));
        } else if (cardNumber == null || cardExpiration == null || cardNumber.isEmpty() || cardExpiration.isEmpty()) {
            handler.handle(Future.failedFuture(new InvalidCardInformationException()));
        } else {
            long paymentId = 2016062214523L;
            handler.handle(Future.succeededFuture(paymentId));
        }
    }
}