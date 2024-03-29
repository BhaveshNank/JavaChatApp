package org.company;

import javax.swing.*;
import java.io.*;
import java.net.Socket;

public class ClientNetworkManager {
    private Socket socket;
    private PrintWriter output;
    private BufferedReader input;
    private ChatClientUI ui;
    private String serverIP;
    private int serverPort;
    private String userName; // Declare the userName variable

    public ClientNetworkManager(String serverIP, int serverPort, String userName) {
//        this.ui = ui;
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.userName = userName; // Initialize the userName variable

        if (!tryToConnect()) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(null,
                        "Could not connect to the server at " + serverIP + ":" + serverPort + ". Please check if the server is running and try again.",
                        "Connection Error", JOptionPane.ERROR_MESSAGE);
                System.exit(1); // Exit or allow the user to retry
            });
        }
    }
    public void setChatClientUI(ChatClientUI ui) {
        this.ui = ui;
        // You can use ui within ClientNetworkManager now
    }
    public boolean tryToConnect() {
        if (isConnected()) {
            return true; // Already connected
        }
        try {
            socket = new Socket(serverIP, serverPort);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);

            sendUserName(); // Send the username immediately after establishing a connection

            String serverResponse = input.readLine();
            if (serverResponse.startsWith("Error:")) {
                JOptionPane.showMessageDialog(null, serverResponse, "Connection Error", JOptionPane.ERROR_MESSAGE);
                disconnect(); // Disconnect from the server
                return false;
            }

            startListening();
            return true; // Connection was successful
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Could not connect to server at " + serverIP + ":" + serverPort + ".\nError: " + e.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
            return false; // Connection failed
        }
    }


    public void disconnect() {
        if (socket != null) {
            try {
                socket.close(); // Close the connection to the server
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendUserName() {
        if (output != null && userName != null && !userName.trim().isEmpty()) {
            output.println(userName); // Send the username to the server
        }
    }

    private void startListening() {
        new Thread(() -> {
            try {
                String message;
                while ((message = input.readLine()) != null) {
                    System.out.println("Received message: " + message);
                    final String msg = message; // to be used within the lambda expression
                    SwingUtilities.invokeLater(() -> {
                        if (msg.startsWith("/updateusers")) {
                            // Correct the offset to the length of the command plus the space
                            String[] usernames = msg.substring("/updateusers ".length()).split(",");
                            ui.updateUserList(usernames);
                        } else {
                            System.out.println("Debug: Received message to display: " + msg); // Debug statement
                            ui.displayMessage(msg);
                        }
                    });
                }
            } catch (IOException e) {
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(ui.getFrame(),
                        "You have been disconnected from the server.",
                        "Disconnected", JOptionPane.ERROR_MESSAGE));
            }
        }).start();
    }


    // Add a method to manually request the user list from the server
    public void requestUserList() {
        if (output != null) {
            output.println("/requestuserlist"); // Ensure this command is handled by the server
        }
    }

    public void sendMessage(String message) {
        output.println(message);
    }

    public boolean isConnected() {
        return (socket != null) && socket.isConnected() && !socket.isClosed();
    }
}


