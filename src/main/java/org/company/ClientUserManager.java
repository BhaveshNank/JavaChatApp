package org.company;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ClientUserManager {
    private final String filename = "usernames.txt";

    public List<String> readUsernames() {
        List<String> usernames = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                usernames.add(line.split(",")[0].trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
            // Handle read error
        }
        return usernames;
    }

    // Include methods for writing and removing usernames as needed...
    public void addUsername(String username) {
        // Append the username to the file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename, true))) {
            writer.write(username + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeUsername(String username) {
        // Read current usernames, filter out the one to remove, and rewrite the file
        List<String> usernames = readUsernames();
        usernames.remove(username);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            for (String user : usernames) {
                writer.write(user + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
