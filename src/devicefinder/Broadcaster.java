/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package devicefinder;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.LinkedList;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Ceyhan ANKITÇI
 */
public class Broadcaster implements Runnable {

    private DatagramSocket serverSocket = null;
    private byte[] sendData;
    private byte[] receiveData;
    private boolean running = false;
    private Thread thread;
    LinkedList<Device> deviceList;

    public Broadcaster() throws IOException {
        receiveData = new byte[250];
        sendData = new byte[20];
        deviceList = new LinkedList<>();
        DatagramPacket sendPacket;

        sendData[0] = 0x2a;
        sendData[1] = 0x73;
        sendData[2] = 0x63;
        sendData[3] = 0x70;
        sendData[4] = 0x40;
        sendData[5] = 0x00;
        sendData[6] = 0x00;
        sendData[7] = 0x00;
        sendData[8] = 0x00;
        sendData[9] = 0x00;
        sendData[10] = 0x00;
        sendData[11] = 0x00;
        sendData[12] = (byte) 0xb0;

        System.out.println("Creating datagram socket...");
        try {
            serverSocket = new DatagramSocket();
        } catch (SocketException ex) {
            System.out.println("Error create datagram socket: " + ex);
        }

        System.out.println("Broadcasting to devices...");
        try {
            sendPacket = new DatagramPacket(sendData, 13, InetAddress.getByName("255.255.255.255"), 31500);
            serverSocket.send(sendPacket);
        } catch (UnknownHostException ex) {
            System.out.println("Error broadcasting: " + ex);
        }
        thread = new Thread(this);
    }

    public void start() {
        this.running = true;
        if (thread.isInterrupted() == true || thread.isAlive() == false) {
            this.thread.start();
        }
    }

    public void stop() {
        this.running = false;
        this.thread.interrupt();
        serverSocket.close();
    }

    private boolean checkPacket(DatagramPacket packet) {
        return packet.getLength() == 203;
    }

    public void updataGUIList() throws UnknownHostException {
        DefaultTableModel model = (DefaultTableModel) DeviceFinder.jDeviceList.getModel();

        model.setRowCount(0);
        for (int i = 0; i < deviceList.size(); i++) {
            model.addRow(new Object[]{new String(deviceList.get(i).localparams.DeviceHostName),
                InetAddress.getByAddress(deviceList.get(i).localparams.IP),
                deviceList.get(i).socketparams.HostPort,
                InetAddress.getByAddress(deviceList.get(i).socketparams.RemoteIP),
                deviceList.get(i).socketparams.RemotePort});
        }
    }

    @Override
    public void run() {
        boolean isInList;
        int i;

        while (running) {
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            isInList = false;

            try {
                serverSocket.receive(receivePacket);
                if (checkPacket(receivePacket)) {
                    Device device = new Device(receivePacket);
                    if (deviceList.size() > 0) {
                        for (i = 0; i < deviceList.size(); i++) {
                            if (Arrays.equals(deviceList.get(i).localparams.MAC, device.localparams.MAC)) {
                                deviceList.set(i, device);
                                isInList = true;
                                updataGUIList();
                                break;
                            }
                        }
                    }
                    if (isInList == false) {
                        deviceList.add(device);
                        updataGUIList();
                    }
                }
            } catch (IOException ex) {
                System.out.println("Broadcast thread error: " + ex);
            }
        }
    }
}
