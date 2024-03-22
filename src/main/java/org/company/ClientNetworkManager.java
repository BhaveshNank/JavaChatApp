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

    public ClientNetworkManager(String serverIP, int serverPort, String userName, ChatClientUI ui) {
        this.ui = ui;
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

    public boolean tryToConnect() {
        if (isConnected()) {
            return true; // Already connected
        }
        try {
            socket = new Socket(serverIP, serverPort);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);

            sendUserName(); // Send the username immediately after establishing a connection

            startListening();
            return true; // Connection was successful
        } catch (IOException e) {
            ui.displayMessage("Could not connect to server at " + serverIP + ":" + serverPort);
            return false; // Connection failed
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
                    if (message.startsWith("/updateusers")) {
                        String[] usernames = message.substring(13).split(",");
                        ui.updateUserList(usernames);
                    } else {
                        ui.displayMessage(message);
                    }
//                    ui.displayMessage(message);
                }
            } catch (IOException e) {
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(ui.getFrame(),
                        "You have been disconnected from the server.",
                        "Disconnected", JOptionPane.ERROR_MESSAGE));
            }
        }).start();
    }

    public void sendMessage(String message) {
        output.println(message);
    }

    public boolean isConnected() {
        return (socket != null) && socket.isConnected() && !socket.isClosed();
    }
}
