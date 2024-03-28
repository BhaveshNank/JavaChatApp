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
    private JButton quitButton;
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
        frame = new JFrame(username);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        messageArea = new JTextArea(20, 40);
        messageArea.setEditable(false);
        JScrollPane messageScrollPane = new JScrollPane(messageArea);

        inputField = new JTextField(40);
        userModel = new DefaultListModel<>();
        userList = new JList<>(userModel);
        JScrollPane userScrollPane = new JScrollPane(userList);

        sendButton = new JButton("Send");
        quitButton = new JButton("Quit");
//        connectButton = new JButton("Connect");

        quitButton.addActionListener(e -> {
            // Logic to handle client shutdown
            networkManager.disconnect(); // Assuming you have a method to disconnect
            frame.dispose(); // Close the GUI assuming chatClientUI is your JFrame or holds your JFrame
            System.exit(0); // Terminate the application
        });
        sendButton.addActionListener(this::sendMessageAction);




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
        buttonPanel.add(quitButton);
        eastPanel.add(buttonPanel, BorderLayout.SOUTH);

        frame.add(eastPanel, BorderLayout.EAST);

        frame.setSize(new Dimension(300, 400));
        frame.setResizable(false);
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
        SwingUtilities.invokeLater(() -> {
            messageArea.append(message + "\n");
            // Check if the message is about coordinator status
            if (message.endsWith("is the coordinator") || message.startsWith("You are now the coordinator")) {
                // Display the message differently or trigger any additional UI changes
                JOptionPane.showMessageDialog(frame, message);
            }
        });
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
