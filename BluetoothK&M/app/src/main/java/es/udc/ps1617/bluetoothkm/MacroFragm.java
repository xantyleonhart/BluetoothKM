package es.udc.ps1617.bluetoothkm;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class MacroFragm extends Fragment implements View.OnClickListener {
    public final String TAG="MacroFragment";

    JSONArray macros = null;
    View lastContextMenuButton=null;


    public OnMacroClickedListener emptyListener = new OnMacroClickedListener() {

        @Override
        public void onMacroEdit(Button b) {

        }

        @Override
        public void onMacroClick(Button b,int pressed) {

        }
    };

    private OnMacroClickedListener mListener;

    public MacroFragm() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_macro, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        load_macros();
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d(TAG, "onAttach()");
        try {
            mListener = (OnMacroClickedListener) activity;
        } catch (ClassCastException e) {
            mListener=emptyListener;
        }
    }

    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");



    }

    public void addButton(int id, String text, String codes){
        GridLayout layout = (GridLayout)getView().findViewById(R.id.layout_macro);
        Button but = new Button(getContext());

        //Establecer el parámetro columnWeight precisa de la API 21, por lo que utilizamos
        //una vista de referencia para cargar los parámetros desde ahí
        but.setLayoutParams(LayoutInflater.from(getContext()).
                inflate(R.layout.default_button_layout_params,null).
                findViewById(R.id.default_layout_grid).getLayoutParams());
        but.setId(id);
        but.setText(text);
        but.setTag(codes);

        but.setOnClickListener(this);
        //but.setOnLongClickListener(this);
        layout.addView(but);
        registerForContextMenu(but);
    }


    @Override
    public void onCreateContextMenu(ContextMenu cmenu, View v, ContextMenu.ContextMenuInfo mInfo) {
        super.onCreateContextMenu(cmenu, v, mInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.macro_context_menu, cmenu);
        lastContextMenuButton=v;
        //solo permite editar macros desde la actividad Settings
        if(!(getActivity() instanceof SettingsActiv)){
            cmenu.removeItem(R.id.act_edit);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.act_delete:
                Button but = (Button)lastContextMenuButton;
                int id = but.getId();
                try {
                    macros = Utils.JSONremove(id,macros);

                    Utils.writeJSON(getContext(),Utils.MACRO_FILENAME,new JSONObject().put("macros",macros));
                    Log.d(TAG,"JSON macro deleted");
                    reload();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return true;
            case R.id.act_edit:
                mListener.onMacroEdit((Button)lastContextMenuButton);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    public void reload(){
        GridLayout layout = (GridLayout)getView().findViewById(R.id.layout_macro);
        layout.removeAllViews();
        load_macros();
    }

    private void load_macros(){
        try {
            JSONObject json = Utils.loadJSON(getContext(),Utils.MACRO_FILENAME);
            if(json==null){
                Log.d(TAG,"JSON not found");
                json = new JSONObject();
                macros = new JSONArray();
                //Open terminal on linux
                macros.put(new KeyMacro("Terminal","KEYCODE_CTRL_LEFT/KEYCODE_ALT_LEFT/KEYCODE_T").toJSON());
                macros.put(new KeyMacro("Close","KEYCODE_CTRL_LEFT/KEYCODE_ALT_LEFT/KEYCODE_F4").toJSON());
                macros.put(new KeyMacro("Copy","KEYCODE_CTRL_LEFT/KEYCODE_C").toJSON());
                macros.put(new KeyMacro("Paste","KEYCODE_CTRL_LEFT/KEYCODE_V").toJSON());
                json.put("macros",macros);
                Utils.writeJSON(getContext(),Utils.MACRO_FILENAME,json);
            }else {
                Log.d(TAG,"Json loaded");
                macros = json.getJSONArray("macros");
            }
            for (int i = 0; i < macros.length(); i++) {
                JSONObject macro = macros.getJSONObject(i);
                int id = i;
                String name = macro.getString("name");
                String keycodes = macro.getString("keyCodes");

                addButton(id, name, keycodes);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        //esto estaba pensado para poder detectar la acción de pulsar y soltar el botón,
        //pero daba conflictos con el contextmenu que sirve para editar las macros.
        //se dejó quedar el argumento "press" del listener por si en el futuro se encuentra
        //una solución mejor
        mListener.onMacroClick((Button)v,1);
        mListener.onMacroClick((Button)v,0);
    }

    //OnTouchListener entra en conflicto con el contextMenu de los botones
   /*  @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }

   @Override
    public boolean onTouch(View v, MotionEvent event) {
        String[] tag = v.getTag().toString().split("/");
        for(String s:tag){
            Log.d(TAG,"key:"+s+"/code:"+ KeyEvent.keyCodeFromString(s));
        }
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                mListener.onMacroClick((Button)v,1);
                return true;
            case MotionEvent.ACTION_UP:
                mListener.onMacroClick((Button)v,0);
                return true;
            default:
                return false;
        }
    }*/
}

interface OnMacroClickedListener {
    public void onMacroEdit(Button b);
    public void onMacroClick(Button b,int press);
}

