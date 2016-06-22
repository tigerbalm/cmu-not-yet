package com.lge.notyet.kiosk.comm;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

/**
 * Created by sjun.lee on 2016-06-21.
 */
public class SerialComm2 implements CommApi {
    SerialPort comPort;
    StatusListener listener;

    StringBuilder systemMessage = new StringBuilder();
    boolean systemMessageReceiving = false;

    public SerialComm2(StatusListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean connect() {
        systemMessageReceiving = false;

        comPort = findPort("Arduino");

        if (comPort.isOpen()) {
            comPort.closePort();
            System.out.println("close comPort: " + comPort.getDescriptivePortName());
        }

        comPort.setBaudRate(57600);

        System.out.println("start open comPort: " + comPort.getDescriptivePortName());
        if (comPort.openPort()) {
            System.out.println("open success!!");
        }

        if (listener != null) {
            listener.onConnected();
        }

        comPort.addDataListener(new SerialPortDataListener() {
            @Override
            public int getListeningEvents() {
                return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
            }

            @Override
            public void serialEvent(SerialPortEvent event)
            {
                if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE)
                    return;


                byte[] data = new byte[comPort.bytesAvailable()];

                comPort.readBytes(data, data.length);

                String message = new String(data);
                if (listener != null) {
                    listener.onMessageReceived(message);
                }
                //System.out.println(message);

                if (systemMessageReceiving || message.contains("#$")) { // start system message
                    systemMessageReceiving = true;

                    int beginIndex = 0;
                    int endIndex = message.length();

                    //System.out.println("message: " + message);
                    System.out.println(systemMessageReceiving + ")systemMessage: " + systemMessage);
                    if (message.contains("$#")) {   // end
                        System.out.println("message: " + message);
                        endIndex = message.indexOf("$#");

                        System.out.println("begin: " + beginIndex + ", end: " + endIndex);

                        systemMessage.append(message.substring(beginIndex, endIndex));

                        System.out.println("systemMessage: " + systemMessage);

                        systemMessageReceiving = false;

                        if (listener != null) {
                            listener.onSystemMessageReceived(systemMessage.toString());
                        }

                        systemMessage.setLength(0);
                    } else {
                        beginIndex = message.contains("#$")? message.indexOf("#$") + 2 : 0;
                        systemMessage.append(message.substring(beginIndex, endIndex));
                    }
                }
            }
        });

        return true;
    }

    @Override
    public boolean disconnect() {
        System.out.println("disconnect called...");

        comPort.closePort();

        if (listener != null) {
            listener.onDisconnected();
        }

        return !connected();
    }

    @Override
    public int send(String string) {
        System.out.println("Send: " + string);

        if (!connected()) {
            return 0;
        }

        int bytes = comPort.writeBytes(string.getBytes(), string.getBytes().length);
        if (bytes < 0) {
            System.out.println("write 0 bytes");
            comPort.closePort();
        }

        return 0;
    }

    @Override
    public boolean connected() {
        return comPort != null && comPort.isOpen();
    }

    private SerialPort findPort(final String descriptor) {
        SerialPort[] ports = SerialPort.getCommPorts();

        for (SerialPort port: ports) {
            if (port.getDescriptivePortName().contains(descriptor)) {
                return port;
            }
        }

        return SerialPort.getCommPorts()[0];
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        SerialPort port = comPort;
        if (port == null) {
            port = findPort("Arduino");
        }

        builder.append(port.getSystemPortName())
                .append("(")
                .append(port.getBaudRate())
                .append(") ");

        if (port.isOpen()) {
            builder.append("connected.");
        } else {
            builder.append("disconnected.");
        }

        return builder.toString();
    }
}
