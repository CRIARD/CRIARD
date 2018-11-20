package com.example.adagiom.notificacion;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Main";
    private Messenger messenger;
    private Button btn_servicio;
    private Button btn_enviar;
    private boolean mStopLoop;
    private Message messagein;
    Messenger mService = null;
    boolean mBound;
    int count = 0;

    private Handler handler = new Handler() {
        public void handleMessage(Message message) {
            Bundle data = message.getData();
            Log.i("MainActivity","Respuesta recibida");
            if (message.arg1 == Servicio.ESTADO && data != null) {
                String text = data.getString(Servicio.RESULTPATH);
                Toast.makeText(MainActivity.this, text, Toast.LENGTH_LONG).show();
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_enviar = findViewById(R.id.btn_enviar);
        btn_servicio = findViewById(R.id.btn_servicio);
        btn_servicio.setOnClickListener(onClickListenerServicio);
        btn_enviar.setOnClickListener(onClickListenerEnviar);

        Intent intent = new Intent(this,Servicio.class);
        startService(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(getApplicationContext(), Servicio.class);
        Messenger messenger = new Messenger(handler);
        intent.putExtra("MESSENGER", messenger);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        }

    private View.OnClickListener onClickListenerServicio = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Bind to the service

        }
    };

    private View.OnClickListener onClickListenerEnviar = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mStopLoop = true;
            //executeOnCustoLooperWithCustomHandler();
            enviarMensajeAServicio();
        }
    };



    public void enviarMensajeAServicio() {
        if (!mBound) return;
        Message msg = Message.obtain(null, Servicio.GET_COUNT, 0, 0);
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
            mBound = true;
        }

        public void onServiceDisconnected(ComponentName className) {
            mService = null;
            mBound = false;
        }
    };

}
