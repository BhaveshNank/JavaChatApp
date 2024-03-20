package org.company;

import javax.swing.*;
import java.io.*;
import java.net.Socket;

public class ClientNetworkManager {
    private Socket socket;
    private PrintWriter output;
    private BufferedReader input;
    private ChatClientUI ui;

    public ClientNetworkManager(String serverIP, int port, ChatClientUI ui) {
        this.ui = ui;
        if (!tryToConnect(serverIP, port)) {
            // Handle failed connection attempt
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(null,
                        "Could not connect to the server at " + serverIP + ":" + port + ". Please check if the server is running and try again.",
                        "Connection Error", JOptionPane.ERROR_MESSAGE);
                System.exit(1); // Exit or allow the user to retry
            });
        }
    }

    public boolean tryToConnect(String serverIP, int serverPort) {
        try {
            socket = new Socket(serverIP, serverPort);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);
            startListening();
            return true;
        } catch (IOException e) {
            System.err.println("Could not establish a connection to the server: " + serverIP + " on port " + serverPort);
            e.printStackTrace();
            return false;
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
}
