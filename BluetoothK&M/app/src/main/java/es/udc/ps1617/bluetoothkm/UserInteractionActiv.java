package es.udc.ps1617.bluetoothkm;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.ToggleButton;

public class UserInteractionActiv extends AppCompatActivity implements View.OnTouchListener,OnMacroClickedListener{
    private static final int NUM_ITEMS = 2;

    BluetoothService bt_serv;
    BluetoothService.SimpleBinder btBinder=null;
    MouseSensorService ms_serv;
    MouseSensorService.SimpleBinder msBinder=null;

    Button but_lmb;
    Button but_rmb;
    ImageButton but_key;
    TouchpadView touchpad;
    TouchpadView wheel;
    SeekBar mouse_sens;
    SeekBar wheel_sens;
    ToggleButton tog_Accel;
    ToggleButton tog_Gyro;

    ViewPager mPager;
    MyAdapter mAdapter;

    float X;
    float Y;

    float scroll_Y;

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
                    switch(msg.arg1){
                        case Utils.SENSOR_ACCEL:
                                if(!tog_Accel.isChecked()){//si el acelerometro está desactivado
                                    message=null;//cancela el mensaje
                                }
                            break;
                        case Utils.SENSOR_GYRO :
                            if(!tog_Gyro.isChecked()){//si el giroscopio está desactivado
                                message=null;//cancela el mensaje
                                //Log.d("MESSAGE","GYRO OFF");
                            }
                            break;
                        default:
                            break;
                    }
                    if (message!=null) {
                        bt_serv.sendMessage(message);
                    }
                    break;
                case Utils.MESSAGE_DISCONNECTED:
                    Toast.makeText(getBaseContext(),"Connection lost", Toast.LENGTH_SHORT).show();
                    finish();
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
        attachSensor();

        but_lmb=(Button)findViewById(R.id.lm_but);
        but_lmb.setOnTouchListener(this);
        but_rmb=(Button)findViewById(R.id.rm_but);
        but_rmb.setOnTouchListener(this);
        wheel = (TouchpadView)findViewById(R.id.wheel);
        wheel.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN: //TOUCH MOUSEWHEEL
                        scroll_Y=event.getY();
                        break;
                    case MotionEvent.ACTION_MOVE: //SCROLL
                        int move = Math.round(event.getY()-scroll_Y);
                        int sensitivity = Math.round(wheel_sens.getProgress()/100f);
                        bt_serv.sendMessage(Utils.SCROLL+"/"+move*sensitivity+"\n");

                        scroll_Y= event.getY();
                        break;
                }
                return false;
            }
        });




        touchpad=(TouchpadView)findViewById(R.id.touchpad);
        touchpad.setOnTouchListener(this);

        mouse_sens = (SeekBar)findViewById(R.id.mouse_sens_bar);
        mouse_sens.setMax(200);
        mouse_sens.setProgress(100);
        wheel_sens = (SeekBar)findViewById(R.id.scroll_sens_bar);
        wheel_sens.setMax(50);
        wheel_sens.setProgress(50);

        tog_Accel = (ToggleButton)findViewById(R.id.toggle_accel);
        tog_Gyro = (ToggleButton)findViewById(R.id.toggle_gyro);

        mAdapter = new MyAdapter(getSupportFragmentManager());
        mPager = (ViewPager)findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);
    }

    @Override
    protected void onDestroy() {
        bt_serv.stop();
        if (ms_serv!=null){
            ms_serv.stop();
        }
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

            bt_serv.sendMessage("Test Message\n");
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
                    int sensitivity = Math.round(mouse_sens.getProgress()/100f);
                    //enviar orden de movimiento
                    bt_serv.sendMessage(Utils.MOUSE_MOVE + "/" + despl_x*sensitivity + "/"
                                                               + despl_y*sensitivity + "\n");
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
        }
            Log.d("KEY", KeyEvent.keyCodeToString(keyCode) + ": " + keyCode);
            bt_serv.sendMessage(Utils.KEY_PRESS + "/" + Utils.keytoJava(keyCode) + "/" + 1 + "\n");

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


    //MACRO FRAGMENT LISTENER
    @Override
    public void onMacroEdit(Button b) {
        //Not used
    }

    @Override
    public void onMacroClick(Button b,int pressed) {
        String [] args = b.getTag().toString().split("/");

        for(String s:args){
            bt_serv.sendMessage(Utils.KEY_PRESS+"/"+Utils.keytoJava(KeyEvent.keyCodeFromString(s))+"/"+pressed+"\n");
        }

    }


    public static class MyAdapter extends FragmentPagerAdapter {

        public MyAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new KeyboardFragment();
                case 1:
                    return new MacroFragm();
                default:
                    return null;
            }
        }
    }

}
