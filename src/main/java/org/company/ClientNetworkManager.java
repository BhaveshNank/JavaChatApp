package org.company;

import javax.swing.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ClientNetworkManager {
    private Socket socket;
    private PrintWriter output;
    private BufferedReader input;
    private ChatClientUI ui;
    private String serverIP;
    private int serverPort;

    public ClientNetworkManager(String serverIP, int serverPort, ChatClientUI ui) {
        this.ui = ui;
        this.serverIP = serverIP;
        this.serverPort = serverPort;

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
            socket = new Socket(serverIP, serverPort); // Use member variables
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);
            startListening();
            return true; // Connection was successful
        } catch (IOException e) {
            // Handle connection error
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(null,
                        "Could not connect to the server at " + serverIP + ":" + serverPort + ". Please check if the server is running and try again.",
                        "Connection Error", JOptionPane.ERROR_MESSAGE);
            });
            return false; // Connection failed
        }
    }



    private void startListening() {
        new Thread(() -> {
            try {
                String message;
                while ((message = input.readLine()) != null) {
                    ui.displayMessage(message);
                }
            } catch (IOException e) {
                // Notify the user of the disconnection
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(ui.getFrame(),
                        "You have been disconnected from the server.",
                        "Disconnected", JOptionPane.ERROR_MESSAGE));
                // Attempt to reconnect or close resources as necessary
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
