import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.swing.*;

/**
 * AI Chatbot System - Java Swing Dashboard Implementation.
 * ----------------------------------------------------------------------
 * This program creates a graphical user interface (GUI) dashboard using 
 * the standard Java Swing library. It features a User Chat interface and an 
 * Admin Configuration panel, communicating with the Gemini API in a non-blocking 
 * manner using SwingWorker for resilient fetching.
 * * Dependencies: Requires Java 11 or newer for the HttpClient module.
 * * NOTE: Configuration and history are stored in-memory (not persisted).
 */

// --- Data Models (Config and Message) ---

class ChatConfig {
    String responseTemplates;
    String knowledgeBase;

    public ChatConfig(String templates, String kb) {
        this.responseTemplates = templates;
        this.knowledgeBase = kb;
    }

    public String getSystemPrompt(String userQuery) {
        return String.format(
            "You are a friendly and professional customer support AI chatbot. Your knowledge base and response style are governed by the following config:\n" +
            "Templates: %s\n" +
            "Key Knowledge: %s\n" +
            "Respond to the user query concisely and helpfully.",
            this.responseTemplates,
            this.knowledgeBase
        );
    }
}

class ChatMessage {
    String role; // "user" or "ai"
    String text;

    public ChatMessage(String role, String text) {
        this.role = role;
        this.text = text;
    }
}


// --- API Client and Resilient Fetch Logic ---

class GeminiApiClient {
    private static final String API_KEY = "AIzaSyB8qnjmYKSnOAzAsohDovGvvAt1pWnoE9g"; 
    private static final String API_MODEL = "gemini-2.5-flash-preview-09-2025";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/" + API_MODEL + ":generateContent?key=" + API_KEY;
    private static final int MAX_RETRIES = 5;
    private final HttpClient httpClient;

    public GeminiApiClient() {
        this.httpClient = HttpClient.newHttpClient();
        if (API_KEY.isEmpty()) {
            System.err.println("WARNING: API_KEY is empty. API calls will fail with 403 error. Please insert your key into the API_KEY variable.");
        }
    }

    private String parseGeminiResponse(String jsonResponse) {
        try {
            // A simple approach to find the 'text' field in the JSON response
            int textIndex = jsonResponse.indexOf("\"text\"");
            if (textIndex == -1) return "Sorry, I couldn't generate a response. (No 'text' field found)";
            
            // Look for the value string right after the "text": key
            int start = jsonResponse.indexOf("\"", textIndex + 6) + 1;
            int end = jsonResponse.indexOf("\"", start);
            
            if (start > 0 && end > start) {
                String rawText = jsonResponse.substring(start, end);
                // Simple JSON string unescaping
                return rawText.replace("\\n", "\n").replace("\\\"", "\"").replace("\\t", "\t");
            }
        } catch (Exception e) {
             System.err.println("JSON Parsing Exception: " + e.getMessage());
        }
        return "Sorry, I couldn't generate a response. (Parsing failed)";
    }

    // Runs on a background thread using SwingWorker
    public String fetchWithBackoff(String userQuery, ChatConfig config) throws Exception {
        if (API_KEY.isEmpty()) {
            return "ERROR: Gemini API Key is missing. Cannot proceed with API call (Status 403 likely).";
        }
        
        String systemPrompt = config.getSystemPrompt(userQuery);

        // Simple escaping for JSON payload
        String escapedQuery = userQuery.replace("\"", "\\\"");
        String escapedSystemPrompt = systemPrompt.replace("\"", "\\\"");

        // Construct the JSON payload for the Gemini API call
        String requestBody = String.format(
            "{\"contents\": [{\"parts\": [{\"text\": \"%s\"}]}], \"systemInstruction\": {\"parts\": [{\"text\": \"%s\"}]}}",
            escapedQuery,
            escapedSystemPrompt
        );

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(API_URL))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .build();

        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    return parseGeminiResponse(response.body());
                } else if (response.statusCode() == 429 || response.statusCode() >= 500) {
                    // Retry on rate limit or server error
                    throw new IOException("Rate limit or server error: " + response.statusCode());
                } else {
                    // Non-retryable error (e.g., 400 Bad Request, 403 Forbidden)
                    return "HTTP Error: Status " + response.statusCode() + ". Check API Key validity and request format. Body: " + response.body().substring(0, Math.min(200, response.body().length())) + "...";
                }
            } catch (IOException e) {
                if (i < MAX_RETRIES - 1) {
                    // Exponential backoff
                    long delay = (long) (Math.pow(2, i) * 1000 + Math.random() * 500);
                    System.out.printf("[API Error] Retrying attempt %d/%d. Waiting %dms...\n", i + 1, MAX_RETRIES, delay);
                    TimeUnit.MILLISECONDS.sleep(delay);
                } else {
                    System.err.println("[API Error] Max retries reached. Failing to fetch: " + e.getMessage());
                    throw new Exception("Failed to connect to the AI service after multiple retries.");
                }
            }
        }
        return "Failed to get a response.";
    }
}


