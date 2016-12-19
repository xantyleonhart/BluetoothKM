package es.udc.ps1617.bluetoothkm;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class MouseSensorService extends Service implements SensorEventListener{
    public MouseSensorService() {
    }

    final String TAG = "MouseSensor";

    private final IBinder sBinder=(IBinder) new SimpleBinder();
    private Handler mHandler;

    SensorManager mSensorManager;
    Sensor mSensor;
    Sensor gSensor;

    //variables used to calibrate the "alpha" value of the sensor
    long timestamp;
    //convert from ns a s
    static final float NS2S = 1.0f / 1000000000.0f;

    float[] velocity = null;
    float[] position = null;

    final int values_memory= 10;
    int threshold = 250;
    List<Float> x_values;
    List<Float> y_values;
    List<Float> z_values;
    List<Float> err_values;
    //mouse speed
    final int ms_speed = 10000;

    private boolean stable;

    @Override
    public void onCreate() {
        super.onCreate();
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mSensorManager.registerListener(this,mSensor,SensorManager.SENSOR_DELAY_FASTEST);
        timestamp = 0;

        x_values = new ArrayList<>(0);
        y_values = new ArrayList<>(0);
        z_values = new ArrayList<>(0);
        err_values=new ArrayList<>();

        stable = false;
    }

    //Esta implementación del servicio con Binder permite reutilizarlo para otras actividades si
    //se decide ampliar la aplicación en el futuro
    @Override
    public IBinder onBind(Intent intent) {

        Log.d(TAG,"service bound");
        return sBinder;
    }


    public class SimpleBinder extends Binder {
        MouseSensorService getService(Handler handler) {
            //asigna el handler de la actividad que se conecta al servicio
            MouseSensorService.this.mHandler=handler;
            return MouseSensorService.this;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) {
    }


    private int count=0;
    private double error =0;
    @Override
    public void onSensorChanged(SensorEvent event) {
        //if not calibrated
        if(velocity==null||position==null){
            //set time
            timestamp=event.timestamp;
            velocity = new float[3];
            position = new float[3];
            velocity[0] = velocity[1] = velocity[2] = 0f;
            return;
        }


        position[0] = position[1] = position[2] = 0f;

        //event.values[0]=Math.signum(event.values[0])*(Math.abs(event.values[0])-(float)error);

        if(x_values.size()>=values_memory){
            x_values.remove(0);
        }
        x_values.add(event.values[0]);

        if(y_values.size()>=values_memory){
            y_values.remove(0);
        }
        y_values.add(event.values[1]);

        if(z_values.size()>=values_memory){
            z_values.remove(0);
        }
        z_values.add(event.values[2]);
        //double movement = Utils.mean(z_values);
        float dt = (event.timestamp - timestamp) * NS2S;

        velocity[0] += (event.values[0] + x_values.get(x_values.size()-1))/2 * dt;
        velocity[1] -= (event.values[1] + y_values.get(y_values.size()-1))/2 * dt;

        double devX = Math.sqrt((double)Utils.variance(Utils.l_abs(x_values)));
        double devY = Math.sqrt((double)Utils.variance(Utils.l_abs(y_values)));
        double devZ = Math.sqrt((double)Utils.variance(z_values));
        double thr = mSensor.getResolution()*threshold;
        if(devX<thr) {
            velocity[0] = 0f;
            x_values.clear();
            x_values.add(0f);
            Log.d("ZVC","Stopped");
        }
        if(devY<thr) {
            velocity[1] = 0f;
            y_values.clear();
            y_values.add(0f);
            //Log.d("ZVC","Stopped");
        }

        Log.d("Accelerometer","velX: "+velocity[0]+"velY: "+velocity[1]);
        Log.d("Accelerometer","sDeviation: "+devX+" ,Threshold: "+thr);

        position[0] = velocity[0] * dt * ms_speed;
        position[1] = velocity[1] * dt * ms_speed;

        if(devZ<thr){
            Log.d("Accelerometer","Stable: "+position[0]+","+position[1]);


            if(Math.abs(position[0])>=1||Math.abs(position[1])>=1) {
                String msg= Utils.MOUSE_MOVE+"/"+(int)position[0]+"/"+(int)position[1]+"\n";
                Log.d("MS_Service",msg);
                mHandler.obtainMessage(Utils.MESSAGE_WRITE,msg).sendToTarget();
            }
        }
        timestamp=event.timestamp;
    }

    public void stop(){
        mSensorManager.unregisterListener(this);
    }
}
