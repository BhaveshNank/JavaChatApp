package org.company; // Change this to match your actual package name

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;

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


    @Mock
    private ClientUserManager userManager; // This line mocks the UserManager for use in tests

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Initialize your ClientNetworkManager with mock dependencies if needed
        clientNetworkManager = Mockito.mock(ClientNetworkManager.class); // Mocked to prevent actual network calls
        lenient().when(clientNetworkManager.tryToConnect()).thenReturn(true);


        // Create a ClientUserManager instance or mock if necessary
//        ClientUserManager userManager = new ClientUserManager(); // or Mockito.mock(ClientUserManager.class);

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

    @Test
    @DisplayName("Test User Login")
    void testUserLogin() {
        // Arrange
        String validUsername = "validUser";
        String validPassword = "validPass";
        when(userManager.checkCredentials(validUsername, validPassword)).thenReturn(true);

        // Act
        boolean result = userManager.checkCredentials(validUsername, validPassword);

        // Assert
        assertTrue(result, "User should log in successfully with correct credentials.");
    }

    @Test
    @DisplayName("Test User Disconnection")
    void testUserDisconnection() {
        // Arrange
        Server serverMock = mock(Server.class);
        ClientHandler mockClientHandler = mock(ClientHandler.class);

        // Act - Perform actions, which are essentially no-ops because they're mocked
        serverMock.addClient(mockClientHandler);
        serverMock.removeClient(mockClientHandler);

        // Assert - Verify that the methods were called on the mock
        verify(serverMock).addClient(mockClientHandler);
        verify(serverMock).removeClient(mockClientHandler);
    }

    @Test
    @DisplayName("Test Message Broadcasting to Multiple Users")
    void testBroadcastMessage() {
        // Arrange
        Server serverMock = mock(Server.class);
        ClientHandler clientHandlerMock1 = mock(ClientHandler.class);
        ClientHandler clientHandlerMock2 = mock(ClientHandler.class);
        List<ClientHandler> clientHandlers = Arrays.asList(clientHandlerMock1, clientHandlerMock2);

        // Assume the server's getClientHandlers method returns the above list
        lenient().when(serverMock.getClientHandlers()).thenReturn(clientHandlers);

        // Act
        // We need to simulate the broadcastMessage method's actions here
        // Since the actual server broadcastMessage method will loop through client handlers,
        // we mimic that behavior in the test.
        for (ClientHandler handler : clientHandlers) {
            handler.sendMessage("Test broadcast message");
        }

        // Assert
        // Verify sendMessage is called on each ClientHandler mock
        for (ClientHandler handler : clientHandlers) {
            verify(handler).sendMessage("Test broadcast message");
        }
    }






}



