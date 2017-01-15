package es.udc.ps1617.bluetoothkm;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class DeviceSelectActiv extends AppCompatActivity {

    ListView dev_list;
    BluetoothService bt_serv;
    BluetoothService.SimpleBinder sBinder=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toast.makeText(this,"onCreate",Toast.LENGTH_SHORT).show();

        //conecta el servicio de Bluetooth
        attachService();

        dev_list=(ListView)findViewById(R.id.dev_list);
        dev_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getBaseContext(),"dev: "+position,Toast.LENGTH_SHORT).show();
                bt_serv.connect(position);
            }
        });
    }


    private final Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case Utils.MESSAGE_DEVICE_NAME:
                    ArrayList<String> devList = msg.getData().getStringArrayList("names");
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(getBaseContext(),
                                                        android.R.layout.simple_list_item_1,devList);
                    dev_list.setAdapter(adapter);
                    break;
                case Utils.MESSAGE_TOAST:
                    String toast_text = msg.getData().getString(Utils.TOAST);
                    Toast.makeText(getBaseContext(),toast_text, Toast.LENGTH_SHORT).show();
                    break;
                case Utils.MESSAGE_CONNECTED:
                    Toast.makeText(getBaseContext(),"Connection established",Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getBaseContext(),UserInteractionActiv.class);
                    startActivity(intent);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onDestroy() {
        bt_serv.stop();
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        Toast.makeText(this,"stop",Toast.LENGTH_SHORT).show();
        if(sBinder!=null){detachService();}
        super.onStop();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Toast.makeText(this,"restart",Toast.LENGTH_SHORT).show();
        attachService();
    }


    private ServiceConnection mConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder bind) {
            sBinder = (BluetoothService.SimpleBinder) bind;
            bt_serv = sBinder.getService(mHandler);
            //Cuando el servicio esté conectado, cargar la lista de dispositivos
            bt_serv.getDevList();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            sBinder = null;
        }
    };


    private void attachService() {
        Intent service = new Intent(this, BluetoothService.class);
        bindService(service, mConn, Service.BIND_AUTO_CREATE);
    }

    private void detachService() {
        unbindService(mConn); sBinder = null;
    }


}
