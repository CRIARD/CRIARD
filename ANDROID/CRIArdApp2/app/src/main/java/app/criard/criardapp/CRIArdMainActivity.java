package app.criard.criardapp;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.support.design.widget.BottomNavigationView;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import java.sql.Time;
import java.text.DecimalFormat;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
public class CRIArdMainActivity extends AppCompatActivity implements SensorEventListener{

    private SensorManager mSensorManager;
    private TextView acelerometro;
    private TextView proximity;
    private TextView luminosidad;
    private TextView txt_servo;
    private TextView txt_led;
    private TextView txt_micro;
    private Intent intent;
    private Button btn_encender;
    private Button btn_apagar;
    private Button btn_musicon;
    private Button btn_musicoff;
    private Message messagein;
    Messenger mService = null;
    boolean mBound;
    int count = 0;
    private boolean flagAcelerometro;
    private boolean flagLuz = false;
    final int handlerState = 0; //used to identify handler message

    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder recDataString = new StringBuilder();
    // SPP UUID service  - Funciona en la mayoria de los dispositivos
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // String for MAC address del Hc05
    private static String address = null;
    //DecimalFormat dosdecimales = new DecimalFormat("###.###");

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    return true;
                case R.id.navigation_cuna:
                    return true;
                case R.id.navigation_config:
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_criard_main);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        acelerometro = (TextView) findViewById(R.id.acelerometro);
        proximity    = (TextView) findViewById(R.id.proximity);
        luminosidad  = (TextView) findViewById(R.id.luminosidad);
        btn_apagar = (Button) findViewById(R.id.btn_apagar);
        btn_encender = (Button) findViewById(R.id.btn_encender);
        btn_musicon = (Button) findViewById(R.id.btn_microon);
        btn_musicoff = (Button) findViewById(R.id.btn_microoff);
        txt_led = (TextView) findViewById(R.id.txt_led);
        txt_led.setText("Luz Apagada");
        txt_micro = (TextView) findViewById(R.id.txt_micro);
        txt_micro.setText("Silencio");
        txt_servo = (TextView) findViewById(R.id.txt_servo);
        txt_servo.setText("En Reposo");
        //obtengo el adaptador del bluethoot
        //btAdapter = BluetoothAdapter.getDefaultAdapter();
        //defino el Handler de comunicacion entre el hilo Principal  el secundario.
        //El hilo secundario va a mostrar informacion al layout atraves utilizando indeirectamente a este handler


        //defino los handlers para los botones Apagar y encender
        btn_encender.setOnClickListener(btnEncenderListener);
        btn_apagar.setOnClickListener(btnApagarListener);
        btn_musicon.setOnClickListener(btnEncenderMusica);
        btn_musicoff.setOnClickListener(btnApagarMusica);

        Intent intent = new Intent(getApplicationContext(), ServicioBT.class);
        Messenger messenger = new Messenger(handler);
        intent.putExtra("MESSENGER", messenger);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    //Listener del boton encender que envia  msj para enceder Servo a Arduino atraves del Bluethoot
    private View.OnClickListener btnEncenderListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!mBound) return;
            Message msg = Message.obtain(null, ServicioBT.GET_SERVO_ON, 0, 0);
            try {
                mService.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
             }
    };

    //Listener del boton encender que envia  msj para Apagar Led a Arduino atraves del Bluethoot
    private View.OnClickListener btnApagarListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!mBound) return;
            Message msg = Message.obtain(null, ServicioBT.GET_SERVO_OFF, 0, 0);
            try {
                mService.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    };

    private View.OnClickListener btnEncenderMusica = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!mBound) return;
            Message msg = Message.obtain(null, ServicioBT.GET_MUSICA_ON, 0, 0);
            try {
                mService.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    };

    private View.OnClickListener btnApagarMusica = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!mBound) return;
            Message msg = Message.obtain(null, ServicioBT.GET_MUSICA_OFF, 0, 0);
            try {
                mService.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public void onSensorChanged(SensorEvent event) {
        String txt = "";

        // Cada sensor puede lanzar un thread que pase por aqui
        // Para asegurarnos ante los accesos simult�neos sincronizamos esto

        synchronized (this) {
            Log.d("sensor", event.sensor.getName());

            switch (event.sensor.getType()) {

                case Sensor.TYPE_ACCELEROMETER:

                    if ((event.values[0] > 20) || (event.values[1] > 20) || (event.values[2] > 20)) {

                        if(!flagAcelerometro){
                            acelerometro.setBackgroundColor(Color.parseColor("#cf091c"));
                            if (!mBound) return;
                            Message msg = Message.obtain(null, ServicioBT.GET_SERVO_ON, 0, 0);
                            try {
                                mService.send(msg);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                            flagAcelerometro = true;
                            acelerometro.setText("\n" +"Acelerometro");
                        }else {
                            acelerometro.setBackgroundColor(Color.TRANSPARENT);
                            if (!mBound) return;
                            Message msg = Message.obtain(null, ServicioBT.GET_SERVO_OFF, 0, 0);
                            try {
                                mService.send(msg);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                            flagAcelerometro = false;
                            acelerometro.setText(" ");
                        }
                    }
                    break;

                case Sensor.TYPE_PROXIMITY :
                    // Si detecta 0 lo represento
                    if( event.values[0] == 0 )
                    {
                        proximity.setBackgroundColor(Color.parseColor("#7C2F8E"));
                        if (!mBound) return;
                        Message msg = Message.obtain(null, ServicioBT.GET_LED_ON, 0, 0);
                        try {
                            mService.send(msg);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                       proximity.setText(" Proximidad ");
                    }
                    else {
                        proximity.setBackgroundColor(Color.parseColor("#FAFAFA"));
                        if (!mBound) return;
                        Message msg = Message.obtain(null, ServicioBT.GET_LED_OFF, 0, 0);
                        try {
                            mService.send(msg);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                        proximity.setText(" ");
                    }
                    break;

                case Sensor.TYPE_LIGHT :

                    if( event.values[0] < 21) { //Celular de Erik, pueden cambiar
                        if(!flagLuz) {
                            luminosidad.setBackgroundColor(Color.parseColor("#256F3E"));
                            if (!mBound) return;
                            Message msg = Message.obtain(null, ServicioBT.GET_MUSICA_ON, 0, 0);
                            try {
                                mService.send(msg);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                            flagLuz=true;
                            luminosidad.setText("Luz ambiente");
                        }else{
                            luminosidad.setBackgroundColor(Color.TRANSPARENT);
                            if (!mBound) return;
                            Message msg = Message.obtain(null, ServicioBT.GET_MUSICA_OFF, 0, 0);
                            try {
                                mService.send(msg);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                            flagLuz=false;
                            luminosidad.setText(" ");
                        }
                    }
                    if( event.values[0] > 121) { //Celular de Erik, pueden cambiar
                        luminosidad.setBackgroundColor(Color.parseColor("#FAFAFA"));
                        //mConnectedThread.write("6");    // Send "1" via Bluetooth
                      //  showToast("ApagarMicro");
                        luminosidad.setText(" ");
                    }
                    break;
            }
        }
    }

    // Metodo para iniciar el acceso a los sensores
    protected void Ini_Sensores()
    {
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),   SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY),       SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT),           SensorManager.SENSOR_DELAY_NORMAL);
    }

    // Metodo para parar la escucha de los sensores
    private void Parar_Sensores()
    {

        mSensorManager.unregisterListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
        mSensorManager.unregisterListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY));
        mSensorManager.unregisterListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onStop()
    {

        Parar_Sensores();

        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();

    }
    @Override
    protected void onDestroy()
    {
        Parar_Sensores();

        super.onDestroy();
    }

    @Override
    protected void onPause()
    {
        Parar_Sensores();

        super.onPause();
    }

    @Override
    protected void onRestart()
    {
        Ini_Sensores();

        super.onRestart();
    }


    @Override
    protected void onResume()
    {
        super.onResume();
        Log.i("Main","entre al onResume");
        if (!mBound) return;
        Message msg = Message.obtain(null, ServicioBT.GET_INFO, 0, 0);
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        Ini_Sensores();
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private Handler handler = new Handler() {
        public void handleMessage(Message message) {
            Bundle data = message.getData();
            String text;
            int servo_encendido;
            int servo_apagado;
            int led_encendido;
            int led_apagado;
            int micro_encendido;
            int micro_apagado;
            int humedad_encendido;
            int humedad_apagado;
            Log.i("MainActivity","Respuesta recibida");
            Log.i("MainActivity", String.valueOf(message.arg1));
            switch (message.arg1){
                case ServicioBT.GET_RESPUESTA:
                    text = data.getString(ServicioBT.RESULTPATH);
                    servo_encendido = text.indexOf("SE");
                    led_encendido = text.indexOf("LE");
                    micro_encendido = text.indexOf("ME");
                    humedad_encendido = text.indexOf("H1");

                    if(servo_encendido >= 0){
                        showToast("Meciendo Cuna");
                        txt_servo.setText("Meciendo");
                        txt_servo.setBackgroundResource(R.drawable.encendido);
                    }else{
                        showToast("Cuna en reposo");
                        txt_servo.setText("En Reposo");
                        txt_servo.setBackgroundResource(R.drawable.apagado);
                    }
                    if(led_encendido >= 0){
                        txt_led.setText("Luz encendida");
                        txt_led.setBackgroundResource(R.drawable.encendido);
                    }else{
                        txt_led.setText("Luz Apagada");
                        txt_led.setBackgroundResource(R.drawable.apagado);
                    }
                    if(micro_encendido >= 0){
                        txt_micro.setText("Musica encendida");
                        txt_micro.setBackgroundResource(R.drawable.encendido);
                    }else{
                        txt_micro.setText("Musica Apagada");
                        txt_micro.setBackgroundResource(R.drawable.apagado);
                    }
                    if(humedad_encendido >= 0){
                        Toast.makeText(CRIArdMainActivity.this, text, Toast.LENGTH_LONG).show();
                    }
                    break;
            }
        }
    };

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
