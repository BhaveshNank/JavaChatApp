package org.company;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClientHandler extends Thread {
    private final Socket socket;
    private final BufferedReader input;
    private final PrintWriter output;
    private final Server server;
    private String name; // Name should not be final because it's assigned later

    public ClientHandler(Socket socket, Server server) throws IOException {
        this.socket = socket;
        this.server = server;
        input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        output = new PrintWriter(socket.getOutputStream(), true);
    }

    @Override
    public void run() {
        try {
            // Read the username from the input stream
            this.name = input.readLine();
            boolean welcomeSent = false;
            if (this.name != null && !this.name.isEmpty()) {
                sendWelcomeMessage();
                notifyJoin();
                welcomeSent = true;
            }

            String inputLine;
            while ((inputLine = input.readLine()) != null) {
                if (!welcomeSent) {
                    sendWelcomeMessage(); // Send welcome message only if it's not yet sent
                    notifyJoin(); // Notify other clients about this client joining
//                    welcomeSent = true; // Set the flag as true after sending the welcome message
                } else {
                    processInput(inputLine); // Process regular messages
                }
            }
        } catch (IOException e) {
            server.broadcastMessage(name + " has left the chat!", this);
            e.printStackTrace();
        } finally {
            disconnect();
        }
    }

    private void sendWelcomeMessage() {
        sendMessage("Welcome to the Chat Client, " + name);
    }

    private void notifyJoin() {
        server.broadcastMessage(name + " has joined the chat!", this);
    }

    private void processInput(String inputLine) {
        if (!inputLine.trim().isEmpty()) {
            server.broadcastMessage(name + ": " + inputLine, this);
        }
    }


    public void disconnect() {
        try {
            socket.close();
        } catch (IOException e) {
            // Log or handle the exception
        }
        server.removeClient(this);
    }

    public void sendMessage(String message) {
        output.println(message);
    }

    public String getClientName() {
        return name;
    }
}
