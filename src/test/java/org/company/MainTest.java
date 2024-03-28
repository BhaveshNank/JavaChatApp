package org.company; // Change this to match your actual package name

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.*;
import java.net.Socket;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MainTest {

    // Assuming ChatClientUI and ClientNetworkManager are the main components of your chat client

    private static ChatClientUI chatClientUI;
    private static ClientNetworkManager clientNetworkManager;
    private static final int PORT = 8080;
    private static final String HOST = "127.0.0.1";
    private static final String USERNAME = "User1";

    @BeforeAll
    public static void setUp() {
        // Initialize your ClientNetworkManager with mock dependencies if needed
        clientNetworkManager = Mockito.mock(ClientNetworkManager.class); // Mocked to prevent actual network calls
        Mockito.when(clientNetworkManager.tryToConnect()).thenReturn(true); // Assume connection always succeeds

        // Create a ClientUserManager instance or mock if necessary
        ClientUserManager userManager = new ClientUserManager(); // or Mockito.mock(ClientUserManager.class);

        // Initialize your ChatClientUI
        chatClientUI = new ChatClientUI(clientNetworkManager, userManager, USERNAME);

        // Mock other dependencies and behaviors as required
    }

    @Test
    @DisplayName("GUI Display test")
    void testGuiDisplayed() {
        // Assuming 'getFrame()' is the method to get the JFrame from ChatClientUI
        assertNotNull(chatClientUI.getFrame());
    }

    @Test
    @DisplayName("Test if Connection is Successful")
    void testConnection() {
        // Assuming 'tryToConnect()' returns true if connection is successful
        assertTrue(clientNetworkManager.tryToConnect(), "The connection should be successful");
    }


}



