package app.criard.criardapp;

import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.design.widget.BottomNavigationView;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

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
    private Intent intent;
    private Button btn_encender;
    private Button btn_apagar;
    private Button btn_musicon;
    private Button btn_musicoff;
    Handler bluetoothIn;
    final int handlerState = 0; //used to identify handler message

    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder recDataString = new StringBuilder();

    private ConnectedThread mConnectedThread;

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

                    intent = new Intent(CRIArdMainActivity.this,menu_hogar.class);
                    startActivity(intent);
                    finish();
                    //mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_cuna:

                    intent = new Intent(CRIArdMainActivity.this,menu_cuna.class);
                    startActivity(intent);
                    finish();
                    //mTextMessage.setText(R.string.title_cuna);
                    return true;
                case R.id.navigation_config:

                    intent = new Intent(CRIArdMainActivity.this,MainActivity.class);
                    startActivity(intent);
                    finish();
                    //mTextMessage.setText(R.string.title_config);
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
        //obtengo el adaptador del bluethoot
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        //defino el Handler de comunicacion entre el hilo Principal  el secundario.
        //El hilo secundario va a mostrar informacion al layout atraves utilizando indeirectamente a este handler
        bluetoothIn = Handler_Msg_Hilo_Principal();

        //defino los handlers para los botones Apagar y encender
        btn_encender.setOnClickListener(btnEncenderListener);
        btn_apagar.setOnClickListener(btnApagarListener);
        btn_musicon.setOnClickListener(btnEncenderMusica);
        btn_musicoff.setOnClickListener(btnApagarMusica);
    }

    //Listener del boton encender que envia  msj para enceder Led a Arduino atraves del Bluethoot
    private View.OnClickListener btnEncenderListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mConnectedThread.write("1");    // Send "1" via Bluetooth
            showToast("Mecer Cuna");        }
    };

    //Listener del boton encender que envia  msj para Apagar Led a Arduino atraves del Bluethoot
    private View.OnClickListener btnApagarListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mConnectedThread.write("2");    // Send "0" via Bluetooth
            showToast("Frenar Cuna");
        }
    };

    private View.OnClickListener btnEncenderMusica = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mConnectedThread.write("5");    // Send "1" via Bluetooth
            showToast("Sonar Musica");        }
    };

    private View.OnClickListener btnApagarMusica = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mConnectedThread.write("6");    // Send "1" via Bluetooth
            showToast("Apagar Musica");        }
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

                    if ((event.values[0] > 15) || (event.values[1] > 15) || (event.values[2] > 15)) {
                        acelerometro.setBackgroundColor(Color.parseColor("#cf091c"));
                        //acelerometro.setText("Sensor detectado: ACELEROMETRO");
                        mConnectedThread.write("1");    // Send "1" via Bluetooth
                        showToast("Mecer Cuna");
                    }
                    break;

                case Sensor.TYPE_PROXIMITY :
                    //txt += "proximity\n";
                    //txt += event.values[0] + "\n";

                    //proximity.setText(txt);

                    // Si detecta 0 lo represento
                    if( event.values[0] == 0 )
                    {
                        proximity.setBackgroundColor(Color.parseColor("#7C2F8E"));
                        mConnectedThread.write("3");    // Send "1" via Bluetooth
                        showToast("EncenderLuz");
                        //proximity.setText("Proximidad Detectada");
                    }
                    else {
                        proximity.setBackgroundColor(Color.parseColor("#FAFAFA"));
                        mConnectedThread.write("4");    // Send "1" via Bluetooth
                        showToast("ApagarLuz");
                        //proximity.setText(txt);
                    }
                    break;

                case Sensor.TYPE_LIGHT :

                    if( event.values[0] < 21) { //Celular de Erik, pueden cambiar
                        luminosidad.setBackgroundColor(Color.parseColor("#256F3E"));
                        mConnectedThread.write("5");    // Send "1" via Bluetooth
                        showToast("EncenderMicro");
                    }
                    if( event.values[0] > 121) { //Celular de Erik, pueden cambiar
                        luminosidad.setBackgroundColor(Color.parseColor("#FAFAFA"));
                        mConnectedThread.write("6");    // Send "1" via Bluetooth
                        showToast("ApagarMicro");
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

        //Obtengo el parametro, aplicando un Bundle, que me indica la Mac Adress del HC05
        Intent intent=getIntent();
        Bundle extras=intent.getExtras();

        address= extras.getString("Direccion_Bluethoot");

        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        //se realiza la conexion del Bluethoot crea y se conectandose a atraves de un socket
        try
        {
            btSocket = createBluetoothSocket(device);
        }
        catch (IOException e)
        {
            showToast( "La creacción del Socket fallo");
        }
        // Establish the Bluetooth socket connection.
        try
        {
            btSocket.connect();
        }
        catch (IOException e)
        {
            try
            {
                btSocket.close();
            }
            catch (IOException e2)
            {
                //insert code to deal with this
            }
        }

        //Una establecida la conexion con el Hc05 se crea el hilo secundario, el cual va a recibir
        // los datos de Arduino atraves del bluethoot
        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();

        //I send a character when resuming.beginning transmission to check device is connected
        //If it is not an exception will be thrown in the write method and finish() will be called
        mConnectedThread.write("x");

        Ini_Sensores();
    }

    //Metodo que crea el socket bluethoot
    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }

    //Handler que sirve que permite mostrar datos en el Layout al hilo secundario
    private Handler Handler_Msg_Hilo_Principal ()
    {
        return new Handler() {
            public void handleMessage(android.os.Message msg)
            {
                //si se recibio un msj del hilo secundario
                if (msg.what == handlerState)
                {
                    //voy concatenando el msj
                    String readMessage = (String) msg.obj;
                    recDataString.append(readMessage);
                    int endOfLineIndex = recDataString.indexOf("\r\n");

                    //cuando recibo toda una linea la muestro en el layout
                    if (endOfLineIndex > 0)
                    {
                        String dataInPrint = recDataString.substring(0, endOfLineIndex);
                        //txtPotenciometro.setText(dataInPrint);

                        recDataString.delete(0, recDataString.length());
                    }
                }
            }
        };

    }


    //******************************************** Hilo secundario del Activity**************************************
    //*************************************** recibe los datos enviados por el HC05**********************************

    private class ConnectedThread extends Thread
    {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        //Constructor de la clase del hilo secundario
        public ConnectedThread(BluetoothSocket socket)
        {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try
            {
                //Create I/O streams for connection
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        //metodo run del hilo, que va a entrar en una espera activa para recibir los msjs del HC05
        public void run()
        {
            byte[] buffer = new byte[256];
            int bytes;

            //el hilo secundario se queda esperando mensajes del HC05
            while (true)
            {
                try
                {
                    //se leen los datos del Bluethoot
                    bytes = mmInStream.read(buffer);
                    String readMessage = new String(buffer, 0, bytes);

                    //se muestran en el layout de la activity, utilizando el handler del hilo
                    // principal antes mencionado
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }

        //write method
        public void write(String input) {
            byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(msgBuffer);                //write bytes over BT connection via outstream
            } catch (IOException e) {
                //if you cannot write, close the application
                showToast("La conexion fallo");
                finish();

            }
        }
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}
