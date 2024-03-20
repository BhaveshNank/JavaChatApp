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

    private ClientNetworkManager networkManager;
    private ClientUserManager userManager;

    public ChatClientUI(ClientNetworkManager networkManager, ClientUserManager userManager) {
        this.networkManager = networkManager;
        this.userManager = userManager;
        initializeUI();
    }

    private void initializeUI() {
        frame = new JFrame("Chat Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout()); // Set a BorderLayout for the JFrame

        messageArea = new JTextArea(30, 50); // Increased width to better fill space
        messageArea.setEditable(false);
        JScrollPane messageScrollPane = new JScrollPane(messageArea); // Put the text area in a scroll pane

        inputField = new JTextField(50); // Adjust to match the width of message area
        userModel = new DefaultListModel<>();
        userList = new JList<>(userModel);
        JScrollPane userScrollPane = new JScrollPane(userList); // Put the user list in a scroll pane

        sendButton = new JButton("Send");
        refreshButton = new JButton("Refresh");

        sendButton.addActionListener(this::sendMessageAction);
        refreshButton.addActionListener(e -> {
            userModel.clear();
            userModel.addAll(userManager.readUsernames());
        });

        // Layout components
        frame.add(messageScrollPane, BorderLayout.CENTER);
        frame.add(inputField, BorderLayout.SOUTH);

        // Panel for user list and buttons
        JPanel eastPanel = new JPanel();
        eastPanel.setLayout(new BorderLayout());
        eastPanel.add(userScrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(sendButton);
        buttonPanel.add(refreshButton);
        eastPanel.add(buttonPanel, BorderLayout.SOUTH);

        frame.add(eastPanel, BorderLayout.EAST);

        frame.setMinimumSize(new Dimension(800, 600)); // Ensure the minimum size is enough to display all components
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void sendMessageAction(ActionEvent e) {
        String message = inputField.getText();
        networkManager.sendMessage(message);
        inputField.setText("");
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
        sendButton.setEnabled(true);
    }

    public JFrame getFrame() {
        return frame;
    }
}
