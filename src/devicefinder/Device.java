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

/**
 *
 * @author Ceyhan ANKITÇI
 */
public class Device {

    /*
    String hostName;
    InetAddress device_ip;
    InetAddress remote_ip;
    int device_port;
    int remote_port;
    byte[] mac;
     */
    private DatagramSocket socket = null;
    private DatagramPacket packet;
    private byte[] sendData;
    private byte[] SmartPackTxFrame;
    public LocalParameters localparams;
    public SocketParameters socketparams;

    public final int SmartCommand_WHOIS = 0x0000;
    public final int SmartCommand_GET_SERIALTOETHERNETCONFIG = 0x0005;
    public final int SmartCommand_GET_SECONDSCOUNTER = 0x0101;
    public final int SmartCommand_UART_ERROR_STATUS = 0x0103;
    public final int SmartCommand_F_SETLOCAL = 0xFF01;
    public final int SmartCommand_F_SETSOCKETS = 0xFF03;
    public final int SmartCommand_F_SETSERIALTOETHERNETCONFIG = 0xFF05;
    public final int SmartCommand_F_SETHARDWARECONFIG = 0xFF07;

    public class LocalParameters {

        public byte[] MAC = new byte[6];
        public byte[] DeviceHostName = new byte[16];
        public byte[] IP = new byte[4];
        public byte[] GatewayIP = new byte[4];
        public byte[] SubnetMask = new byte[4];
        public byte[] PrimaryDNS = new byte[4];
        public byte[] SecondaryDNS = new byte[4];
        public byte DHCP;
        public byte Rsv1;
        public byte Rsv2;
        public byte Rsv3;

        private byte[] _SmartPackCommandParameters = new byte[40];
    }

    public class SocketParameters {

        public byte ServerConfig;
        public byte ClientConfig;
        public byte Rsv1;
        public byte Rsv2;
        public int HostPort;
        public int RemotePort;
        public byte[] RemoteIP = new byte[4];

        public byte[] _SmartPackCommandParameters = new byte[12];
    }

    public Device(byte[] hostName, InetAddress device_ip, InetAddress remote_ip, int device_port, int remote_port, byte[] mac) {
        localparams = new LocalParameters();
        socketparams = new SocketParameters();

        localparams.DeviceHostName = hostName;
        localparams.IP = device_ip.getAddress();
        localparams.MAC = mac;
        socketparams.RemoteIP = remote_ip.getAddress();
        socketparams.HostPort = device_port;
        socketparams.RemotePort = remote_port;
    }

    public Device(DatagramPacket whoisPacket) throws UnknownHostException {
        localparams = new LocalParameters();
        socketparams = new SocketParameters();
        
        localparams.MAC = Arrays.copyOfRange(whoisPacket.getData(), 10, 16);
        localparams.DeviceHostName = Arrays.copyOfRange(whoisPacket.getData(), 26, 42);
        localparams.IP = Arrays.copyOfRange(whoisPacket.getData(), 78, 82);
        localparams.GatewayIP = Arrays.copyOfRange(whoisPacket.getData(), 82, 86);
        localparams.SubnetMask = Arrays.copyOfRange(whoisPacket.getData(), 86, 90);
        localparams.PrimaryDNS = Arrays.copyOfRange(whoisPacket.getData(), 90, 94);
        localparams.SecondaryDNS = Arrays.copyOfRange(whoisPacket.getData(), 94, 98);
        localparams.DHCP = whoisPacket.getData()[62];
        localparams.Rsv1 = 0;
        localparams.Rsv2 = 0;
        localparams.Rsv3 = 0;
        
        socketparams.ServerConfig = whoisPacket.getData()[66];
        socketparams.ClientConfig = whoisPacket.getData()[67];
        socketparams.Rsv1 = 0;
        socketparams.Rsv2 = 0;
        socketparams.HostPort = (whoisPacket.getData()[102] & 0xff) | ((whoisPacket.getData()[103] & 0xff) << 8);
        socketparams.RemotePort = (whoisPacket.getData()[104] & 0xff) | ((whoisPacket.getData()[105] & 0xff) << 8);
        socketparams.RemoteIP = Arrays.copyOfRange(whoisPacket.getData(), 106, 110);
    }
    
    private void sendCommand(byte[] payload, int command){
        byte[] SmartPackHeader = new byte[12];
        
        SmartPackHeader[0] = (byte) '*';
        SmartPackHeader[1] = (byte) 's';
        SmartPackHeader[2] = (byte) 'c';
        SmartPackHeader[3] = (byte) 'p';
        SmartPackHeader[4] = (byte) '@';
        SmartPackHeader[5] = localparams.MAC[3];
        SmartPackHeader[6] = localparams.MAC[4];
        SmartPackHeader[7] = localparams.MAC[5];
        SmartPackHeader[8] = (byte) command;
        SmartPackHeader[9] = (byte) (command >> 8);
        SmartPackHeader[10] = (byte) (payload.length);
        SmartPackHeader[11] = (byte) (payload.length >> 8);
        
        SmartPackTxFrame = new byte[SmartPackHeader.length + payload.length + 1]; //+1 Checksum
        System.arraycopy(SmartPackHeader, 0, SmartPackTxFrame, 0, SmartPackHeader.length);
        System.arraycopy(payload, 0, SmartPackTxFrame, SmartPackHeader.length, payload.length);

        byte[] TxChecksum = new byte[1];
        TxChecksum[0] = 0;
        for (int i = 0; i < SmartPackTxFrame.length; i++) {
            TxChecksum[0] += SmartPackTxFrame[i];
        }
        System.arraycopy(TxChecksum, 0, SmartPackTxFrame, SmartPackTxFrame.length - 1, TxChecksum.length);
        
        try {
            socket = new DatagramSocket();
        } catch (SocketException ex) {
            System.out.println("Error create datagram socket: " + ex);
        }
        try {
            packet = new DatagramPacket(SmartPackTxFrame, SmartPackTxFrame.length, InetAddress.getByName("255.255.255.255"), 31500);
            socket.send(packet);
        } catch (IOException ex) {
            System.out.println("Error broadcasting: " + ex);
        }
        socket.close();
    }

