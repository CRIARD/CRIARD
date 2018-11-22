package app.criard.criardapp;

import java.text.DecimalFormat;
import android.app.Activity;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity{

    private ProgressBar miBarraDeProgreso;
    private ImageView img;
    Integer contador =1;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        miBarraDeProgreso = (ProgressBar) findViewById(R.id.pbar);
        miBarraDeProgreso.setMax(100);
        contador =1;
        miBarraDeProgreso.setVisibility(View.VISIBLE);
        miBarraDeProgreso.setProgress(0);

        img = (ImageView) findViewById(R.id.loadingView);
        img.setBackgroundResource(R.drawable.loading);

        AnimationDrawable animationDrawable = (AnimationDrawable) img.getBackground();
        animationDrawable.start();

        new Load().execute(100);


    }


    public class Load extends AsyncTask <Integer, Integer, String> {

        @Override
        protected String doInBackground(Integer... integers) {
            Log.i("Async","Empieza a ejecutar el hilo");
            while (contador < integers[0]){
                try {
                    //Simula el tiempo aleatorio de descargar una imagen, al dormir unos milisegundos aleatorios al hilo en segundo plano
                    Thread.sleep(200);
                    publishProgress(contador);
                    contador++;
                } catch (InterruptedException e) {
                    cancel(true); //Cancelamos si entramos al catch porque algo ha ido mal
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            Log.i("Async","Preparo el hilo");

            Intent service = new Intent(MainActivity.this,  ServicioBT.class);
            startService(service);
        }

        @Override
        protected void onProgressUpdate(Integer... porcentajeProgreso) {
            Log.i("Async","Actualiza el progress" + porcentajeProgreso[0] );
            miBarraDeProgreso.setProgress(porcentajeProgreso[0]);
        }
        @Override
        protected void onPostExecute(String cantidadProcesados) {
            Log.i("Async","Finaliza el hilo");
            Intent intent = new Intent(MainActivity.this,  CRIArdMainActivity.class);
            startActivity(intent);
        }
    }
}
