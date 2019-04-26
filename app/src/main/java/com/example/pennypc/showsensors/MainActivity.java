package com.example.pennypc.showsensors;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.hardware.usb.*;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.*;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


    }
    UsbDevice dev;

    public static void l(String s){
        System.out.print(s);
    }
    public static void l(StringBuilder s){
        System.out.print(s.toString());
    }

    public void sendMessage(View view) {
        // Do something in response to button
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);

        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();

        TextView textView =(TextView) findViewById(R.id.textView);
        textView.setText("");
        if(deviceIterator.hasNext()) {
            dev = deviceIterator.next();
            textView.append(dev.getSerialNumber()+"\n");

            Thread thread = new Thread(new Runnable() {
                public void run() {






                     Runnable mLoop = new Runnable() {

                        @Override
                        public void run() {
                            if (dev == null)
                                return;
                            UsbManager usbm = (UsbManager) getSystemService(USB_SERVICE);
                            UsbDeviceConnection conn = usbm.openDevice(dev);
                            l("Interface Count: " + dev.getInterfaceCount());
                            l("Using "
                                    + String.format("%04X:%04X", dev.getVendorId(),
                                    dev.getProductId()));

                            if (!conn.claimInterface(dev.getInterface(0), true))
                                return;

                            conn.controlTransfer(0x40, 0, 0, 0, null, 0, 0);// reset
                            // mConnection.controlTransfer(0×40,
                            // 0, 1, 0, null, 0,
                            // 0);//clear Rx
                            conn.controlTransfer(0x40, 0, 2, 0, null, 0, 0);// clear Tx
                            conn.controlTransfer(0x40, 0x02, 0x0000, 0, null, 0, 0);// flow
                            // control
                            // none
                            conn.controlTransfer(0x40, 0x03, 0x001a, 0, null, 0, 0);// baudrate
                            // 57600
                            conn.controlTransfer(0x40, 0x04, 0x0008, 0, null, 0, 0);// data bit
                            // 8, parity
                            // none,
                            // stop bit
                            // 1, tx off

                            UsbEndpoint epIN = null;
                            UsbEndpoint epOUT = null;

                            byte counter = 0;

                            UsbInterface usbIf = dev.getInterface(0);
                            for (int i = 0; i < usbIf.getEndpointCount(); i++) {
                                l("EP: "
                                        + String.format("0x%02X", usbIf.getEndpoint(i)
                                        .getAddress()));
                                if (usbIf.getEndpoint(i).getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                                    l("Bulk Endpoint");
                                    if (usbIf.getEndpoint(i).getDirection() == UsbConstants.USB_DIR_IN)
                                        epIN = usbIf.getEndpoint(i);
                                    else
                                        epOUT = usbIf.getEndpoint(i);
                                } else {
                                    l("Not Bulk");
                                }
                            }
                            EditText editText=(EditText) findViewById(R.id.editText);

                            for (;;) {// this is the main loop for transferring
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                                String get = "$fDump G" + "\n";
                                l("Sending: " + get);

                                byte[] by = get.getBytes();

                                // This is where it sends
                                l("out " + conn.bulkTransfer(epOUT, by, by.length, 500));

                                // This is where it is meant to receive
                                byte[] buffer = new byte[4096];

                                StringBuilder str = new StringBuilder();

                                if (conn.bulkTransfer(epIN, buffer, 4096, 500) >= 0) {
                                    for (int i = 2; i < 4096; i++) {
                                        editText.append(String.format("%8s", Integer.toBinaryString(buffer[i] & 0xFF)).replace(' ', '0'));
                                        if (buffer[i] != 0) {
                                            str.append((char) buffer[i]);

                                        } else {
                                            l(str);
                                            break;
                                        }
                                    }

                                }

                                // this shows the complete string
                                l(str);
                                /*
                                if (mStop) {
                                    mStopped = true;
                                    return;
                                }
                                */
                                l("sent " + counter);
                                counter++;
                                counter = (byte) (counter % 16);
                            }
                        }
                    };










                }
            });
            thread.start();

        }




    }



}
