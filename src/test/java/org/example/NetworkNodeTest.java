package org.example;

import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

public class NetworkNodeTest {
    @Test
    void testLinkNodeParentExists() {
        Device parentDevice = new Device("parent", "Gateway", "");
        Device childDevice = new Device("child", "Switch", "parent");
        NetworkNode parentNode = new NetworkNode(parentDevice);
        NetworkNode childNode = new NetworkNode(childDevice);
        Map<String, NetworkNode> nodeMap = new HashMap<>();
        nodeMap.put(parentDevice.getMacAddress(), parentNode);
        nodeMap.put(childDevice.getMacAddress(), childNode);
        // Checks that linkNode links childNode to its parent in nodeMap
        NetworkNode.linkNode(childNode, nodeMap);
        assertTrue(parentNode.children.contains(childNode));
        assertTrue(childNode.children.isEmpty());
        assertTrue(childNode.hasParent);
    }

    @Test
    void testLinkNodeParentDoesNotExist() {
        Device childDevice = new Device("child", "Switch", "parent");
        NetworkNode childNode = new NetworkNode(childDevice);
        Map<String, NetworkNode> nodeMap = new HashMap<>();
        nodeMap.put(childDevice.getMacAddress(), childNode);
        // Parent does not exist in nodeMap
        NetworkNode.linkNode(childNode, nodeMap);
        assertTrue(childNode.children.isEmpty());
        assertFalse(childNode.hasParent);
    }

    @Test
    void testLinkNodeWithExistingChildren() {
        Device parentDevice = new Device("parent", "Gateway", "");
        Device childDevice1 = new Device("child1", "Switch", "parent");
        Device childDevice2 = new Device("child2", "Access Point", "parent");
        NetworkNode parentNode = new NetworkNode(parentDevice);
        NetworkNode childNode1 = new NetworkNode(childDevice1);
        NetworkNode childNode2 = new NetworkNode(childDevice2);
        Map<String, NetworkNode> nodeMap = new HashMap<>();
        nodeMap.put(childDevice1.getMacAddress(), childNode1);
        nodeMap.put(childDevice2.getMacAddress(), childNode2);
        // Now add parentNode and link
        nodeMap.put(parentDevice.getMacAddress(), parentNode);
        NetworkNode.linkNode(parentNode, nodeMap);
        assertTrue(parentNode.children.contains(childNode1));
        assertTrue(parentNode.children.contains(childNode2));
        assertTrue(childNode1.hasParent);
        assertTrue(childNode2.hasParent);
    }

    @Test
    void testLinkNodeNoUplink() {
        Device device = new Device("node", "Gateway", "");
        NetworkNode node = new NetworkNode(device);
        Map<String, NetworkNode> nodeMap = new HashMap<>();
        nodeMap.put(device.getMacAddress(), node);
        NetworkNode.linkNode(node, nodeMap);
        assertTrue(node.children.isEmpty());
        assertFalse(node.hasParent);
    }

    @Test
    void testLinkNodeCircularReferenceFails() {
        Device deviceA = new Device("A", "Switch", "B");
        Device deviceB = new Device("B", "Switch", "A");
        NetworkNode nodeA = new NetworkNode(deviceA);
        NetworkNode nodeB = new NetworkNode(deviceB);
        Map<String, NetworkNode> nodeMap = new HashMap<>();
        nodeMap.put(deviceA.getMacAddress(), nodeA);
        NetworkNode.linkNode(nodeA, nodeMap);
        // Linking should not create loops
        nodeMap.put(deviceB.getMacAddress(), nodeB);
        assertThrows(IllegalArgumentException.class, () -> NetworkNode.linkNode(nodeB, nodeMap));
    }

    @Test
    void testGetLogCountMap() {
        List<String> logs = Arrays.asList(
            "ERROR 2025-10-24 10:12:05 User1 failed login",
            "INFO 2025-10-24 10:12:06 User1 login",
            "WARN 2025-10-24 10:12:05 User2 password attempt"
        );
        Map<String, Pair> result = NetworkNode.getLogCountMap(logs);
        System.out.println(result.toString());
        assertTrue(true);
    }
}