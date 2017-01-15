package es.udc.ps1617.bluetoothkm;

import android.app.Fragment;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class SettingsActiv extends AppCompatActivity implements OnMacroClickedListener{

    final static String TAG="SettingsActiv";
    Menu menu;
    MacroFragm fragm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        fragm = (MacroFragm) getSupportFragmentManager().findFragmentById(R.id.fragm_macro);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu= menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_add:

                InitializeDialogBuilder().create().show();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onMacroEdit(Button b) {
        InitializeDialogBuilder(b.getText().toString(),b.getTag().toString(),b.getId())
                .create().show();
    }

    //en settings, hacer click en el boton sirve para testear
    @Override
    public void onMacroClick(Button b,int press) {
        if(press==1) {
            Toast.makeText(this, b.getTag().toString(), Toast.LENGTH_SHORT).show();
        }
    }


    /*esto crea un nuevo AlertDialog Builder. Es necesario hacerlo de esta manera ya que el builder
     debe ser declarado "final" para poder acceder a los elementos de su layout y configurar los
     listeners*/
    private AlertDialog.Builder InitializeDialogBuilder(String def_name, String def_keys, final int index){
        final AlertDialog.Builder builder =
                new AlertDialog.Builder(this);

        builder.setTitle(getResources().getString(R.string.add_str));
        View diaglayout = (LinearLayout) LayoutInflater.from(builder.getContext()).
                inflate(R.layout.add_macro_dialog,null);

        final EditText et_name = (EditText)diaglayout.findViewById(R.id.et_macroName);
        et_name.setText(def_name);
        final EditText et_keys = (EditText)diaglayout.findViewById(R.id.et_keylist);
        et_keys.setText(def_keys);

        Button but_adkey = (Button)diaglayout.findViewById(R.id.but_addkey);
        but_adkey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(),SelectKeysActiv.class);
                startActivityForResult(intent,Utils.KEY_PRESS);
            }
        });
        builder.setView(diaglayout);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String[]keyCodes = et_keys.getText().toString().split("/");
                for(String s:keyCodes){
                    if (KeyEvent.keyCodeFromString(s)==KeyEvent.KEYCODE_UNKNOWN){
                        dialog.cancel();
                        Log.d(TAG,"Invalid keyCode: "+s);
                        return;
                    }
                }
                //KeyMacro macro = new KeyMacro(0);
                try {
                    JSONArray array = Utils.loadJSON(getBaseContext(),Utils.MACRO_FILENAME)
                                                        .getJSONArray("macros");
                    KeyMacro macro = new KeyMacro(et_name.getText().toString(),
                                                        et_keys.getText().toString());
                    if (index<0) {//si el índice recibido es menor de 0, es una nueva macro, la
                        // añadimos al final del fichero
                        array.put(macro.toJSON());
                    }else{//sino, editamos la entrada correspondiente
                        array.put(index,macro.toJSON());
                    }
                    Utils.writeJSON(getBaseContext(),Utils.MACRO_FILENAME,
                                                            new JSONObject().put("macros",array));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                dialog.dismiss();

            }
        });
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        //cuando se pulsa "OK" y se cierra el diálogo, actualizar el layout del fragmento
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                fragm.reload();
            }
        });
        return builder;
    }

    //Sirve para pasarlo sin parámetros en caso de que sea el menú de crear una nueva macro
    private AlertDialog.Builder InitializeDialogBuilder(){
        return InitializeDialogBuilder("","",-1);
    }
}
