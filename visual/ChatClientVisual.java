import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatClientVisual {

    private JFrame frame;
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private JButton exitButton;

    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;

    private String serverAddress = "localhost";
    private int serverPort = 12345;

    public ChatClientVisual() {
        frame = new JFrame("Chat Client");
        frame.setSize(600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Панель для компонентов интерфейса
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        // Область чата
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("Arial", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(chatArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout());

        messageField = new JTextField();
        messageField.setFont(new Font("Arial", Font.PLAIN, 14));
        messageField.addActionListener(e -> sendMessage());
        inputPanel.add(messageField, BorderLayout.CENTER);

        sendButton = new JButton("Send");
        sendButton.setFont(new Font("Arial", Font.BOLD, 14));
        sendButton.addActionListener(e -> sendMessage());
        inputPanel.add(sendButton, BorderLayout.EAST);

        exitButton = new JButton("Exit");
        exitButton.setFont(new Font("Arial", Font.PLAIN, 14));
        exitButton.addActionListener(e -> exitChat());
        inputPanel.add(exitButton, BorderLayout.WEST);

        panel.add(inputPanel, BorderLayout.SOUTH);

        frame.add(panel);
        frame.setVisible(true);

        connectToServer();
    }

    private void connectToServer() {
        try {
            socket = new Socket(serverAddress, serverPort);
            writer = new PrintWriter(socket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            new Thread(() -> {
                try {
                    while (true) {
                        String message = reader.readLine();
                        if (message != null) {
                            appendToChat(message);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    closeResources();
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
            closeResources();
        }
    }

    private void sendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty()) {
            writer.println(message);
            messageField.setText("");
        }
    }

    private void appendToChat(String message) {
        SwingUtilities.invokeLater(() -> {
            String timeStamp = getTimeStamp();
            chatArea.append("[" + timeStamp + "] " + message + "\n");
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }

    private String getTimeStamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(new Date());
    }

    private void exitChat() {
        closeResources();
        frame.dispose();
    }

    private void closeResources() {
        try {
            if (writer != null) {
                writer.close();
            }
            if (reader != null) {
                reader.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            new ChatClientVisual();
        });
    }
}