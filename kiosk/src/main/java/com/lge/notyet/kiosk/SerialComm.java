package com.lge.notyet.kiosk;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

import java.util.Arrays;

/**
 * Created by sjun.lee on 2016-06-15.
 */
public class SerialComm {
    public interface MessageUpdatable {
        void onMessageUpdate(String message);
    }

    SerialPort comPort;
    MessageUpdatable updatable;

    public SerialComm(MessageUpdatable updatable) {
        this.updatable = updatable;
    }

    public void connect() {
        comPort = findPort("Arduino");

        if (comPort.isOpen()) {
            comPort.closePort();
            System.out.println("close comPort: " + comPort.getDescriptivePortName());
        }

        System.out.println("start open comPort: " + comPort.getDescriptivePortName());
        if (comPort.openPort()) {
            System.out.println("open success!!");
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
                if (updatable != null) {
                    updatable.onMessageUpdate(message);
                }
                System.out.print(message);
            }
        });
    }

    public void send(byte[] buffer) {
        System.out.println("try to write: " + Arrays.toString(buffer));

        int bytes = comPort.writeBytes(buffer, buffer.length);
        if (bytes < 0) {
            System.out.println("write 0 bytes");
            comPort.closePort();
            System.exit(0);
        }
    }

    public void disconnect() {
        System.out.println("disconnect called...");

        comPort.closePort();
        //serialCommMonitorThread.interrupt();
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

    public boolean isConnected() {
        return comPort.isOpen();
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
