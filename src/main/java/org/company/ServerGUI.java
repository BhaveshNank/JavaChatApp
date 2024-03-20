package org.company;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ServerGUI extends JFrame {

    private JButton startStopButton;
    private JTextArea logTextArea;
    private Server server;
    private boolean isServerRunning = false;
    private JSpinner portSpinner;

    public ServerGUI() {
        initializeUI();
        setTitle("Chat Server Control Panel");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void initializeUI() {
        logTextArea = new JTextArea();
        logTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logTextArea);
        add(scrollPane, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel();
        controlPanel.add(new JLabel("Enter port number:"));
        SpinnerNumberModel portModel = new SpinnerNumberModel(8080, 0, 65535, 1);
        portSpinner = new JSpinner(portModel);
        controlPanel.add(portSpinner);

        startStopButton = new JButton("Start Server");
        startStopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isServerRunning) {
                    int port = (int) portSpinner.getValue();
                    startServer(port);
                } else {
                    stopServer();
                }
            }
        });
        controlPanel.add(startStopButton);
        add(controlPanel, BorderLayout.SOUTH);
    }

    private void startServer(int port) {
        if (server == null || !server.isRunning()) {
            server = new Server(port);
            new Thread(() -> server.start()).start();
            startStopButton.setText("Stop Server");
            logTextArea.append("Server started on port: " + port + "\n");
            isServerRunning = true;
        }
    }

    private void stopServer() {
        if (server != null) {
            server.stop();
            startStopButton.setText("Start Server");
            logTextArea.append("Server stopped.\n");
            isServerRunning = false;
        }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ServerGUI());
    }

    public void appendLog(String message) {
        SwingUtilities.invokeLater(() -> logTextArea.append(message + "\n"));
    }
}
