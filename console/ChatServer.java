import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class ChatServer {

    private static final int PORT = 12345;
    private static List<ClientHandler> clients = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("Chat Server is running...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket);

                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clients.add(clientHandler);
                Thread clientThread = new Thread(clientHandler);
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket clientSocket;
        private PrintWriter writer;
        private String username;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try {
                Scanner scanner = new Scanner(clientSocket.getInputStream());
                writer = new PrintWriter(clientSocket.getOutputStream(), true);

                writer.println("Enter your name:");
                if (scanner.hasNextLine()) {
                    username = scanner.nextLine();
                    broadcastMessage(username + " has joined the chat.");
                }

                while (true) {
                    if (scanner.hasNextLine()) {
                        String message = scanner.nextLine();
                        broadcastMessage(username + ": " + message);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (username != null) {
                    clients.remove(this);
                    broadcastMessage(username + " has left the chat.");
                }
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void broadcastMessage(String message) {
            String formattedMessage = getTimeStamp() + " " + message;
            System.out.println(formattedMessage);
            for (ClientHandler client : clients) {
                client.writer.println(formattedMessage);
            }
        }

        private String getTimeStamp() {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            return "[" + sdf.format(new Date()) + "]";
        }
    }
}