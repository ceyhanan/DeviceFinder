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
    private byte[] receiveData;
    private boolean running = false;
    private final Thread thread;
    LinkedList<Device> deviceList;
    int activeDevice;

    public Broadcaster() throws IOException {
        receiveData = new byte[250];
        deviceList = new LinkedList<>();
        thread = new Thread(this);
        broadcast();
    }

    public void broadcast() throws IOException {
        byte[] sendData = new byte[13];
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
            sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("255.255.255.255"), 31500);
            serverSocket.send(sendPacket);
        } catch (UnknownHostException ex) {
            System.out.println("Error broadcasting: " + ex);
        }
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

    public void updateGUIList() throws UnknownHostException {
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

    public void setActiveDevice(int num) {
        String str;
        activeDevice = num;
        Device tmpdevice;

        try {
            tmpdevice = deviceList.get(activeDevice);

            str = String.format("%d", (int) tmpdevice.localparams.IP[0] & 0xFF);
            str += "." + String.format("%d", (int) tmpdevice.localparams.IP[1] & 0xFF);
            str += "." + String.format("%d", (int) tmpdevice.localparams.IP[2] & 0xFF);
            str += "." + String.format("%d", (int) tmpdevice.localparams.IP[3] & 0xFF);
            DeviceFinder.setIPText(str);

            str = String.format("%d", (int) tmpdevice.localparams.GatewayIP[0] & 0xFF);
            str += "." + String.format("%d", (int) tmpdevice.localparams.GatewayIP[1] & 0xFF);
            str += "." + String.format("%d", (int) tmpdevice.localparams.GatewayIP[2] & 0xFF);
            str += "." + String.format("%d", (int) tmpdevice.localparams.GatewayIP[3] & 0xFF);
            DeviceFinder.setGatewayText(str);

            str = String.format("%d", (int) tmpdevice.localparams.SubnetMask[0] & 0xFF);
            str += "." + String.format("%d", (int) tmpdevice.localparams.SubnetMask[1] & 0xFF);
            str += "." + String.format("%d", (int) tmpdevice.localparams.SubnetMask[2] & 0xFF);
            str += "." + String.format("%d", (int) tmpdevice.localparams.SubnetMask[3] & 0xFF);
            DeviceFinder.setSubnetText(str);

            str = String.format("%d", (int) tmpdevice.localparams.PrimaryDNS[0] & 0xFF);
            str += "." + String.format("%d", (int) tmpdevice.localparams.PrimaryDNS[1] & 0xFF);
            str += "." + String.format("%d", (int) tmpdevice.localparams.PrimaryDNS[2] & 0xFF);
            str += "." + String.format("%d", (int) tmpdevice.localparams.PrimaryDNS[3] & 0xFF);
            DeviceFinder.setDNS1Text(str);

            str = String.format("%d", (int) tmpdevice.localparams.SecondaryDNS[0] & 0xFF);
            str += "." + String.format("%d", (int) tmpdevice.localparams.SecondaryDNS[1] & 0xFF);
            str += "." + String.format("%d", (int) tmpdevice.localparams.SecondaryDNS[2] & 0xFF);
            str += "." + String.format("%d", (int) tmpdevice.localparams.SecondaryDNS[3] & 0xFF);
            DeviceFinder.setDNS2Text(str);

            str = String.format("%02X", (int) tmpdevice.localparams.MAC[0] & 0xFF);
            str += "-" + String.format("%02X", (int) tmpdevice.localparams.MAC[1] & 0xFF);
            str += "-" + String.format("%02X", (int) tmpdevice.localparams.MAC[2] & 0xFF);
            str += "-" + String.format("%02X", (int) tmpdevice.localparams.MAC[3] & 0xFF);
            str += "-" + String.format("%02X", (int) tmpdevice.localparams.MAC[4] & 0xFF);
            str += "-" + String.format("%02X", (int) tmpdevice.localparams.MAC[5] & 0xFF);
            DeviceFinder.setMACText(str);

            str = String.format("%d", tmpdevice.socketparams.HostPort);
            DeviceFinder.setPortText(str);

            str = String.format("%d", (int) tmpdevice.socketparams.RemoteIP[0] & 0xFF);
            str += "." + String.format("%d", (int) tmpdevice.socketparams.RemoteIP[1] & 0xFF);
            str += "." + String.format("%d", (int) tmpdevice.socketparams.RemoteIP[2] & 0xFF);
            str += "." + String.format("%d", (int) tmpdevice.socketparams.RemoteIP[3] & 0xFF);
            DeviceFinder.setRemoteIPText(str);

            str = String.format("%d", tmpdevice.socketparams.RemotePort);
            DeviceFinder.setRemotePortText(str);

            str = new String(tmpdevice.localparams.DeviceHostName);
            DeviceFinder.setHostnameText(str);
        } catch (Exception e) {
            System.out.println("setActiveDevice error: " + e);
        }
    }

    public void saveLocalParams2Device() {
        Device tmpdevice;

        try {
            tmpdevice = deviceList.get(activeDevice);

            tmpdevice.localparams.SubnetMask = InetAddress.getByName(DeviceFinder.getSubnetText()).getAddress();
            tmpdevice.localparams.GatewayIP = InetAddress.getByName(DeviceFinder.getGatewayText()).getAddress();
            tmpdevice.localparams.PrimaryDNS = InetAddress.getByName(DeviceFinder.getDNS1Text()).getAddress();
            tmpdevice.localparams.SecondaryDNS = InetAddress.getByName(DeviceFinder.getDNS2Text()).getAddress();

            tmpdevice.changeIP(InetAddress.getByName(DeviceFinder.getIPText()));
        } catch (Exception e) {
            System.out.println("saveLocalParams2Device error: " + e);
        }
    }

    public void saveSocketParams2Device() {
        Device tmpdevice;

        try {
            tmpdevice = deviceList.get(activeDevice);

            tmpdevice.socketparams.HostPort = Integer.parseUnsignedInt(DeviceFinder.getPortText());
            tmpdevice.socketparams.RemotePort = Integer.parseUnsignedInt(DeviceFinder.getRemotePortText());

            tmpdevice.changeRemoteIP(InetAddress.getByName(DeviceFinder.getRemoteIPText()));
        } catch (Exception e) {
            System.out.println("saveSocketParams2Device error: " + e);
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
                                updateGUIList();
                                break;
                            }
                        }
                    }
                    if (isInList == false) {
                        deviceList.add(device);
                        updateGUIList();
                    }
                }
            } catch (Exception ex) {
                System.out.println("Broadcast thread error: " + ex);
            }
        }
    }
}
