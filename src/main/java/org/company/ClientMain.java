package org.company;

import javax.swing.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class ClientMain {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            String serverIP;
            int serverPort;

            while (true) {
                serverIP = JOptionPane.showInputDialog("Enter the server IP (e.g., localhost):");
                if (serverIP == null || serverIP.trim().isEmpty() || !isValidIPAddress(serverIP)) {
                    JOptionPane.showMessageDialog(null, "Invalid IP address. Please try again.");
                    continue;
                }

                String serverPortStr = JOptionPane.showInputDialog("Enter the server port:");
                if (serverPortStr == null || serverPortStr.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Port number cannot be empty. Please try again.");
                    continue;
                }

                try {
                    serverPort = Integer.parseInt(serverPortStr);
                    if (serverPort < 0 || serverPort > 65535) {
                        JOptionPane.showMessageDialog(null, "Port number must be between 0 and 65535.");
                        continue;
                    }

                    // Attempt to connect to the server
                    Socket testSocket = new Socket(serverIP, serverPort);
                    testSocket.close();
                    break; // Break the loop if connection is successful
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Please enter a valid port number.");
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null, "Could not connect to the server. Please try again.");
                }
            }

            // Initialize the user manager, network manager, and UI
            ClientUserManager userManager = new ClientUserManager();
            ChatClientUI ui = new ChatClientUI(null, userManager);
            ClientNetworkManager networkManager = new ClientNetworkManager(serverIP, serverPort, ui);
            if (networkManager.tryToConnect(serverIP, serverPort)) {
                ui.setNetworkManager(networkManager);
                ui.refreshUserList(); // Refresh user list from file on startup
                ui.displayMessage("Welcome to the Chat Client");
            } else {
                System.exit(1); // Exit the application if connection failed
            }
        });
    }

    private static boolean isValidIPAddress(String ip) {
        try {
            if (ip == null || ip.isEmpty()) {
                return false;
            }
            InetAddress address = InetAddress.getByName(ip);
            // If the IP address is valid, return true
            return true;
        } catch (UnknownHostException ex) {
            // The IP address is invalid, return false
            return false;
        }
    }
}
