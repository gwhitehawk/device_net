package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

public class DeviceControllerTest {
    
    private DeviceController controller;
    
    @BeforeEach
    void setUp() {
        controller = new DeviceController();
    }
    
    @Test
    void testAddDevice() {
        Device device = new Device("AA:BB:CC:DD:EE:FF", "Gateway", "");
        Device result = controller.addDevice(device);
        
        assertEquals(device.getMacAddress(), result.getMacAddress());
        assertEquals(device.getDeviceType(), result.getDeviceType());
        assertEquals(device.getUplinkMacAddress(), result.getUplinkMacAddress());
    }
    
    @Test
    void testAddDeviceWithNullMacAddress() {
        Device device = new Device(null, "Gateway", "");
        
        assertThrows(IllegalArgumentException.class, () -> controller.addDevice(device));
    }
    
    @Test
    void testAddDeviceWithEmptyMacAddress() {
        Device device = new Device("", "Gateway", "");
        
        assertThrows(IllegalArgumentException.class, () -> controller.addDevice(device));
    }
    
    @Test
    void testAddDeviceWithNullDeviceType() {
        Device device = new Device("AA:BB:CC:DD:EE:FF", null, "");
        
        assertThrows(IllegalArgumentException.class, () -> controller.addDevice(device));
    }
    
    @Test
    void testAddDeviceWithEmptyDeviceType() {
        Device device = new Device("AA:BB:CC:DD:EE:FF", "", "");
        
        assertThrows(IllegalArgumentException.class, () -> controller.addDevice(device));
    }

    @Test
    void testAddDeviceWithUnsupportedDeviceType() {
        Device device = new Device("AA:BB:CC:DD:EE:FF", "Router", "");
        
        assertThrows(IllegalArgumentException.class, () -> controller.addDevice(device));
    }
    
    @Test
    void testAddDuplicateDevice() {
        Device device1 = new Device("AA:BB:CC:DD:EE:FF", "Gateway", "");
        Device device2 = new Device("AA:BB:CC:DD:EE:FF", "Switch", "");
        
        controller.addDevice(device1);
        
        assertThrows(IllegalArgumentException.class, () -> controller.addDevice(device2));
    }
    
    @Test
    void testAddDeviceWithSelfUplink() {
        Device device = new Device("AA:BB:CC:DD:EE:FF", "Gateway", "AA:BB:CC:DD:EE:FF");
        
        assertThrows(IllegalArgumentException.class, () -> controller.addDevice(device));
    }
    
    @Test
    void testGetDevice() {
        Device device = new Device("AA:BB:CC:DD:EE:FF", "Gateway", "");
        controller.addDevice(device);
        
        Device result = controller.getDevice("AA:BB:CC:DD:EE:FF");
        
        assertNotNull(result);
        assertEquals(device.getMacAddress(), result.getMacAddress());
        assertEquals(device.getDeviceType(), result.getDeviceType());
        assertEquals(device.getUplinkMacAddress(), result.getUplinkMacAddress());
    }
    
    @Test
    void testListDevicesEmpty() {
        List<Device> devices = controller.listDevices();
        
        assertTrue(devices.isEmpty());
    }
    
    @Test
    void testListDevicesSorted() {
        Device gateway = new Device("AA:BB:CC:DD:EE:FF", "Gateway", "");
        Device switchDevice = new Device("BB:CC:DD:EE:FF:AA", "Switch", "AA:BB:CC:DD:EE:FF");
        Device accessPoint = new Device("CC:DD:EE:FF:AA:BB", "Access Point", "BB:CC:DD:EE:FF:AA");
        
        controller.addDevice(switchDevice);
        controller.addDevice(accessPoint);
        controller.addDevice(gateway);
        
        List<Device> devices = controller.listDevices();
        
        assertEquals(3, devices.size());
        // Devices should be sorted according to Device's compareTo implementation
        assertEquals("Access Point", devices.get(0).getDeviceType());
        assertEquals("Switch", devices.get(1).getDeviceType());
        assertEquals("Gateway", devices.get(2).getDeviceType());
    }
    
