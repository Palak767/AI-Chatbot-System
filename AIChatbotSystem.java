// Explicitly adding this to fix the error
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.TimeUnit;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * MODERN AI CHATBOT DASHBOARD (V2.1)
 * ---------------------------------------------------------
 * Fixes:
 * - Resolved 'EdgeInsets' symbol error (Added java.awt.Insets)
 * - Improved sidebar styling and text colors
 * - Added a cleaner "Main Hub" aesthetic
 */

class Style {
    public static final Color PRIMARY = new Color(79, 70, 229);   // Indigo 600
    public static final Color SECONDARY = new Color(15, 23, 42);  // Slate 900
    public static final Color ACCENT = new Color(16, 185, 129);   // Emerald 500
    public static final Color BG_LIGHT = new Color(248, 250, 252); // Slate 50
    public static final Color TEXT_MAIN = new Color(30, 41, 59);   // Slate 800
    public static final Color BORDER = new Color(226, 232, 240);
    public static final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 18);
    public static final Font MAIN_FONT = new Font("SansSerif", Font.PLAIN, 14);
}

class ChatConfig {
    String persona = "A professional and friendly AI assistant.";
    String knowledgeBase = "General knowledge enabled.";

    public String getPrompt() {
        return "Identity: " + persona + "\nKnowledge: " + knowledgeBase + 
               "\nRule: Answer any question flexibly.";
    }
}

class ModernButton extends JButton {
    public ModernButton(String text, Color bg) {
        super(text);
        setBackground(bg);
        setForeground(Color.WHITE);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(false);
        setFont(new Font("SansSerif", Font.BOLD, 13));
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setMargin(new Insets(10, 20, 10, 20)); // Fixed: Using java.awt.Insets
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
        g2.dispose();
        super.paintComponent(g);
    }
}

public class AIChatbotSystem extends JFrame {
    private ChatConfig config = new ChatConfig();
    private JTextArea chatArea;
    private JTextField inputField;
    private JTextArea personaArea;
    private JTextArea kbArea;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public AIChatbotSystem() {
        setTitle("AI Intelligence Hub v2.1");
        setSize(1050, 720);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setBackground(Style.BG_LIGHT);

        initComponents();
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        // Sidebar Setup
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.LEFT);
        tabs.setFont(new Font("SansSerif", Font.BOLD, 13));
        tabs.setBackground(Style.SECONDARY);
        tabs.setForeground(Style.TEXT_MAIN);

        // Sidebar Tabs with Icons
        tabs.addTab("<html><div style='padding: 15px 10px; width: 100px;'>💬 Chat</div></html>", createChatPanel());
        tabs.addTab("<html><div style='padding: 15px 10px; width: 100px;'>⚙️ Config</div></html>", createAdminPanel());

