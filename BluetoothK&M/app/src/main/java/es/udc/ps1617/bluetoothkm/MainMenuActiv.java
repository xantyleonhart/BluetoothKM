package es.udc.ps1617.bluetoothkm;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

public class MainMenuActiv extends AppCompatActivity implements View.OnClickListener {

    Button but_start;
    Button but_exit;
    Button but_help;
    Button but_settings;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        but_start= (Button)findViewById(R.id.but_start);
        but_start.setOnClickListener(this);

        but_exit= (Button)findViewById(R.id.but_exit);
        but_exit.setOnClickListener(this);

        but_help=(Button)findViewById(R.id.but_help);
        but_help.setOnClickListener(this);

        but_settings=(Button)findViewById(R.id.but_settings);
        but_settings.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
        if (v==but_start){
            Intent intent = new Intent(this,DeviceSelectActiv.class);
            startActivity(intent);
        }else if (v==but_help){
            Intent intent = new Intent(this,HelpActivity.class);
            intent.putExtra("page",1);
            startActivity(intent);
        }else if (v==but_settings){
            Intent intent = new Intent(this,SettingsActiv.class);
            startActivity(intent);
        }else if (v==but_exit){
            finish();
        }
    }
}
