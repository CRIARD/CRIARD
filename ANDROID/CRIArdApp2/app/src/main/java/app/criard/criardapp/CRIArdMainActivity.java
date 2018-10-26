package app.criard.criardapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.design.widget.BottomNavigationView;
import android.support.annotation.NonNull;
import android.view.MenuItem;
import android.widget.TextView;

public class CRIArdMainActivity extends AppCompatActivity {

    private TextView mTextMessage;
    private String puerto;
    private String ip;
    private String ruta;
    private Intent intent;

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
        mTextMessage = (TextView) findViewById(R.id.message);

        mTextMessage.setText("Se encuentra conectado a la direccion: " + this.ip + " puerto: " + this.puerto);
        armarRuta();
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    private void armarRuta(){

        this.ruta = "http://" + this.ip  + "/";
    }

}
