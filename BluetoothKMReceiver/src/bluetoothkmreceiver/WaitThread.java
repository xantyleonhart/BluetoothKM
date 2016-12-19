/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bluetoothkmreceiver;

import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;

public class WaitThread implements Runnable{

    // Constant that indicate command from devices
    private static final int EXIT_CMD = -1;
    private static final int KEY_RIGHT = 1;
    private static final int KEY_LEFT = 2;

    /** Constructor */
    public WaitThread() {
    }

    @Override
    public void run() {
        waitForConnection();
    }

    /** Waiting for connection from devices */
    private void waitForConnection() {
        // retrieve the local Bluetooth device object
        LocalDevice local = null;

        StreamConnectionNotifier notifier;
        StreamConnection connection = null;

        // setup the server to listen for connection
        try {
            local = LocalDevice.getLocalDevice();
            //local.setDiscoverable(DiscoveryAgent.GIAC);

            UUID uuid = new UUID(80087355); // "04c6093b-0000-1000-8000-00805f9b34fb"
            String url = "btspp://localhost:" + uuid.toString() + ";name=RemoteBluetooth";
            notifier = (StreamConnectionNotifier)Connector.open(url);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
                // waiting for connection
        while(true) {
            System.out.println("hola holita");
            try {
                System.out.println("waiting for connection...");
                        connection = notifier.acceptAndOpen();
                        
                System.out.println(RemoteDevice.getRemoteDevice(connection).getBluetoothAddress());
                //Thread processThread = new Thread(new ProcessConnectionThread(connection));
                processThread(connection);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
    }
    
    private void processThread(StreamConnection connection){
        try {
            // prepare to receive data
            InputStream inputStream = connection.openInputStream();

            System.out.println("waiting for input");
//            int in=inputStream.read();
//            while (in!=-1) {
//                
//                System.out.print((char)in);
//                //processCommand(0);
//                in=inputStream.read();
//            }

           
            Robot robot = new Robot();
            long clickTimer =0;
            
            
            BufferedReader bufRead = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while((line = bufRead.readLine())!=null){
                System.out.println("line: "+line);
                String[] args = line.split("/");
                int action=0;
                try{
                     action = Integer.parseInt(args[0]);
                }catch (NumberFormatException e){
                    System.err.println("Error: "+args[0]+" is not a valid action");
                }
                
                
                switch (action){
                    case 100: //Mouse coord, move mouse
                        Point m_pos = MouseInfo.getPointerInfo().getLocation();
                        int X = (int)m_pos.getX();
                        int Y = (int)m_pos.getY();
                        int x = Integer.parseInt(args[1]);
                        int y = Integer.parseInt(args[2]);
                        X+=x;
                        Y+=y;
                        robot.mouseMove(X,Y);
                        break;
                    case 101:
                        int button = Integer.parseInt(args[1]);
                        int mask = 0;
                        switch (button){
                            case 0:
                                mask = InputEvent.BUTTON1_DOWN_MASK;
                                break;
                            case 1:
                                mask = InputEvent.BUTTON2_DOWN_MASK;
                                break;
                            case -1: //touchpad
                                mask = InputEvent.BUTTON1_DOWN_MASK;
                                break;
                        }
                        int press = Integer.parseInt(args[2]);
                        
                        if(press==1){//mose button DOWN
                            if(button==-1){//Special case / touchpad
                                long delay = System.currentTimeMillis();
                                if(delay-clickTimer<=300){
                                    robot.mousePress(mask);
                                }
                                clickTimer=delay;
                            }else{
                                robot.mousePress(mask);
                            }
                        }else{//mouse button UP
                            if(button==-1){//Special case / touchpad
                                long delay = System.currentTimeMillis();
                                if(delay-clickTimer<=100){
                                    robot.mousePress(mask);
                                }
                            }
                            robot.mouseRelease(mask);
                        }
                        
                        break;
                    case 102:
                        if(Integer.parseInt(args[2])==1){
                            robot.keyPress(Integer.parseInt(args[1]));    
                        }else{
                            robot.keyRelease(Integer.parseInt(args[1]));
                        }
                        
                        break;
                    default:
                        System.err.println("Error: received action code is invalid");
                        break;
                }
            }
            //conexión cortada
            System.out.println("Connection closed");
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
            connection=null;
        }
    }
    
}