package org.company;

import java.io.*;
import java.net.Socket;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class ClientHandler extends Thread {
    private final String name;
    private final Socket socket;
    private final BufferedReader input;
    private final PrintWriter output;
    private final Server server;
    private boolean welcomeSent = false; // Add a flag to track if welcome has been sent

    public ClientHandler(Socket socket, Server server) throws IOException {
        this.socket = socket;
        this.server = server;
        input = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        output = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);

        // Read the username from the input stream
        this.name = input.readLine();
        // Validate username
        if (this.name == null || this.name.trim().isEmpty()) {
            throw new IOException("Invalid username received.");
        }

        // Call the sendWelcomeMessage only once, right here
        sendWelcomeMessage(name);
        notifyJoin(name);
        welcomeSent = true; // Set the flag as true after sending the welcome message
    }

    private void sendWelcomeMessage(String userName) {
        sendMessage("Welcome to the Chat Client, " + userName);
    }

    private void notifyJoin(String userName) {
        server.broadcastMessage(userName + " has joined the chat!", this);
    }

    @Override
    public void run() {
        try {
            // Process incoming messages from the client
            String inputLine;
            while ((inputLine = input.readLine()) != null) {
                processInput(inputLine); // No need to pass the userName here since it's stored as 'name'
            }
        } catch (IOException e) {
            server.broadcastMessage(name + " has left the chat!", this);
            e.printStackTrace();
        } finally {
            disconnect();
        }
    }

    private void processInput(String inputLine) {
        if (inputLine.startsWith("/")) {
            // Handle command
            server.executeCommand(inputLine, this);
        } else {
            // Regular chat message handling
            server.broadcastMessage(name + ": " + inputLine, this); // Use the 'name' field directly
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
        output.println(message); // Remove URL encoding if not necessary
    }

    public String getClientName() {
        return name;
    }
}
