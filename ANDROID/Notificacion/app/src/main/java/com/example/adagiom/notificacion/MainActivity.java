package com.example.adagiom.notificacion;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    LooperThread looperThread;
    Handler mHandler;
    private Button btn_servicio;
    private Button btn_enviar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_enviar = findViewById(R.id.btn_enviar);
        btn_servicio = findViewById(R.id.btn_servicio);
        btn_servicio.setOnClickListener(onClickListenerServicio);
        btn_enviar.setOnClickListener(onClickListenerEnviar);

        looperThread = new LooperThread();
        looperThread.start();

        MessageQueue messageQueue = Looper.myQueue();

        MessageQueue.IdleHandler idleHandler =  new MessageQueue.IdleHandler() {
            @Override
            public boolean queueIdle() {
                return false;
            }
        };
        messageQueue.addIdleHandler(idleHandler);

        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                System.out.println(msg.obj);
            }
        };

        Thread thread = new Thread(new MyThread());
        thread.start();
    }

    private View.OnClickListener onClickListenerServicio = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(MainActivity.this,Servicio.class);
            startService(intent);
        }
    };
    private View.OnClickListener onClickListenerEnviar = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Message msg = new Message();
            msg.obj = "Mensaje enviado desde Main";
            mHandler.sendMessage(msg);
        }
    };

    private class MyThread implements Runnable {

        @Override
        public void run() {
            while (true) {
                mHandler.obtainMessage();
            }
        }
    }
}
