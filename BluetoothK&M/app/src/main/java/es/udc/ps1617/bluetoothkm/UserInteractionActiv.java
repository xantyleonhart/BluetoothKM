package es.udc.ps1617.bluetoothkm;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

public class UserInteractionActiv extends AppCompatActivity implements View.OnTouchListener, View.OnClickListener{
    BluetoothService bt_serv;
    BluetoothService.SimpleBinder btBinder=null;
    MouseSensorService ms_serv;
    MouseSensorService.SimpleBinder msBinder=null;

    Button but_lmb;
    Button but_rmb;
    ImageButton but_key;
    TouchpadView touchpad;

    float X;
    float Y;

    private final Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case Utils.MESSAGE_TOAST:
                    String toast_text = msg.getData().getString(Utils.TOAST);
                    Toast.makeText(getBaseContext(),toast_text, Toast.LENGTH_SHORT).show();
                    break;
                case Utils.MESSAGE_WRITE:
                    String message = (String)msg.obj;
                    bt_serv.sendMessage(message);
                    break;
                default:
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_interaction);

        //bind BluetoothService
        attachBluetooth();

        //bind accelerometer MouseSensorService (experimental)
        //attachSensor();

        but_lmb=(Button)findViewById(R.id.lm_but);
        but_lmb.setOnTouchListener(this);
        but_rmb=(Button)findViewById(R.id.rm_but);
        but_rmb.setOnTouchListener(this);

        but_key=(ImageButton)findViewById(R.id.keyboard_but);
        but_key.setOnClickListener(this);

        touchpad=(TouchpadView)findViewById(R.id.touchpad);
        touchpad.setOnClickListener(this);
        touchpad.setOnTouchListener(this);
    }


    @Override
    public void onClick(View v) {
        if(v==but_key){//SHOW KEYBOARD
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
        }
    }

    @Override
    protected void onDestroy() {
        bt_serv.stop();
        ms_serv.stop();
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        if(btBinder!=null){detachBluetooth();}
        if(msBinder!=null){detachSensor();}
        super.onStop();
    }

    private ServiceConnection btConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder bind) {
            btBinder = (BluetoothService.SimpleBinder) bind;
            bt_serv = btBinder.getService(mHandler);

            bt_serv.sendMessage("Hola holita bluetooth\n");
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            btBinder = null;
        }
    };

    private ServiceConnection msConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder bind) {
            msBinder = (MouseSensorService.SimpleBinder) bind;
            ms_serv = msBinder.getService(mHandler);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            msBinder = null;
        }
    };


    private void attachBluetooth() {
        Intent service = new Intent(this, BluetoothService.class);
        bindService(service, btConn, Service.BIND_AUTO_CREATE);
    }

    private void detachBluetooth() {
        unbindService(btConn); btBinder = null;
    }

    private void attachSensor() {
        Intent service = new Intent(this, MouseSensorService.class);
        bindService(service, msConn, Service.BIND_AUTO_CREATE);
    }

    private void detachSensor() {
        unbindService(msConn); msBinder = null;
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN: //MOUSE CLICK
                String msg = Utils.MOUSE_CLICK+"";

                if(v==but_lmb){//LEFT CLICK
                    msg+="/"+0;
                }else if(v==but_rmb){//RIGHT CLICK
                    msg +="/"+1;
                }else if(v==touchpad){
                    //LEFT CLICK / RELEASE
                    msg+="/"+(-1);
                    //establecer posición del ratón
                    X = event.getX();
                    Y = event.getY();
                }
                bt_serv.sendMessage(msg+"/"+1+"\n");
                break;

            case MotionEvent.ACTION_MOVE:
                if(v==touchpad) {
                    //medir el desplazamiento
                    int despl_x = Math.round(event.getX() - X);
                    int despl_y = Math.round(event.getY() - Y);
                    //enviar orden de movimiento
                    bt_serv.sendMessage(Utils.MOUSE_MOVE + "/" + despl_x + "/" + despl_y + "\n");
                    //actualizar posición del ratón
                    X = event.getX();
                    Y = event.getY();
                }
                break;

            case MotionEvent.ACTION_UP://MOUSE RELEASE
                msg = Utils.MOUSE_CLICK+"";

                if(v==but_lmb){//LEFT CLICK release
                    msg+="/"+0;
                }else if(v==but_rmb){//RIGHT CLICK release
                    msg +="/"+1;
                }else if(v==touchpad){
                    msg+="/"+(-1);
                }
                bt_serv.sendMessage(msg+"/"+0+"\n");
                break;

        }


        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_SHIFT_LEFT||keyCode == KeyEvent.KEYCODE_SHIFT_RIGHT)
        {
            Log.d("KEY","SHIFT activated"+keyCode);
        }else {
            Log.d("KEY", KeyEvent.keyCodeToString(keyCode) + ": " + keyCode);
            bt_serv.sendMessage(Utils.KEY_PRESS + "/" + Utils.keytoJava(keyCode) + "/" + 1 + "\n");
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Log.d("KEY","B"+" "+keyCode);
        bt_serv.sendMessage(Utils.KEY_PRESS+"/"+ Utils.keytoJava(keyCode)+"/"+0+"\n");
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
        return super.onKeyMultiple(keyCode, repeatCount, event);
    }



}
