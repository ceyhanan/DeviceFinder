/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package devicefinder;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 *
 * @author Ceyhan ANKITÇI
 */
public class Device {

    String hostName;
    InetAddress device_ip;
    InetAddress remote_ip;
    int device_port;
    int remote_port;
    byte[] mac;
    private DatagramSocket socket = null;
    private DatagramPacket packet;
    private byte[] sendData;

    public Device(String hostName, InetAddress device_ip, InetAddress remote_ip, int device_port, int remote_port, byte[] mac) {
        this.hostName = hostName;
        this.device_ip = device_ip;
        this.remote_ip = remote_ip;
        this.device_port = device_port;
        this.remote_port = remote_port;
        this.mac = mac;
    }

    public Device(Device device) {
        this.hostName = device.hostName;
        this.device_ip = device.device_ip;
        this.remote_ip = device.remote_ip;
        this.device_port = device.device_port;
        this.remote_port = device.remote_port;
        this.mac = device.mac;
    }

    public void setIP(InetAddress ip) {
        this.device_ip = ip;
        
        sendData = new byte[95];
    }
}
