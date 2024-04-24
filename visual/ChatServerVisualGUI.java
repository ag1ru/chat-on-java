import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class ChatServerVisualGUI {

    public static final int PORT = 12345;

    private List<ClientHandler> clients = new ArrayList<>();
    private boolean running = false;

    private JFrame frame;
    private JTextArea consoleTextArea;
    private JButton startButton;
    private JButton stopButton;

    public ChatServerVisualGUI() {
        frame = new JFrame("Chat Server");
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel controlPanel = new JPanel();
        startButton = new JButton("Start Server");
        stopButton = new JButton("Stop Server");

        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                startServer();
            }
        });

        stopButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                stopServer();
            }
        });

        controlPanel.add(startButton);
        controlPanel.add(stopButton);

        consoleTextArea = new JTextArea();
        consoleTextArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(consoleTextArea);

        frame.add(controlPanel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);

        frame.setVisible(true);
    }

    private synchronized void startServer() {
        if (running) {
            appendToConsole("Server is already running.");
            return;
        }

        running = true;

        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(PORT)) {
                appendToConsole("Chat Server is running on port " + PORT);

                while (running) {
                    Socket clientSocket = serverSocket.accept();
                    appendToConsole("New client connected: " + clientSocket);

                    ClientHandler clientHandler = new ClientHandler(clientSocket);
                    clients.add(clientHandler);
                    clientHandler.start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private synchronized void stopServer() {
        if (!running) {
            appendToConsole("Server is not running.");
            return;
        }

        running = false;

        try {
            for (ClientHandler client : clients) {
                client.close();
            }
            clients.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }

        appendToConsole("Chat Server stopped.");
    }

    private void appendToConsole(String message) {
        SwingUtilities.invokeLater(() -> {
            String timeStamp = getTimeStamp();
            consoleTextArea.append("[" + timeStamp + "] " + message + "\n");
        });
    }

    private String getTimeStamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(new Date());
    }

    private class ClientHandler extends Thread {
        private Socket clientSocket;
        private PrintWriter writer;
        private String username;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        public void run() {
            try {
                Scanner scanner = new Scanner(clientSocket.getInputStream());
                writer = new PrintWriter(clientSocket.getOutputStream(), true);

                writer.println("Enter your name:");
                if (scanner.hasNextLine()) {
                    username = scanner.nextLine();
                    broadcastMessage(username + " has joined the chat.");
                }

                while (running) {
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
            appendToConsole(formattedMessage);
            for (ClientHandler client : clients) {
                client.writer.println(formattedMessage);
            }
        }

        public void close() throws IOException {
            if (clientSocket != null) {
                clientSocket.close();
            }
            if (writer != null) {
                writer.close();
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ChatServerVisualGUI();
        });
    }
}