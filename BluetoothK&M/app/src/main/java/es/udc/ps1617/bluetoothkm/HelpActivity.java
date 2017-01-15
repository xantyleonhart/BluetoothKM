package es.udc.ps1617.bluetoothkm;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class HelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

    }

    public void expandHelp(View v){
        int id = getResources().getIdentifier(v.getTag().toString(), "id", getPackageName());
        TextView helptext = (TextView)findViewById(id);
        if (helptext.getVisibility()==View.GONE){
            //Log.d("HELP","Turning help topic visible");
            helptext.setVisibility(View.VISIBLE);
            Linkify.addLinks(helptext, Linkify.WEB_URLS);
        }else{
            helptext.setVisibility(View.GONE);
           // Log.d("HELP","Turning help topic invisible");
        }

    }
}
