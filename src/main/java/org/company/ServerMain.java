package org.company;

public class ServerMain {
    public static void main(String[] args) {
        // Determine the port number, can be passed as a command line argument
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8080;

        try {
            Server server = new Server(port);
            System.out.println("Starting the server on port " + port);
            server.start();

            // Shutdown hook to gracefully stop the server
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Stopping the server.");
                server.stop();
            }));
        } catch (Exception e) {
            System.err.println("Error starting the server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