    public void changeIP(InetAddress ip) {
        localparams.IP = ip.getAddress();
        
        System.arraycopy(localparams.DeviceHostName, 0, localparams._SmartPackCommandParameters, 0, 16);
        System.arraycopy(localparams.IP, 0, localparams._SmartPackCommandParameters, 16, 4);
        System.arraycopy(localparams.GatewayIP, 0, localparams._SmartPackCommandParameters, 20, 4);
        System.arraycopy(localparams.SubnetMask, 0, localparams._SmartPackCommandParameters, 24, 4);
        System.arraycopy(localparams.PrimaryDNS, 0, localparams._SmartPackCommandParameters, 28, 4);
        System.arraycopy(localparams.SecondaryDNS, 0, localparams._SmartPackCommandParameters, 32, 4);
        localparams._SmartPackCommandParameters[36] = localparams.DHCP;
        localparams._SmartPackCommandParameters[37] = 0;
        localparams._SmartPackCommandParameters[38] = 0;
        localparams._SmartPackCommandParameters[39] = 0;

        sendCommand(localparams._SmartPackCommandParameters, SmartCommand_F_SETLOCAL);
    }

    public void changeRemoteIP(InetAddress ip) {
        socketparams.RemoteIP = ip.getAddress();
        
        socketparams._SmartPackCommandParameters[0] = socketparams.ServerConfig;
        socketparams._SmartPackCommandParameters[1] = socketparams.ClientConfig;
        socketparams._SmartPackCommandParameters[2] = socketparams.Rsv1;
        socketparams._SmartPackCommandParameters[3] = socketparams.Rsv2;
        socketparams._SmartPackCommandParameters[4] = (byte) (socketparams.HostPort);
        socketparams._SmartPackCommandParameters[5] = (byte) (socketparams.HostPort >> 8);
        socketparams._SmartPackCommandParameters[6] = (byte) (socketparams.RemotePort);
        socketparams._SmartPackCommandParameters[7] = (byte) (socketparams.RemotePort >> 8);
        socketparams._SmartPackCommandParameters[8] = socketparams.RemoteIP[0];
        socketparams._SmartPackCommandParameters[9] = socketparams.RemoteIP[1];
        socketparams._SmartPackCommandParameters[10] = socketparams.RemoteIP[2];
        socketparams._SmartPackCommandParameters[11] = socketparams.RemoteIP[3];
        
        sendCommand(socketparams._SmartPackCommandParameters, SmartCommand_F_SETSOCKETS);
    }

    public void changePort(int port) {
        socketparams.HostPort = port;
        
        socketparams._SmartPackCommandParameters[0] = socketparams.ServerConfig;
        socketparams._SmartPackCommandParameters[1] = socketparams.ClientConfig;
        socketparams._SmartPackCommandParameters[2] = socketparams.Rsv1;
        socketparams._SmartPackCommandParameters[3] = socketparams.Rsv2;
        socketparams._SmartPackCommandParameters[4] = (byte) (socketparams.HostPort);
        socketparams._SmartPackCommandParameters[5] = (byte) (socketparams.HostPort >> 8);
        socketparams._SmartPackCommandParameters[6] = (byte) (socketparams.RemotePort);
        socketparams._SmartPackCommandParameters[7] = (byte) (socketparams.RemotePort >> 8);
        socketparams._SmartPackCommandParameters[8] = socketparams.RemoteIP[0];
        socketparams._SmartPackCommandParameters[9] = socketparams.RemoteIP[1];
        socketparams._SmartPackCommandParameters[10] = socketparams.RemoteIP[2];
        socketparams._SmartPackCommandParameters[11] = socketparams.RemoteIP[3];
        
        sendCommand(socketparams._SmartPackCommandParameters, SmartCommand_F_SETSOCKETS);
    }

    public void changeRemotePort(int port) {
        socketparams.RemotePort = port;
        
        socketparams._SmartPackCommandParameters[0] = socketparams.ServerConfig;
        socketparams._SmartPackCommandParameters[1] = socketparams.ClientConfig;
        socketparams._SmartPackCommandParameters[2] = socketparams.Rsv1;
        socketparams._SmartPackCommandParameters[3] = socketparams.Rsv2;
        socketparams._SmartPackCommandParameters[4] = (byte) (socketparams.HostPort);
        socketparams._SmartPackCommandParameters[5] = (byte) (socketparams.HostPort >> 8);
        socketparams._SmartPackCommandParameters[6] = (byte) (socketparams.RemotePort);
        socketparams._SmartPackCommandParameters[7] = (byte) (socketparams.RemotePort >> 8);
        socketparams._SmartPackCommandParameters[8] = socketparams.RemoteIP[0];
        socketparams._SmartPackCommandParameters[9] = socketparams.RemoteIP[1];
        socketparams._SmartPackCommandParameters[10] = socketparams.RemoteIP[2];
        socketparams._SmartPackCommandParameters[11] = socketparams.RemoteIP[3];
        
        sendCommand(socketparams._SmartPackCommandParameters, SmartCommand_F_SETSOCKETS);
    }
}