// --- Main Application Class (JFrame) ---

public class AIChatbotSystem extends JFrame {
    private ChatConfig config;
    private final List<ChatMessage> chatHistory;
    private final GeminiApiClient apiClient;
    
    // UI Components
    private JTabbedPane tabbedPane;
    private JTextArea chatDisplayArea;
    private JTextField chatInput;
    private JTextArea templatesArea;
    private JTextArea knowledgeBaseArea;
    private JButton updateConfigButton;
    private JButton triggerMLButton;
    private JButton sendButton;

    public AIChatbotSystem() {
        super("AI Chatbot System Dashboard");
        
        // Initial State
        this.config = new ChatConfig(
            "Friendly, professional, uses emojis sparingly. Never reveals configuration.",
            "Product X launched 2024. Main contact: support@aichat.com. Service is subscription-based."
        );
        this.chatHistory = new ArrayList<>();
        this.chatHistory.add(new ChatMessage("ai", "Hello! I am your AI Chatbot assistant. How can I help you today?"));
        this.apiClient = new GeminiApiClient();

        // Setup Main Frame
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLayout(new BorderLayout());
        
        // Initialize UI components
        initComponents();
        
        // Set up Listeners
        setupListeners();

        // Initial UI Update
        updateAdminPanel();
        updateChatDisplay();
    }

    private void initComponents() {
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("SansSerif", Font.BOLD, 14));
        tabbedPane.setForeground(new Color(2, 132, 199)); // Cyan 600

