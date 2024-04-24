import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ChatClient {

    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             Scanner serverInput = new Scanner(socket.getInputStream());
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
             Scanner userInput = new Scanner(System.in)) {

            System.out.println("Connected to chat server. Enter your name:");
            String username = userInput.nextLine();
            writer.println(username);

            Thread serverListenerThread = new Thread(() -> {
                while (serverInput.hasNextLine()) {
                    String message = serverInput.nextLine();
                    System.out.println(message);
                }
            });
            serverListenerThread.start();

            while (true) {
                String message = userInput.nextLine();
                writer.println(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}