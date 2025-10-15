package org.example;

import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api")
public class DeviceController {
    // Dummy in-memory store
    private final Map<String, Device> devices = new HashMap<>();
    
    @GetMapping("/devices")
    public List<Device> listDevices() {
        return sortedDevices();
    }

    @GetMapping("/devices/{macAddress}")
    public Device getDevice(@PathVariable String macAddress) {
        return devices.get(macAddress);
    }

    @PostMapping("/devices")
    public Device addDevice(@RequestBody Device device) {
        devices.put(device.getMacAddress(), device);
        return device;
    }

    @GetMapping("/network/{rootMacAddress}")
    public NetworkNode getNetwork(@PathVariable String rootMacAddress) {
        Device root = devices.get(rootMacAddress);
        if (root == null) return null;
        return NetworkNode.buildTree(devices, root);
    }

    @GetMapping("/network")
    public List<NetworkNode> getFullNetwork() {
        List<NetworkNode> forest = new ArrayList<>();
        devices.values().stream()
            .filter(d -> d.getUplinkMacAddress() == null || d.getUplinkMacAddress().isEmpty())
            .forEach(root -> forest.add(NetworkNode.buildTree(devices, root)));
        return forest;
    }

    private List<Device> sortedDevices() {
        return devices.values().stream()
                .sorted()
                .toList();
    }
}
