package org.example;

import java.util.*;

public class NetworkNode {
    public Device device;
    public List<NetworkNode> children = new ArrayList<>();

    public NetworkNode(Device device) {
        this.device = device;
    }

    public static List<Device> findChildren(Map<String, Device> devices, String uplinkMacAddress) {
        List<Device> children = new ArrayList<>();
        for (Device device : devices.values()) {
            if (uplinkMacAddress.equals(device.getUplinkMacAddress())) {
                children.add(device);
            }
        }
        return children;
    }

    public static NetworkNode buildTree(Map<String, Device> devices, Device device) {
        NetworkNode node = new NetworkNode(device);
        List<Device> children = findChildren(devices, device.getMacAddress());
        for (Device child : children) {
            node.children.add(buildTree(devices, child));
        }
        return node;
    }
}