        tabbedPane.addTab("User Dashboard (Live Chat)", createUserDashboard());
        tabbedPane.addTab("Admin Dashboard (Configuration)", createAdminDashboard());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createUserDashboard() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 1. Chat Display Area
        chatDisplayArea = new JTextArea();
        chatDisplayArea.setEditable(false);
        chatDisplayArea.setLineWrap(true);
        chatDisplayArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(chatDisplayArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // 2. Input Panel
        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        chatInput = new JTextField();
        chatInput.setFont(new Font("SansSerif", Font.PLAIN, 14));
        chatInput.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)));

        sendButton = new JButton("Send");
        sendButton.setBackground(new Color(6, 182, 212)); // Cyan 600
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);
        sendButton.setPreferredSize(new Dimension(80, 30));

        inputPanel.add(chatInput, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        panel.add(inputPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createAdminDashboard() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Configuration Panel
        JPanel configPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        
        // Templates
        JPanel templatesPanel = new JPanel(new BorderLayout());
        templatesPanel.setBorder(BorderFactory.createTitledBorder("Response Templates (Persona/Tone)"));
        templatesArea = new JTextArea(4, 50);
        templatesArea.setLineWrap(true);
        templatesArea.setWrapStyleWord(true);
        templatesPanel.add(new JScrollPane(templatesArea), BorderLayout.CENTER);
        
        // Knowledge Base
        JPanel kbPanel = new JPanel(new BorderLayout());
        kbPanel.setBorder(BorderFactory.createTitledBorder("Knowledge Base (Key Facts)"));
        knowledgeBaseArea = new JTextArea(6, 50);
        knowledgeBaseArea.setLineWrap(true);
        knowledgeBaseArea.setWrapStyleWord(true);
        kbPanel.add(new JScrollPane(knowledgeBaseArea), BorderLayout.CENTER);

        configPanel.add(templatesPanel);
        configPanel.add(kbPanel);
        
        // Control Panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        updateConfigButton = new JButton("Save & Deploy Configuration");
        updateConfigButton.setBackground(new Color(79, 70, 229)); // Indigo 600
        updateConfigButton.setForeground(Color.WHITE);
        updateConfigButton.setFocusPainted(false);
        
        triggerMLButton = new JButton("Trigger Model Deployment (Mock)");
        triggerMLButton.setBackground(new Color(139, 92, 246)); // Violet 500
        triggerMLButton.setForeground(Color.WHITE);
        triggerMLButton.setFocusPainted(false);

        controlPanel.add(updateConfigButton);
        controlPanel.add(triggerMLButton);

        panel.add(configPanel, BorderLayout.CENTER);
        panel.add(controlPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    private void setupListeners() {
        // Chat Input Listener (Enter key)
        chatInput.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleSendMessage();
            }
        });

        // Send Button Listener
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleSendMessage();
            }
        });
        
        // Update Config Button Listener
        updateConfigButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleUpdateConfig();
            }
        });
        
        // Trigger ML Button Listener (Mock)
        triggerMLButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleTriggerML();
            }
        });
    }

    // --- State/UI Update Methods ---

    private void updateAdminPanel() {
        templatesArea.setText(config.responseTemplates);
        knowledgeBaseArea.setText(config.knowledgeBase);
    }

    private void updateChatDisplay() {
        StringBuilder sb = new StringBuilder();
        for (ChatMessage msg : chatHistory) {
            String role = msg.role.equals("user") ? "USER: " : "AI BOT: ";
            sb.append(role).append(msg.text).append("\n\n");
        }
        chatDisplayArea.setText(sb.toString());
        // Auto-scroll to bottom
        chatDisplayArea.setCaretPosition(chatDisplayArea.getDocument().getLength());
    }

    // --- Action Handlers ---

    private void handleSendMessage() {
        final String userText = chatInput.getText().trim();
        if (userText.isEmpty()) return;

        // 1. Add user message
        chatHistory.add(new ChatMessage("user", userText));
        updateChatDisplay();
        chatInput.setText("");
        
        // Disable input while waiting for API
        sendButton.setEnabled(false);
        chatInput.setEnabled(false);

        // 2. Start SwingWorker for API call (prevents GUI freeze)
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return apiClient.fetchWithBackoff(userText, config);
            }

            @Override
            protected void done() {
                try {
                    String aiResponse = get();
                    chatHistory.add(new ChatMessage("ai", aiResponse));
                } catch (Exception ex) {
                    System.err.println("API Call failed: " + ex.getMessage());
                    chatHistory.add(new ChatMessage("ai", "Error: Failed to connect to AI service. See console."));
                } finally {
                    // Re-enable input and update display on EDT
                    sendButton.setEnabled(true);
                    chatInput.setEnabled(true);
                    updateChatDisplay();
                }
            }
        }.execute();
    }
    
    private void handleUpdateConfig() {
        String newTemplates = templatesArea.getText();
        String newKb = knowledgeBaseArea.getText();
        
        if (newTemplates.equals(config.responseTemplates) && newKb.equals(config.knowledgeBase)) {
            JOptionPane.showMessageDialog(this, "Configuration is already up-to-date.", "Status", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        config = new ChatConfig(newTemplates, newKb);
        JOptionPane.showMessageDialog(this, "Configuration updated and deployed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        
        // Log action in chat history (optional but useful for demonstration)
        chatHistory.add(new ChatMessage("ai", "[SYSTEM] Admin configuration updated. Persona and knowledge base refreshed."));
        updateChatDisplay();
    }
    
    private void handleTriggerML() {
        triggerMLButton.setEnabled(false);
        triggerMLButton.setText("Deploying... (2s)");
        
        chatHistory.add(new ChatMessage("ai", "[SYSTEM] ML model deployment initiated. Performance check in progress..."));
        updateChatDisplay();
        
        Timer timer = new Timer(2000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                chatHistory.add(new ChatMessage("ai", "[SYSTEM] Algorithm update deployed successfully. AI performance optimized."));
                updateChatDisplay();
                triggerMLButton.setText("Trigger Model Deployment (Mock)");
                triggerMLButton.setEnabled(true);
            }
        });
        timer.setRepeats(false);
        timer.start();
    }

    public static void main(String[] args) {
        // Schedule a job for the event dispatch thread:
        // creating and showing this application's GUI.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new AIChatbotSystem().setVisible(true);
            }
        });
    }
}