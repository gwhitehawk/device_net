package org.example.client;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.print("Sample server workflow.\n");

        // Connect to Spring Boot server and add sample devices to the datastore
        String accessNode = "{\"macAddress\":\"00:11:AA:BB:44:55\",\"deviceType\":\"Access Point\",\"uplinkMacAddress\":\"00:11:22:33:44:55\"}";
        String switchNode = "{\"macAddress\":\"00:11:22:33:44:55\",\"deviceType\":\"Switch\",\"uplinkMacAddress\":\"AA:BB:CC:DD:EE:FF\"}";
        String gatewayNode = "{\"macAddress\":\"AA:BB:CC:DD:EE:FF\",\"deviceType\":\"Gateway\",\"uplinkMacAddress\":\"\"}";
        String orphanSwitchNode = "{\"macAddress\":\"FF:EE:DD:CC:BB:AA\",\"deviceType\":\"Switch\",\"uplinkMacAddress\":\"\"}";

        List<String> devices = List.of(accessNode, switchNode, gatewayNode, orphanSwitchNode);
        for (String deviceJson : devices) {
            try {
                URL url = URL.of("http://localhost:8080/api/devices");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = deviceJson.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }
                int responseCode = conn.getResponseCode();
                System.out.println("addDevice response code: " + responseCode);
                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Retrieve sample devices by their macAddress
        String[] macAddresses = {"00:11:AA:BB:44:55", "00:11:22:33:44:55", "AA:BB:CC:DD:EE:FF", "FF:EE:DD:CC:BB:AA"};
        for (String mac : macAddresses) {
            try {
                URL url = URL.of("http://localhost:8080/api/devices/" + mac);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                int responseCode = conn.getResponseCode();
                System.out.println("GET device " + mac + " response code: " + responseCode);
                try (var is = conn.getInputStream()) {
                    System.out.println(new String(is.readAllBytes()));
                }
                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Retrieve a sorted list of all devices
        try {
            URL url = URL.of("http://localhost:8080/api/devices");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            int responseCode = conn.getResponseCode();
            System.out.println("GET all devices response code: " + responseCode);
            try (var is = conn.getInputStream()) {
                System.out.println(new String(is.readAllBytes()));
            }
            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Retrieve and print the full network topology
        try {
            URL url = URL.of("http://localhost:8080/api/network");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            int responseCode = conn.getResponseCode();
            System.out.println("GET full network response code: " + responseCode);
            try (var is = conn.getInputStream()) {
                System.out.println(new String(is.readAllBytes()));
            }
            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Retrieve and print the subtree from the switch node
        try {
            URL url = URL.of("http://localhost:8080/api/network/00:11:22:33:44:55");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            int responseCode = conn.getResponseCode();
            System.out.println("GET switch subtree response code: " + responseCode);
            try (var is = conn.getInputStream()) {
                System.out.println(new String(is.readAllBytes()));
            }
            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}