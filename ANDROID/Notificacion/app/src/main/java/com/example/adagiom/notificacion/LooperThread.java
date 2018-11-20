package com.example.adagiom.notificacion;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class LooperThread extends Thread {
    public Handler handler;

    @Override
    public void run() {

        Looper.prepare();

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
            }
        };

        Looper.loop();
    }
}