    @Test
    void testGetNetwork() {
        Device gateway = new Device("AA:BB:CC:DD:EE:FF", "Gateway", "");
        Device switchDevice = new Device("BB:CC:DD:EE:FF:AA", "Switch", "AA:BB:CC:DD:EE:FF");
        
        controller.addDevice(gateway);
        controller.addDevice(switchDevice);
        
        NetworkNode network = controller.getNetwork("AA:BB:CC:DD:EE:FF");
        
        assertNotNull(network);
        assertEquals("AA:BB:CC:DD:EE:FF", network.device.getMacAddress());
        assertEquals(1, network.children.size());
        assertTrue(network.children.stream().anyMatch(child -> 
            child.device.getMacAddress().equals("BB:CC:DD:EE:FF:AA")));
    }
    
    @Test
    void testGetFullNetwork() {
        Device gateway1 = new Device("AA:BB:CC:DD:EE:FF", "Gateway", "");
        Device gateway2 = new Device("11:22:33:44:55:66", "Gateway", "");
        Device switchDevice = new Device("BB:CC:DD:EE:FF:AA", "Switch", "AA:BB:CC:DD:EE:FF");
        
        controller.addDevice(gateway1);
        controller.addDevice(gateway2);
        controller.addDevice(switchDevice);
        
        List<NetworkNode> forest = controller.getFullNetwork();
        
        assertEquals(2, forest.size()); // Two root nodes (gateways)
        assertTrue(forest.stream().anyMatch(node -> 
            node.device.getMacAddress().equals("AA:BB:CC:DD:EE:FF")));
        assertTrue(forest.stream().anyMatch(node -> 
            node.device.getMacAddress().equals("11:22:33:44:55:66")));
    }
    
    @Test
    void testAddDeviceWithUplinkCreateHierarchy() {
        Device gateway = new Device("AA:BB:CC:DD:EE:FF", "Gateway", "");
        Device switchDevice = new Device("BB:CC:DD:EE:FF:AA", "Switch", "AA:BB:CC:DD:EE:FF");
        Device accessPoint = new Device("CC:DD:EE:FF:AA:BB", "Access Point", "BB:CC:DD:EE:FF:AA");
        
        controller.addDevice(gateway);
        controller.addDevice(switchDevice);
        controller.addDevice(accessPoint);
        
        NetworkNode gatewayNode = controller.getNetwork("AA:BB:CC:DD:EE:FF");
        
        assertNotNull(gatewayNode);
        assertFalse(gatewayNode.hasParent);
        assertEquals(1, gatewayNode.children.size());
        
        NetworkNode switchNode = gatewayNode.children.iterator().next();
        assertTrue(switchNode.hasParent);
        assertEquals(1, switchNode.children.size());
        
        NetworkNode accessPointNode = switchNode.children.iterator().next();
        assertTrue(accessPointNode.hasParent);
        assertEquals(0, accessPointNode.children.size());
    }

    @Test
    void testAddDeviceWithUplinkCreateHierarchyWithCycle() {
        Device gateway = new Device("AA:BB:CC:DD:EE:FF", "Gateway", "");
        Device switchDevice = new Device("BB:CC:DD:EE:FF:AA", "Switch", "CC:DD:EE:FF:AA:BB");
        Device accessPoint = new Device("CC:DD:EE:FF:AA:BB", "Access Point", "BB:CC:DD:EE:FF:AA");

        controller.addDevice(gateway);
        controller.addDevice(switchDevice);

        // Attempt to add a cycle-inducing device
        assertThrows(IllegalArgumentException.class, () -> controller.addDevice(accessPoint));
        assertEquals(2, controller.listDevices().size()); // Ensure no additional device was added
    }
}
