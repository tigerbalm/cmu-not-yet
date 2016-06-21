package com.lge.notyet.server.proxy;

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

    public void verify(String cardNumber, int expireYear, int expireMonth) {

    }

    public void makePayment() {

    }
}
