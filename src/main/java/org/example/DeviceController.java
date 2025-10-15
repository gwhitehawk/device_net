package org.example;

import org.springframework.web.bind.annotation.*;
import java.util.*;

/**
 * REST controller for managing network devices and their topology.
 * Provides endpoints for adding devices, retrieving devices, and network trees.
 */
@RestController
@RequestMapping("/api")
public class DeviceController {
    /**
     * In-memory store of devices as NetworkNode objects, keyed by MAC address.
     */
    private final Map<String, NetworkNode> devices = new HashMap<>();
    
    /**
     * Returns a sorted list of all devices.
     * @return sorted list of Device objects
     */
    @GetMapping("/devices")
    public List<Device> listDevices() {
        return sortedDevices();
    }

    /**
     * Returns a device by its MAC address.
     * @param macAddress MAC address of the device
     * @return Device object, or null if not found
     */
    @GetMapping("/devices/{macAddress}")
    public Device getDevice(@PathVariable String macAddress) {
        return devices.get(macAddress).device;
    }

    /**
     * Adds a new device to the network.
     * @param device Device object to add
     * @return the added Device object
     * @throws IllegalArgumentException if validation fails
     */
    @PostMapping("/devices")
    public Device addDevice(@RequestBody Device device) {
        // Basic validation
        if (device.getMacAddress() == null || device.getMacAddress().isEmpty()) {
            throw new IllegalArgumentException("MAC address is required");
        }
        if (devices.containsKey(device.getMacAddress())) {
            throw new IllegalArgumentException("Device with this MAC address already exists");
        }
        if (device.getDeviceType() == null || device.getDeviceType().isEmpty()) {
            throw new IllegalArgumentException("Device type is required");
        }
        if (device.getMacAddress().equals(device.getUplinkMacAddress())) {
            throw new IllegalArgumentException("Device cannot be its own uplink");
        }
    
        NetworkNode node = new NetworkNode(device);
        devices.put(device.getMacAddress(), node);
        NetworkNode.linkNode(node, devices);  
        return device;
    }

    /**
     * Returns the network subtree starting from the given root MAC address.
     * @param rootMacAddress MAC address of the root device
     * @return NetworkNode representing the subtree, or null if not found
     */
    @GetMapping("/network/{rootMacAddress}")
    public NetworkNode getNetwork(@PathVariable String rootMacAddress) {
        return devices.get(rootMacAddress);
    }

    /**
     * Returns a forest of all root nodes and their corresponding subtrees.
     * @return list of NetworkNode objects representing all network trees
     */
    @GetMapping("/network")
    public List<NetworkNode> getFullNetwork() {
        List<NetworkNode> forest = new ArrayList<>();
        devices.values().stream()
            .filter(d -> !d.hasParent)
            .forEach(root -> forest.add(root));
        return forest;
    }

    /**
     * Returns a sorted list of all devices.
     * @return sorted list of Device objects
     */
    private List<Device> sortedDevices() {
        return devices.values().stream().map(node -> node.device)
                .sorted()
                .toList();
    }
}
