package es.udc.ps1617.bluetoothkm;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

//View personalizada para capturar los movimientos del dedo sobre el area del touchpad
public class TouchpadView extends View {
    public TouchpadView(Context context){
        super(context);
    }
    public TouchpadView(Context context, AttributeSet attrs){
        super(context,attrs);
    }
    public TouchpadView(Context context, AttributeSet attrs, int defStyle){
        super(context, attrs, defStyle);
    }

    //por defecto, view solo devuelve los eventos ACTION_DOWN
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();;
        switch (action) {
            case (MotionEvent.ACTION_DOWN):
                return true;
            case (MotionEvent.ACTION_MOVE):
                return true;
            case (MotionEvent.ACTION_UP):
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }
}
