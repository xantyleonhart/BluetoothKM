package es.udc.ps1617.bluetoothkm;

import android.app.Activity;
import android.content.Context;
import android.inputmethodservice.Keyboard;
import android.util.Log;
import android.view.KeyEvent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
    public static final int MESSAGE_DISCONNECTED = 7;

    public static final int MOUSE_MOVE = 100;
    public static final int MOUSE_CLICK = 101;
    public static final int KEY_PRESS = 102;
    public static final int SCROLL = 103;


    public static final int SENSOR_ACCEL = 200;
    public static final int SENSOR_GYRO = 201;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    public static final String MACRO_FILENAME = "macros.json";


    //TRADUCTOR DE KEYCODES DE ANDROID A JAVA

    public static int keytoJava(int keyCode){
        //LETRAS
        if (keyCode>= KeyEvent.KEYCODE_A && keyCode<=KeyEvent.KEYCODE_Z){
            return keyCode+36;
        }

        //NUMEROS
        if (keyCode>= KeyEvent.KEYCODE_0 && keyCode<=KeyEvent.KEYCODE_9){
            return keyCode+41;
        }

        //TECLAS DE FUNCION
        if (keyCode>= KeyEvent.KEYCODE_F1 && keyCode<=KeyEvent.KEYCODE_F12){
            return keyCode-19;
        }


        switch (keyCode){
            case KeyEvent.KEYCODE_DEL:
                return 8;
            case KeyEvent.KEYCODE_SPACE:
                return 32;
            case KeyEvent.KEYCODE_ENTER:
                return 10;
            case KeyEvent.KEYCODE_SHIFT_LEFT:
                return 16;
            case KeyEvent.KEYCODE_SHIFT_RIGHT:
                return 20;//CAPS LOCK
            case KeyEvent.KEYCODE_COMMA:
                return 44;
            case 56://DOT
                return 110;
            case KeyEvent.KEYCODE_ALT_LEFT:
                return 18;
            case KeyEvent.KEYCODE_ALT_RIGHT:
                return 65406;
            case KeyEvent.KEYCODE_CTRL_LEFT:
                return 17;
            case KeyEvent.KEYCODE_CTRL_RIGHT:
                return 17;
            case KeyEvent.KEYCODE_WINDOW://window no es la tecla windows pero la usamos en el sitio
                return 524;
            default:
                return keyCode;
        }
    }

    //MATH OPERATIONS OVER LISTS

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


    //FUNCIONES PARA MANEJAR LECTURA/ESCRITURA DE JSON

    //lee archivo JSON desde memoria interna
    public static JSONObject loadJSON(Context context,String filename) throws JSONException {
        String json = null;
        try {
            FileInputStream is = context.openFileInput(filename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        //Debug
        Log.d("JSON",json);
        return new JSONObject(json);
    }

    //escribe archivo JSON a memoria interna
    public static void writeJSON(Context context,String filename,JSONObject obj) throws JSONException {
        String json = obj.toString();
        //Debug
        Log.d("JSON","Write: " + json);
        try {
            FileOutputStream os = context.openFileOutput(filename,Context.MODE_PRIVATE);
            os.write(json.getBytes());
            os.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    //La función de borrado
    //Nesario implementar, pues JSONArray.remove() solo está disponible a partir de la API 19
    public static JSONArray JSONremove(final int index, final JSONArray from) {
        final List<JSONObject> objs = getList(from);
        objs.remove(index);

        final JSONArray jarray = new JSONArray();
        for (final JSONObject obj : objs) {
            jarray.put(obj);
        }

        return jarray;
    }

    private static List<JSONObject> getList(final JSONArray jarray) {
        final int len = jarray.length();
        final ArrayList<JSONObject> result = new ArrayList<JSONObject>(len);
        for (int i = 0; i < len; i++) {
            final JSONObject obj = jarray.optJSONObject(i);
            if (obj != null) {
                result.add(obj);
            }
        }
        return result;
    }


}
