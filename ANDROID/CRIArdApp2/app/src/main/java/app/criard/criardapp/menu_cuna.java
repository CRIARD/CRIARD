package app.criard.criardapp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.widget.CompoundButton;

import android.os.Handler;
import android.widget.Switch;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class menu_cuna extends Activity implements InterfazAsyntask{

    private Switch btn_led;
    private Switch btn_micro;
    private Switch btn_servo;
    private String ruta;
    private String uri;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_cuna);

        //Bundle bundle = getIntent().getExtras();
        //ruta =(String) bundle.get("ruta");
        btn_led =  (Switch) findViewById(R.id.btn_led);
        btn_micro =  (Switch)findViewById(R.id.btn_micro);
        btn_servo =  (Switch)findViewById(R.id.btn_servo);

        btn_servo.setChecked(false);
        btn_micro.setChecked(false);
        btn_led.setChecked(false);
        btn_led.setOnCheckedChangeListener(checkedChangeListener);
        btn_micro.setOnCheckedChangeListener(checkedChangeListener);
        btn_servo.setOnCheckedChangeListener(checkedChangeListener);
    }

    private CompoundButton.OnCheckedChangeListener checkedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            switch (buttonView.getId()){

                case R.id.btn_led:
                    if(isChecked){
                        uri =  ruta + "led=1";
                        iniciarPeticion(uri);
                    }else{
                        uri =  ruta + "led=0";
                        iniciarPeticion(uri);
                    }
                    break;

                case R.id.btn_micro:
                    if(isChecked){
                        uri =  ruta + "micro=1";
                        iniciarPeticion(uri);
                    }else{
                        uri =  ruta + "micro=0";
                        iniciarPeticion(uri);
                    }
                    break;

                case R.id.btn_servo:
                    if(isChecked){
                        uri =  ruta + "servo=1";
                        iniciarPeticion(uri);
                    }else{
                        uri =  ruta + "servo=0";
                        iniciarPeticion(uri);
                    }
                    break;
            }
        }
    };
    public void iniciarPeticion(final String uri){


    }

    //Metodo que es llamada cuando se cierra la APP
    protected void onDestroy()
    {
        super.onDestroy();

        //Cuando se cierra la APP se detiene la recepcion de los msjs del Servidor
        handler.removeCallbacksAndMessages(null);


        Log.i("LOG","Recepcion de msjs finalizada");
        Toast.makeText(getApplicationContext(),"Cerrando Aplicacion...",Toast.LENGTH_SHORT).show();
    }


    @Override
    public void mostrarToastMake(String msg) {
        Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_LONG).show();
    }

    @Override
    public void construirRespuesta(String input) throws JSONException {

        JSONObject json = new JSONObject(input);

        String sensor = json.getString("sensor");
        Float valor = Float.parseFloat(json.getString("valor"));
        String str = "Sensor: "+ sensor + ", Valor: " + valor;

        mostrarToastMake(str);
    }
}
