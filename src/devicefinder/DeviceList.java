/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package devicefinder;

import java.util.LinkedList;

/**
 *
 * @author Ceyhan ANKITÇI
 */
public class DeviceList {
    private LinkedList<Device> devices;
    
    public DeviceList(){
        devices = new LinkedList<>();
    }
    
    public Device getDevice(int num) throws IndexOutOfBoundsException{
        return devices.get(num);
    }
    
    public void addDevice(Device dev) throws IndexOutOfBoundsException{
        devices.add(dev);
    }
}
