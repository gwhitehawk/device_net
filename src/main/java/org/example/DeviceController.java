package org.example;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

/**
 * REST controller for managing network devices and their topology.
 * Provides endpoints for adding devices, retrieving devices, and network trees.
 */
@RestController
@RequestMapping("/api")
public class DeviceController {

    private static final Set<String> VALID_DEVICE_TYPES = Set.of("Access Point", "Switch", "Gateway");

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
        if (!devices.containsKey(macAddress)) {
            return null;
        }
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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "MAC address is required");
        }
        if (devices.containsKey(device.getMacAddress())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Device with this MAC address already exists");
        }
        if (device.getDeviceType() == null || device.getDeviceType().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Device type is required");
        }
        if (VALID_DEVICE_TYPES.stream().noneMatch(dt -> dt.equals(device.getDeviceType()))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid device type");
        }
        if (device.getMacAddress().equals(device.getUplinkMacAddress())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Device cannot be its own uplink");
        }

        // Potentially add that the uplink must have priority equal or higher.
        // This was not specified in the requirements, so skipping for now.
    
        NetworkNode node = new NetworkNode(device);
        devices.put(device.getMacAddress(), node);
        try {
            NetworkNode.linkNode(node, devices);
        } catch (IllegalArgumentException e) {
            devices.remove(device.getMacAddress()); // Clean up if linking fails
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
        return device;
    }

    /**
     * Returns the network subtree starting from the given root MAC address.
     * @param rootMacAddress MAC address of the root device
     * @return NetworkNode representing the subtree, or null if not found
     */
    @GetMapping("/network/{rootMacAddress}")
    public NetworkNode getNetwork(@PathVariable String rootMacAddress) {
        if (!devices.containsKey(rootMacAddress)) {
            return null;
        }
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
