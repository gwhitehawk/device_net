package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
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
        
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> controller.addDevice(device));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("MAC address is required", exception.getReason());
    }
    
    @Test
    void testAddDeviceWithEmptyMacAddress() {
        Device device = new Device("", "Gateway", "");
        
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> controller.addDevice(device));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("MAC address is required", exception.getReason());
    }
    
    @Test
    void testAddDeviceWithNullDeviceType() {
        Device device = new Device("AA:BB:CC:DD:EE:FF", null, "");
        
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> controller.addDevice(device));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Device type is required", exception.getReason());
    }
    
    @Test
    void testAddDeviceWithEmptyDeviceType() {
        Device device = new Device("AA:BB:CC:DD:EE:FF", "", "");
        
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> controller.addDevice(device));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Device type is required", exception.getReason());
    }

    @Test
    void testAddDeviceWithUnsupportedDeviceType() {
        Device device = new Device("AA:BB:CC:DD:EE:FF", "Router", "");
        
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> controller.addDevice(device));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Invalid device type", exception.getReason());
    }
    
    @Test
    void testAddDuplicateDevice() {
        Device device1 = new Device("AA:BB:CC:DD:EE:FF", "Gateway", "");
        Device device2 = new Device("AA:BB:CC:DD:EE:FF", "Switch", "");
        
        controller.addDevice(device1);
        
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> controller.addDevice(device2));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Device with this MAC address already exists", exception.getReason());
    }
    
    @Test
    void testAddDeviceWithSelfUplink() {
        Device device = new Device("AA:BB:CC:DD:EE:FF", "Gateway", "AA:BB:CC:DD:EE:FF");
        
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> controller.addDevice(device));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Device cannot be its own uplink", exception.getReason());
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
    void testGetNonExistingDevice() {
        Device result = controller.getDevice("FF:EE:DD:CC:BB:AA");
        assertNull(result);
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
    void testGetNetworkRootNotFound() {
        NetworkNode result = controller.getNetwork("FF:EE:DD:CC:BB:AA");
        assertNull(result);
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
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> controller.addDevice(accessPoint));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals(2, controller.listDevices().size()); // Ensure no additional device was added
    }
}
