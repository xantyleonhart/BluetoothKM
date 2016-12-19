package es.udc.ps1617.bluetoothkm;

import android.inputmethodservice.Keyboard;
import android.view.KeyEvent;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by xanty on 1/11/16.
 */
public abstract class Utils {

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final int MESSAGE_CONNECTED = 6;

    public static final int MOUSE_MOVE = 100;
    public static final int MOUSE_CLICK = 101;
    public static final int KEY_PRESS = 102;



    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";



    public static int keytoJava(int keyCode){
        if (keyCode>= KeyEvent.KEYCODE_A && keyCode<=KeyEvent.KEYCODE_Z){
            return keyCode+36;
        }

        if (keyCode>= KeyEvent.KEYCODE_0 && keyCode<=KeyEvent.KEYCODE_9){
            return keyCode+41;
        }

        switch (keyCode){
            case KeyEvent.KEYCODE_DEL:
                return 8;
            default:
                return keyCode;
        }
    }



    public static float mean(Collection<Float> m) {
        float sum = 0;
        for (float d :m) {
                sum += d;
        }
        return sum/m.size();
    }

    public static float variance(Collection<Float> m)
    {
        float mean = mean(m);
        float sum = 0;
        for(float a :m)
            sum += (a-mean)*(a-mean);
        return sum/m.size();
    }

    public static Collection<Float> l_abs(Collection<Float> m)
    {   Collection<Float> abs = new ArrayList<Float>();
        for(float a :m)
            abs.add(Math.abs(a));
        return abs;
    }

    public static float sum(Collection<Float> m) {
        float sum = 0;
        for (float d :m) {
            sum += d;
        }
        return sum;
    }
}
