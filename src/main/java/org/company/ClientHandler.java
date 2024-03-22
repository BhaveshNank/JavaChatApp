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
            if (this.name != null && !this.name.trim().isEmpty()) {
                sendWelcomeMessage();
                notifyJoin();
                server.updateActiveUsers(); // update the active user list
            }

            String inputLine;
            while ((inputLine = input.readLine()) != null) {
                processInput(inputLine, this); // Process regular messages
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

    private void processInput(String inputLine, ClientHandler client) {
        if (!inputLine.startsWith("/")) {
            // If it's not a command, broadcast it as a chat message
            server.broadcastMessage(client.getName() + ": " + inputLine, this);
        } else {
            // Here, you would handle the command - but do not send the command itself to all clients.
            handleCommand(inputLine, client);
        }
    }


    private void handleCommand(String command, ClientHandler client) {
        // Parse and execute the command
        // Do not broadcast this as a chat message
    }



    public void disconnect() {
        try {
            socket.close();
        } catch (IOException e) {
            // Log or handle the exception
        }
        server.removeClient(this);
        server.updateActiveUsers();
    }

    public void sendMessage(String message) {
        output.println(message);
    }

    public String getClientName() {
        return name;
    }
}
