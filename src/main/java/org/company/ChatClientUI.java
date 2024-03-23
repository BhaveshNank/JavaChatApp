package org.company;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class ChatClientUI {
    private JFrame frame;
    private JTextArea messageArea;
    private JTextField inputField;
    private JList<String> userList;
    private DefaultListModel<String> userModel;
    private JButton sendButton;
    private JButton refreshButton;
//    private JButton connectButton;

    private ClientNetworkManager networkManager;
    private ClientUserManager userManager;
    private String username;

    public ChatClientUI(ClientNetworkManager networkManager, ClientUserManager userManager, String username) {
        this.networkManager = networkManager;
        this.userManager = userManager;
        this.username = username; // set the username passed from the constructor
        initializeUI(); // initialize the user interface
    }

    private void initializeUI() {
        frame = new JFrame("Chat Client - " + username);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        messageArea = new JTextArea(30, 50);
        messageArea.setEditable(false);
        JScrollPane messageScrollPane = new JScrollPane(messageArea);

        inputField = new JTextField(50);
        userModel = new DefaultListModel<>();
        userList = new JList<>(userModel);
        JScrollPane userScrollPane = new JScrollPane(userList);

        sendButton = new JButton("Send");
        refreshButton = new JButton("Refresh");
//        connectButton = new JButton("Connect");

        // Action listeners
        sendButton.addActionListener(this::sendMessageAction);
        refreshButton.addActionListener(e -> networkManager.requestUserList());

//        connectButton.addActionListener(this::connectAction);

        // Layout components
        frame.add(messageScrollPane, BorderLayout.CENTER);
        frame.add(inputField, BorderLayout.SOUTH);

        // Panel for user list and buttons
        JPanel eastPanel = new JPanel(new BorderLayout());
        eastPanel.add(userScrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
//        buttonPanel.add(connectButton);
        buttonPanel.add(sendButton);
        buttonPanel.add(refreshButton);
        eastPanel.add(buttonPanel, BorderLayout.SOUTH);

        frame.add(eastPanel, BorderLayout.EAST);

        frame.setMinimumSize(new Dimension(800, 600));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void sendMessageAction(ActionEvent e) {
        if (networkManager.isConnected()) {
            String message = inputField.getText();
            if (!message.isEmpty()) {
                networkManager.sendMessage(message);
                displayMessage("You: " + message); // This will append the message to the chat area
                inputField.setText("");
            }
        } else {
            displayMessage("Not connected to server. Please connect first.");
        }
    }


//    private void connectAction(ActionEvent e) {
//        if (!networkManager.isConnected()) {
//            boolean success = networkManager.tryToConnect();
//            if (success) {
//                connectButton.setEnabled(false); // Disable after successful connection
//                sendButton.setEnabled(true); // Enable send button after connection
//                displayMessage("Connected to the server.");
//            } else {
//                displayMessage("Could not connect to the server. Try again.");
//            }
//        }
//    }



    private void refreshUserListAction(ActionEvent e) {
        networkManager.requestUserList();
    }

    public void refreshUserList() {
        userModel.clear();
        userModel.addAll(userManager.readUsernames());
    }

    public void displayMessage(String message) {
        SwingUtilities.invokeLater(() -> messageArea.append(message + "\n"));
    }

    public void setNetworkManager(ClientNetworkManager networkManager) {
        this.networkManager = networkManager;
        // Assuming the network manager must be connected before enabling the send button.
        sendButton.setEnabled(networkManager.isConnected());
    }

    public void updateUserList(String[] usernames) {
        SwingUtilities.invokeLater(() -> {
            userModel.clear();
            for (String user : usernames) {
                userModel.addElement(user);
            }
        });
    }


    public JFrame getFrame() {
        return frame;
    }
}
