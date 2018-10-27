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

public class CRIArdMainActivity extends AppCompatActivity implements SensorEventListener{

    private TextView mTextMessage;
    private SensorManager mSensorManager;
    private TextView acelerometro;
    private TextView proximity;
    private TextView luminosidad;
    private String puerto;
    private String ip;
    private String ruta;
    private Intent intent;
    //DecimalFormat dosdecimales = new DecimalFormat("###.###");

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:

                    intent = new Intent(CRIArdMainActivity.this,menu_hogar.class);
                    intent.putExtra("ruta",ruta);
                    startActivity(intent);
                    finish();
                    //mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_cuna:

                    intent = new Intent(CRIArdMainActivity.this,menu_cuna.class);
                    intent.putExtra("ruta",ruta);
                    startActivity(intent);
                    finish();
                    //mTextMessage.setText(R.string.title_cuna);
                    return true;
                case R.id.navigation_config:

                    intent = new Intent(CRIArdMainActivity.this,MainActivity.class);
                    intent.putExtra("ruta",ruta);
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

        Bundle bundle =  getIntent().getExtras();
        this.ip = (String) bundle.get("ip");
        this.puerto = (String) bundle.get("puerto");
        mTextMessage = (TextView) findViewById(R.id.msg);

        mTextMessage.setText("Se encuentra conectado a la direccion: " + this.ip + " puerto: " + this.puerto);
        armarRuta();
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        acelerometro = (TextView) findViewById(R.id.acelerometro);
        proximity    = (TextView) findViewById(R.id.proximity);
        luminosidad  = (TextView) findViewById(R.id.luminosidad);


    }

    private void armarRuta(){

        this.ruta = "http://" + this.ip  + "/";
    }

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
                        acelerometro.setText("Sensor detectado: ACELEROMETRO");
                    }
                    break;

                case Sensor.TYPE_PROXIMITY :
                    txt += "proximity\n";
                    txt += event.values[0] + "\n";

                    proximity.setText(txt);

                    // Si detecta 0 lo represento
                    if( event.values[0] == 0 )
                    {
                        proximity.setBackgroundColor(Color.parseColor("#7C2F8E"));
                        proximity.setText("Proximidad Detectada");
                    }
                    else {
                        proximity.setBackgroundColor(Color.parseColor("#FAFAFA"));
                        proximity.setText(txt);
                    }
                    break;

                case Sensor.TYPE_LIGHT :
                    txt += "Luminosidad\n";
                    txt += event.values[0] + " Lux \n";

                    if( event.values[0] < 90)
                        luminosidad.setBackgroundColor(Color.parseColor("#256F3E"));
                    else
                        luminosidad.setBackgroundColor(Color.parseColor("#FAFAFA"));
                    luminosidad.setText(txt);
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

        Ini_Sensores();
    }
}
