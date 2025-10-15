package org.example;

import java.util.Map;

public class Device implements Comparable<Device> {
    private String macAddress;
    private String deviceType;
    private String uplinkMacAddress;

    private static final Map<String, Integer> deviceTypePriority = Map.of(
        "Access Point", 1,
        "Switch", 2,
        "Gateway", 3
    );

    public Device() {}

    public Device(
            String macAddress,
            String deviceType,
            String uplinkMacAddress) {
        this.macAddress = macAddress;
        this.deviceType = deviceType;
        this.uplinkMacAddress = uplinkMacAddress;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getUplinkMacAddress() {
        return uplinkMacAddress;
    }

    public void setUplinkMacAddress(String uplinkMacAddress) {
        this.uplinkMacAddress = uplinkMacAddress;
    }

    @Override
    public int compareTo(Device other) {
        return deviceTypePriority.get(this.deviceType) - deviceTypePriority.get(other.deviceType);
    }
}
