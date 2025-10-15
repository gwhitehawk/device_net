package org.example;

import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

public class NetworkNodeTest {
    @Test
    void testFindChildren() {
        Device root = new Device("root", "Gateway", "");
        Device child1 = new Device("child1", "Switch", "root");
        Device child2 = new Device("child2", "Access Point", "root");
        Device unrelated = new Device("other", "Switch", "none");
    
        Map<String, Device> devices = new HashMap<>();
        devices.put(root.getMacAddress(), root);
        devices.put(child1.getMacAddress(), child1);
        devices.put(child2.getMacAddress(), child2);
        devices.put(unrelated.getMacAddress(), unrelated);
    
        List<Device> children = NetworkNode.findChildren(devices, "root");
        assertEquals(2, children.size());
        assertTrue(children.contains(child1));
        assertTrue(children.contains(child2));
    }

    @Test
    void testBuildTree() {
        Device root = new Device("root", "Gateway", "");
        Device child1 = new Device("child1", "Switch", "root");
        Device child2 = new Device("child2", "Access Point", "root");
        Device grandchild = new Device("grandchild", "Access Point", "child1");
    
        Map<String, Device> devices = new HashMap<>();
        devices.put(root.getMacAddress(), root);
        devices.put(child1.getMacAddress(), child1);
        devices.put(child2.getMacAddress(), child2);
        devices.put(grandchild.getMacAddress(), grandchild);
    
        NetworkNode tree = NetworkNode.buildTree(devices, root);
        assertEquals(root, tree.device);
        assertEquals(2, tree.children.size());
    
        NetworkNode child1Node = tree.children.stream().filter(n -> n.device.equals(child1)).findFirst().orElse(null);
        assertNotNull(child1Node);
        assertEquals(1, child1Node.children.size());
        assertEquals(grandchild, child1Node.children.get(0).device);
    }
}
