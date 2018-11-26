package app.criard.criardapp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.design.widget.BottomNavigationView;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.MenuItem;
import android.widget.CompoundButton;
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
import android.widget.ToggleButton;

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
    private ToggleButton btn_cuna;
    private ToggleButton btn_musica;
    Messenger mService = null;
    private HandlerActivity handler;
    boolean mBound;
    private boolean flagAcelerometro;
    private boolean flagLuz = false;
    private int contador = 0;
    private ActualizarCuna actualizarCuna;
    Bundle extra;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    Intent intent = new Intent(CRIArdMainActivity.this,Informe_temperatura.class);
                    startActivity(intent);
                    finish();
                    return true;
                case R.id.navigation_cuna:
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_criard_main);

        actualizarCuna = new ActualizarCuna();
        handler = HandlerActivity.getInstance();
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setSelectedItemId(R.id.navigation_cuna);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        acelerometro = (TextView) findViewById(R.id.acelerometro);
        proximity    = (TextView) findViewById(R.id.proximity);
        luminosidad  = (TextView) findViewById(R.id.luminosidad);

        btn_cuna = (ToggleButton) findViewById(R.id.cuna);
        btn_musica = (ToggleButton) findViewById(R.id.musica);
        txt_led = (TextView) findViewById(R.id.txt_led);
        txt_micro = (TextView) findViewById(R.id.txt_micro);
        txt_servo = (TextView) findViewById(R.id.txt_servo);

        btn_cuna.setOnCheckedChangeListener(btnAccionCuna);
        btn_musica.setOnCheckedChangeListener(btnAccionMusica);
        //obtengo el adaptador del bluethoot
        IntentFilter filter = new IntentFilter();

        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED); //Cambia el estado del Bluethoot (Acrtivado /Desactivado)
        //se define (registra) el handler que captura los broadcast anterirmente mencionados.
        registerReceiver(mReceiver, filter);
        new Thread(new Runnable() {

            @Override
            public void run() {
                // El servicio se finaliza a sí mismo cuando finaliza su
                // trabajo.

                try {
                    while(!mBound) {
                        // Simulamos trabajo de 2 segundos.
                        Thread.sleep(2000);
                        Message msg = Message.obtain(null, ServicioBT.GET_INFO, 0, 0);
                        try {
                            mService.send(msg);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }).start();
        actualizarCuna.execute();
    }

    //Listener del boton encender que envia  msj para enceder Servo a Arduino atraves del Bluethoot
    private ToggleButton.OnCheckedChangeListener btnAccionCuna = new ToggleButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if(isChecked){
                Message msg = Message.obtain(null, ServicioBT.GET_SERVO_ON, 0, 0);
                try {
                    mService.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }else{
                Message msg = Message.obtain(null, ServicioBT.GET_SERVO_OFF, 0, 0);
                try {
                    mService.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    //Listener del boton encender que envia  msj para enceder Servo a Arduino atraves del Bluethoot
    private ToggleButton.OnCheckedChangeListener btnAccionMusica = new ToggleButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if(isChecked){
                Message msg = Message.obtain(null, ServicioBT.GET_MUSICA_ON, 0, 0);
                try {
                    mService.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }else{
                Message msg = Message.obtain(null, ServicioBT.GET_MUSICA_OFF, 0, 0);
                try {
                    mService.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    @Override
    public void onSensorChanged(SensorEvent event) {
        String txt = "";

        synchronized (this) {
            Log.d("sensor", event.sensor.getName());

            switch (event.sensor.getType()) {

                case Sensor.TYPE_ACCELEROMETER:

                    if ((event.values[0] > 20) || (event.values[1] > 20)) {

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

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

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
    protected void onStop()
    {

        Parar_Sensores();
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(getApplicationContext(), ServicioBT.class);
        Messenger messenger = new Messenger(handler);
        intent.putExtra("MESSENGER", messenger);
        intent.putExtra("CLIENTE",ServicioBT.ACTIVITY_CRIARD);

        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }
    @Override
    protected void onDestroy()
    {
        Parar_Sensores();
        unbindService(mConnection);
        unregisterReceiver(mReceiver);
        actualizarCuna.cancel(true);
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
        Ini_Sensores();
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
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

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)){
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,BluetoothAdapter.ERROR);
                if(state == BluetoothAdapter.STATE_DISCONNECTED || state == BluetoothAdapter.STATE_OFF){
                    Toast.makeText(CRIArdMainActivity.this,"Se ha perdido la conexion..",Toast.LENGTH_SHORT).show();
                    unbindService(mConnection);
                    AlertDialog alerta = createSimpleDialog();
                    alerta.show();
                }
            }
        }
    };

    public AlertDialog createSimpleDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Error")
                .setMessage("Se ha perdido la conexion.")
                .setNegativeButton("Aceptar",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(CRIArdMainActivity.this,ServicioBT.class);
                                stopService(intent);
                                finish();
                            }
                        });

        return builder.create();
    }

    @Override
    public void onBackPressed() {

        if(contador == 0){
            Toast.makeText(getApplicationContext(),"Presione nuevamente para salir",Toast.LENGTH_SHORT).show();
            contador++;
        }else{
            Intent intent = new Intent(CRIArdMainActivity.this,ServicioBT.class);
            stopService(intent);
            finish();
        }

        new CountDownTimer(3000,1000){

            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                contador=0;
            }
        }.start();
    }

    public class ActualizarCuna extends AsyncTask<Void, String, String> {

        @Override
        protected String doInBackground(Void... strings) {
            Log.i("Async","Empieza a ejecutar el hilo");
            while (true){
                if(isCancelled())break;
                try {
                    //Simula el tiempo aleatorio de descargar una imagen, al dormir unos milisegundos aleatorios al hilo en segundo plano
                    Thread.sleep(2000);
                    publishProgress(handler.dato_cuna);
                } catch (InterruptedException e) {
                    cancel(true); //Cancelamos si entramos al catch porque algo ha ido mal
                    e.printStackTrace();
                }

            }
            return null;
        }


        @Override
        protected void onPreExecute() {

        }

        @Override
        protected void onProgressUpdate(String... datos) {

            String text;
            int servo_encendido;
            int servo_apagado;
            int led_encendido;
            int led_apagado;
            int micro_encendido;
            int micro_apagado;
            int humedad_encendido;
            int humedad_apagado;
            servo_encendido = datos[0].indexOf("Q");
            servo_apagado = datos[0].indexOf("W");
            led_encendido = datos[0].indexOf("E");
            led_apagado = datos[0].indexOf("R");
            micro_encendido = datos[0].indexOf("M");
            micro_apagado = datos[0].indexOf("Y");
            humedad_encendido = datos[0].indexOf("U");
            humedad_apagado = datos[0].indexOf("I");

            if(servo_encendido >= 0){
                txt_servo.setText("Meciendo");
                txt_servo.setBackgroundResource(R.drawable.encendido);
            }
            if(servo_apagado >= 0){
                txt_servo.setText("En Reposo");
                txt_servo.setBackgroundResource(R.drawable.apagado);
            }
            if(led_encendido >= 0){
                txt_led.setText("Luz encendida");
                txt_led.setBackgroundResource(R.drawable.encendido);
            }
            if(led_apagado >= 0) {
                txt_led.setText("Luz Apagada");
                txt_led.setBackgroundResource(R.drawable.apagado);
            }
            if(micro_encendido >= 0){
                txt_micro.setText("Llorando");
                txt_micro.setBackgroundResource(R.drawable.encendido);
            }
            if(micro_apagado >= 0){
                txt_micro.setText("Durmiendo");
                txt_micro.setBackgroundResource(R.drawable.apagado);
            }
            if(humedad_encendido >= 0){
                Toast.makeText(CRIArdMainActivity.this, datos[0], Toast.LENGTH_LONG).show();
            }
        }
        @Override
        protected void onPostExecute(String cantidadProcesados) {
        }
    }
}
