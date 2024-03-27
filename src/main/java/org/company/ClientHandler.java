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
            String attemptedName = input.readLine().trim();
            System.out.println("Debug: Attempted Username received - '" + attemptedName + "'"); // Debug line

            if (attemptedName == null || attemptedName.isEmpty()) {
                System.out.println("Username is null or empty");
                output.println("Error: Username cannot be empty.");
                disconnect(); // Disconnect if no valid name is provided
                return; // Exit the run method
            } else if (server.isUsernameTaken(attemptedName)) {
                System.out.println("Username is already taken: " + attemptedName);
                output.println("Error: Username is already taken.");
                socket.close(); // Disconnect if username is taken
                return; // Exit the run method
            }

            // If username is valid and not taken, proceed
            this.name = attemptedName; // Set the username
            server.notifyNewClientConnection(this.name);
            sendWelcomeMessage();
            notifyJoin();
            server.updateActiveUsers(); // Update the active user list

            String inputLine;
            while ((inputLine = input.readLine()) != null) {
                if (inputLine.startsWith("/")) {
                    // If the input line starts with "/", it's a command. Execute it.
                    server.executeCommand(inputLine, this);
                } else {
                    // If it's not a command, process it as a regular message.
                    processInput(inputLine, this);
                }
            }
        } catch (IOException e) {
            System.out.println(name + " encountered an error: " + e.getMessage());
//            server.broadcastMessage(name + " has left the chat!", this);
            e.printStackTrace();
        } finally {
            disconnect();
        }
    }


    private void sendWelcomeMessage() {
        sendMessage("Welcome to the Chat Client, " + name);
    }

    private void notifyJoin() {
        String joinMessage = name + " has joined the chat!";
        server.broadcastMessage(joinMessage, this);
    }



    private void processInput(String inputLine, ClientHandler client) {
        // If the message starts with "@", it's a private message
        if (inputLine.startsWith("@")) {
            String[] parts = inputLine.split(" ", 2);
            if (parts.length < 2) {
                sendMessage("Invalid private message format. Usage: @username message");
            } else {
                String recipientName = parts[0].substring(1); // remove "@" and get the username
                String message = parts[1];
                server.sendPrivateMessage(message, recipientName, client);
            }
        } else if (!inputLine.startsWith("/")) {
            // If it's not a command, broadcast it as a chat message
            server.broadcastMessage(inputLine, this);

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
            if (!socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            // Log or handle the exception
        } finally {
            // Notify the server that this client is disconnecting
            if (server != null) {
                server.removeClient(this);
            }
        }
    }


    public void sendMessage(String message) {
        System.out.println("Debug: Sending message to " + this.getClientName() + " - " + message); // Debug statement
        output.println(message);
    }

    public String getClientName() {
        return name;
    }
}
