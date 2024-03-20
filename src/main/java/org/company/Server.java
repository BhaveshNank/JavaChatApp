package org.company;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


public class Server {
    private int port;
    private List<ClientHandler> clientHandlers;
    private ServerSocket serverSocket;
    private boolean isRunning;
    private ExecutorService threadPool; // For handling client threads
//    private CommandExecutor commandExecutor;

    public Server(int port) {
        this.port = port;
        this.clientHandlers = new ArrayList<>();
        this.isRunning = false;
        this.threadPool = Executors.newCachedThreadPool();
//        this.commandExecutor = new CommandExecutor(this);
    }

    public void start() {
        isRunning = true;
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server started on port " + port);

            while (isRunning) {
                try {
                    Socket clientSocket = serverSocket.accept(); // Accept incoming connections
                    System.out.println("Accepted connection from " + clientSocket);
                    ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                    threadPool.execute(clientHandler); // Handle client in a separate thread
                    clientHandlers.add(clientHandler);
                } catch (IOException e) {
                    if (!serverSocket.isClosed()) {
                        System.err.println("Error accepting client connection: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Could not start the server: " + e.getMessage());
            e.printStackTrace();
        } finally {
            stop(); // Ensure the server is stopped if it exits the loop
        }
    }


    public void stop() {
        isRunning = false;
        try {
            serverSocket.close(); // Ensure the server socket is closed
        } catch (IOException e) {
            e.printStackTrace();
        }
        threadPool.shutdown(); // Shutdown the thread pool
        try {
            if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                threadPool.shutdownNow(); // Force shutdown if tasks did not finish
            }
        } catch (InterruptedException ex) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }


    public void broadcastMessage(String message, ClientHandler sender) {
        for (ClientHandler client : clientHandlers) {
            if (!client.equals(sender)) {
                client.sendMessage(message);
            }
        }
    }

    public void removeClient(ClientHandler clientHandler) {
        clientHandlers.remove(clientHandler);
        System.out.println("Client disconnected: " + clientHandler.getClientName());
    }

    public void executeCommand(String command, ClientHandler sender) {
        String[] tokens = command.split("\\s+", 2);
        String baseCommand = tokens[0].toLowerCase();
        switch (baseCommand) {
            case "/kick":
                if (tokens.length > 1) {
                    String usernameToKick = tokens[1];
                    kickUser(usernameToKick, sender);
                } else {
                    sender.sendMessage("Usage: /kick <username>");
                }
                break;
            case "/activeusers":
                String activeUsers = clientHandlers.stream()
                        .map(ClientHandler::getClientName)
                        .collect(Collectors.joining(", "));
                sender.sendMessage("Active users: " + activeUsers);
                break;
            // Handle other commands...
        }
    }

    private void kickUser(String username, ClientHandler initiator) {
        // Logic to kick a user...
    }


    // Main method for starting the server
    public static void main(String[] args) {
        int port = 8080; // or read from args or a configuration
        Server server = new Server(port);
        server.start();
    }

    public boolean isRunning() {
        return isRunning;
    }
}
