package org.company;

import javax.swing.*;
import java.awt.*;

public class ClientMain {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Use GridLayout to layout labels and text fields
            JPanel panel = new JPanel(new GridLayout(0, 1, 2, 2));
            JTextField usernameField = new JTextField();
            JTextField portField = new JTextField("8080"); // set default port
            JTextField serverIPField = new JTextField("localhost"); // set default IP

            panel.add(new JLabel("Name:"));
            panel.add(usernameField);
            panel.add(new JLabel("Port:"));
            panel.add(portField);
            panel.add(new JLabel("Server IP:"));
            panel.add(serverIPField);

            // Prompt user to enter their information
            int result = JOptionPane.showConfirmDialog(null, panel, "Enter your information",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                String userName = usernameField.getText();
                String serverPortStr = portField.getText();
                String serverIP = serverIPField.getText();

                // Validate input
                if (userName.trim().isEmpty() || !isValidPort(serverPortStr) || !isValidIPAddress(serverIP)) {
                    JOptionPane.showMessageDialog(null, "Please enter valid information.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                    System.exit(1); // Exit on invalid input
                }

                int serverPort = Integer.parseInt(serverPortStr);

                ClientUserManager userManager = new ClientUserManager();
                ClientNetworkManager networkManager = new ClientNetworkManager(serverIP, serverPort, userName);
                ChatClientUI ui = new ChatClientUI(networkManager, userManager, userName);

                // If needed, pass the ChatClientUI reference to the ClientNetworkManager
                networkManager.setChatClientUI(ui); // You must add this setter method in ClientNetworkManager class.


                // Try to connect to the server
                if (networkManager.tryToConnect()) {
                    ui.setNetworkManager(networkManager);
                    ui.refreshUserList(); // Refresh user list from file on startup
//                    ui.displayMessage("Welcome to the Chat Client");
                } else {
                    JOptionPane.showMessageDialog(null, "Could not connect to the server.",
                            "Connection Error", JOptionPane.ERROR_MESSAGE);
                    System.exit(1); // Exit the application if connection failed
                }
            } else {
                System.exit(0); // User cancelled the operation
            }
        });
    }


    private static boolean isValidPort(String portStr) {
        try {
            int port = Integer.parseInt(portStr);
            return port >= 0 && port <= 65535;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean isValidIPAddress(String ip) {
        // Implementation to validate the IP address...
        // Dummy implementation, replace with real validation
        return ip != null && !ip.trim().isEmpty();
    }
}
