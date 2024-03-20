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

    public ClientHandler(Socket socket, Server server) throws IOException {
        this.socket = socket;
        this.server = server;
        input = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        output = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
        name = input.readLine();
        sendWelcomeMessage();
        notifyJoin();
    }

    private void sendWelcomeMessage() {
        sendMessage("Welcome " + name);
    }

    private void notifyJoin() {
        server.broadcastMessage(name + " has joined the chat!", this);
    }

    @Override
    public void run() {
        String inputLine;
        try {
            while ((inputLine = input.readLine()) != null) {
                processInput(inputLine);
            }
        } catch (IOException e) {
            server.removeClient(this);
            server.broadcastMessage(name + " has left the chat!", this);
        } finally {
            disconnect();
        }
    }

    private void processInput(String input) {
        if (input.startsWith("/")) {
            // Call the server's method directly to execute commands
            server.executeCommand(input, this);
        } else {
            // Regular chat message handling, broadcast the message or handle it as needed
            server.broadcastMessage(name + ": " + input, this);
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
        output.println(URLEncoder.encode(message, StandardCharsets.UTF_8));
    }

    public String getClientName() {
        return name;
    }
}
