package org.example;

import java.util.*;

/**
 * Represents a node in the network topology tree.
 * Each node wraps a Device and maintains a list of child nodes.
 * Provides static methods for linking nodes and cycle detection.
 */
public class NetworkNode {
    /**
     * The device represented by this node.
     */
    public Device device;
    /**
     * The list of child nodes (devices uplinked to this device).
     */
    public List<NetworkNode> children = new ArrayList<>();
    /**
     * Indicates whether this node has a parent in the network tree.
     */
    public boolean hasParent = false;

    /**
     * Constructs a NetworkNode for the given device.
     * @param device the device to wrap
     */
    public NetworkNode(Device device) {
        this.device = device;
    }

    /**
     * Links a child node to a parent node.
     * @param parent the parent node
     * @param child the child node
     */
    private static void linkParentChild(NetworkNode parent, NetworkNode child) {
        parent.children.add(child);
    }

    /**
     * Checks if the given node is its own descendant in the network map (cycle detection).
     * @param node the node to check
     * @param nodeMap the map of all nodes
     * @return true if a cycle is detected, false otherwise
     */
    private static boolean isSelfDescendant(NetworkNode node, Map<String, NetworkNode> nodeMap) {
        NetworkNode current = node;
        while (current.device.getUplinkMacAddress() != null && !node.device.getUplinkMacAddress().isEmpty()) {
            NetworkNode parent = nodeMap.get(current.device.getUplinkMacAddress());
            if (parent == null) {
                return false; // No further parent, so no cycle
            }
            if (parent == node) {
                return true; // Cycle detected
            }
            current = parent; // Move up the tree
        }
        return false; // Reached the top without finding a cycle
    }

    /**
     * Links the given node to its parent and children in the node map, checking for cycles.
     * @param node the node to link
     * @param nodeMap the map of all nodes
     * @throws IllegalArgumentException if a cycle would be created
     */
    public static void linkNode(NetworkNode node, Map<String, NetworkNode> nodeMap) {
        // Link device to its parent if uplinkMacAddress is set.
        // If such parent does not exist yet, it will be linked when the parent is added.
        if (node.device.getUplinkMacAddress() != null && !node.device.getUplinkMacAddress().isEmpty()) {
            NetworkNode parent = nodeMap.get(node.device.getUplinkMacAddress());
            if (parent != null) {
                // Check for cycle: parent must not be a descendant of node
                if (isSelfDescendant(node, nodeMap)) {
                    nodeMap.remove(node.device.getMacAddress()); // Clean up to avoid partial addition  
                    throw new IllegalArgumentException("Cycle detected: cannot link node as it would create a cycle.");
                }
                linkParentChild(parent, node);
                node.hasParent = true;
            }
        }
        // Link existing children to this device
        for (NetworkNode n : nodeMap.values()) {
            if (node.device.getMacAddress().equals(n.device.getUplinkMacAddress())) {
                NetworkNode.linkParentChild(node, n);
                n.hasParent = true;
            }
        }
    }
}
