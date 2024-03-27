package org.company;

import javax.swing.*;
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
    private JTextArea serverLogTextArea;
    private ClientHandler coordinator;


    // Setter method for serverLogTextArea
    public void setServerLogTextArea(JTextArea logTextArea) {
        this.serverLogTextArea = logTextArea;
    }

    // Method to append a message to the server log
    public void appendToServerLog(String message) {
        if (serverLogTextArea != null) {
            SwingUtilities.invokeLater(() -> {
                serverLogTextArea.append(message + "\n");
            });
        } else {
            System.out.println("Server log text area not set or GUI not initialized.");
        }
    }
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
                    appendToServerLog("Accepted connection from " + clientSocket.getInetAddress().getHostAddress()); // Log the connection

                    ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                    threadPool.execute(clientHandler); // Handle client in a separate thread
                    clientHandlers.add(clientHandler);
                    updateActiveUsers(); // call this method to update all clients with the new user list
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


    public synchronized void broadcastMessage(String message, ClientHandler sender) {
        for (ClientHandler client : clientHandlers) {
            // When sender is null, it means it's a system message, not a user message.
            if (sender == null) {
                client.sendMessage(message);
            } else if (!client.equals(sender)) {
                client.sendMessage(sender.getClientName() + ": " + message);
            }
        }
    }

    public synchronized boolean isUsernameTaken(String username) {
        return clientHandlers.stream().anyMatch(client -> username.equalsIgnoreCase(client.getClientName()));
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
                // This case seems to handle a similar functionality as the requested "/requestuserlist"
                // Ensure the command used here aligns with the one used in ClientNetworkManager's requestUserList method
                String activeUsers = clientHandlers.stream()
                        .map(ClientHandler::getClientName)
                        .collect(Collectors.joining(", "));
                sender.sendMessage("/updateusers " + activeUsers); // Ensure this format is consistent with the client's expectation
                break;
            case "/requestuserlist":
                // Respond to a request for a list of currently active users
                String users = clientHandlers.stream()
                        .map(ClientHandler::getClientName)
                        .collect(Collectors.joining(","));
                sender.sendMessage("/updateusers " + users);
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

    public synchronized void updateActiveUsers() {
        String activeUsers = clientHandlers.stream()
                .map(ClientHandler::getClientName)
                .collect(Collectors.joining(","));

        String updateMessage = "/updateusers " + activeUsers;
        for (ClientHandler client : clientHandlers) {
            client.sendMessage(updateMessage);
        }
    }

    public synchronized void setCoordinator(ClientHandler clientHandler) {
        // Inform the new coordinator
        clientHandler.sendMessage("You are now the coordinator.");

        // Inform all other clients about the new coordinator
        String coordinatorMessage = clientHandler.getClientName() + " is the coordinator";
        for (ClientHandler client : clientHandlers) {
            if (client != clientHandler) { // Don't send this message to the new coordinator
                client.sendMessage(coordinatorMessage);
            }
        }

        coordinator = clientHandler; // Update the coordinator reference
        // Broadcast a message if needed or update server log
    }
    public void notifyNewClientConnection(String clientName) {
        // Update the server log to show the new connection
        SwingUtilities.invokeLater(() -> serverLogTextArea.append(clientName + " has connected.\n"));

        synchronized (this) {
            // If there's no coordinator yet, set the newly connected client as the coordinator
            if (coordinator == null) {
                for (ClientHandler client : clientHandlers) {
                    if (client.getClientName().equals(clientName)) {
                        // Send a welcome message first
                        client.sendMessage("Welcome to the Chat Client, " + clientName);
                        setCoordinator(client);
                        // Log update must be done in the Swing thread if it's updating the GUI
//                        SwingUtilities.invokeLater(() -> serverLogTextArea.append(clientName + " is now the coordinator.\n"));
                        break;
                    }
                }
            }
        }
    }


    public void sendPrivateMessage(String message, String recipientName, ClientHandler sender) {
        for (ClientHandler client : clientHandlers) {
            if (client.getClientName().equals(recipientName)) {
                System.out.println("Debug: Sending private message from " + sender.getClientName() + " to " + recipientName); // Debugging line
                client.sendMessage(sender.getClientName() + " (private): " + message);
                return; // message sent to the intended recipient; no need to continue the loop
            }
        }
        // If we reach here, the recipient was not found
        System.out.println("Debug: Private recipient not found: " + recipientName); // Debugging line
        sender.sendMessage("User " + recipientName + " not found or not connected.");
    }


}
