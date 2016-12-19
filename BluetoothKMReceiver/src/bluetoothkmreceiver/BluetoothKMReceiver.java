/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bluetoothkmreceiver;

import java.awt.Frame;



/**
 *
 * @author xanty
 */
public class BluetoothKMReceiver extends Frame{

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        Thread waitThread = new Thread(new WaitThread());
        waitThread.start();
    }
    
}