        add(tabs, BorderLayout.CENTER);
    }

    private JPanel createChatPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(Style.BG_LIGHT);
        panel.setBorder(new EmptyBorder(25, 25, 25, 25));

        // Header Title
        JLabel header = new JLabel("AI Connection Center");
        header.setFont(new Font("SansSerif", Font.BOLD, 24));
        header.setForeground(Style.TEXT_MAIN);
        panel.add(header, BorderLayout.NORTH);

        // Chat Display Area
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        chatArea.setBackground(Color.WHITE);
        chatArea.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JScrollPane scroll = new JScrollPane(chatArea);
        scroll.setBorder(BorderFactory.createLineBorder(Style.BORDER, 1));
        panel.add(scroll, BorderLayout.CENTER);

        // Input Bar
        JPanel footer = new JPanel(new BorderLayout(15, 0));
        footer.setOpaque(false);
        
        inputField = new JTextField();
        inputField.setFont(Style.MAIN_FONT);
        inputField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Style.BORDER),
            BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));

        ModernButton sendBtn = new ModernButton("Send Message", Style.PRIMARY);
        sendBtn.addActionListener(e -> handleAction());
        inputField.addActionListener(e -> handleAction());

        footer.add(inputField, BorderLayout.CENTER);
        footer.add(sendBtn, BorderLayout.EAST);
        panel.add(footer, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createAdminPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(Style.BG_LIGHT);
        panel.setBorder(new EmptyBorder(30, 30, 30, 30));

        JLabel header = new JLabel("System Logic Configuration");
        header.setFont(Style.TITLE_FONT);
        panel.add(header, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridLayout(2, 1, 25, 25));
        center.setOpaque(false);

        personaArea = createStyledTextArea("AI PERSONA (Behave as...)");
        personaArea.setText(config.persona);
        kbArea = createStyledTextArea("KNOWLEDGE BASE (Known facts...)");
        kbArea.setText(config.knowledgeBase);

        center.add(new JScrollPane(personaArea));
        center.add(new JScrollPane(kbArea));
        panel.add(center, BorderLayout.CENTER);

        ModernButton saveBtn = new ModernButton("🚀 Update System Brain", Style.ACCENT);
        saveBtn.addActionListener(e -> {
            config.persona = personaArea.getText();
            config.knowledgeBase = kbArea.getText();
            JOptionPane.showMessageDialog(this, "System Logic Updated!", "Success", JOptionPane.INFORMATION_MESSAGE);
        });
        panel.add(saveBtn, BorderLayout.SOUTH);

        return panel;
    }

    private JTextArea createStyledTextArea(String title) {
        JTextArea area = new JTextArea();
        area.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Style.BORDER), title));
        area.setFont(Style.MAIN_FONT);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setMargin(new Insets(10, 10, 10, 10));
        return area;
    }

    private void handleAction() {
        String query = inputField.getText().trim();
        if (query.isEmpty()) return;

        chatArea.append("YOU: " + query + "\n");
        inputField.setText("");
        chatArea.append("SYSTEM: [Generating Response...]\n");

        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return sendMessageToServlet(query);
            }

            @Override
            protected void done() {
                try {
                    String response = get();
                    String text = chatArea.getText();
                    chatArea.setText(text.replace("SYSTEM: [Generating Response...]\n", ""));
                    chatArea.append("AI BOT: " + response + "\n\n");
                } catch (Exception e) {
                    chatArea.append("SYSTEM ERROR: Failed to communicate with AI model.\n");
                }
            }
        }.execute();
    }

    private String fetchFromAI(String query) throws Exception {
        String API_KEY = "AIzaSyBG-M1M2o7MnYAoiA9wM2TVBXkJ89zWVvM"; // Insert Key Here
        if(API_KEY.isEmpty()) return "Please add your Gemini API Key in the code.";

        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-preview-09-2025:generateContent?key=" + API_KEY;
        String jsonPayload = "{\"contents\": [{\"parts\": [{\"text\": \"" + query.replace("\"", "\\\"") + "\"}]}], " +
                             "\"systemInstruction\": {\"parts\": [{\"text\": \"" + config.getPrompt().replace("\"", "\\\"") + "\"}]}}";

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        String body = response.body();
        try {
            int start = body.indexOf("\"text\": \"") + 9;
            int end = body.indexOf("\"", start);
            return body.substring(start, end).replace("\\n", "\n");
        } catch (Exception e) {
            return "Error parsing response: " + body;
        }
    }

    private String sendMessageToServlet(String message) {
    try {
        URL url = new URL("http://localhost:8080/AI-CHATBOT-SYSTEM/chat");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        String jsonInput = "{\"message\":\"" + message.replace("\"", "\\\"") + "\"}";
        conn.getOutputStream().write(jsonInput.getBytes());

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream())
        );

        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }

        // Extract reply from JSON
        if (response.toString().contains("reply")) {
            return response.toString()
                    .replaceAll(".*\"reply\"\\s*:\\s*\"(.*?)\".*", "$1");
        }

        return "No response from server";

    } catch (Exception e) {
        return "Unable to connect to server";
    }
}

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}
        SwingUtilities.invokeLater(() -> new AIChatbotSystem().setVisible(true));
    }
}