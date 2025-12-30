import java.io.*;
import java.net.*;
import java.net.http.*;
import java.util.concurrent.*;

/**
 * ChatProxyServer - The Backend Component for Review 2.
 * This server acts as a middleware between the Swing UI and the Gemini API.
 */
public class ChatProxyServer {
    private static final int PORT = 8080;
    private static final String API_KEY = "AIzaSyBG-M1M2o7MnYAoiA9wM2TVBXkJ89zWVvM"; // Gemini API Key goes here
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + API_KEY;

    public static void main(String[] args) {
        // Using a ThreadPool to handle multiple client requests if needed
        ExecutorService executor = Executors.newCachedThreadPool();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("==========================================");
            System.out.println("   BACKEND SERVER STARTED ON PORT " + PORT);
            System.out.println("   Waiting for Chatbot UI to connect...");
            System.out.println("==========================================");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("[SERVER] New connection from: " + clientSocket.getInetAddress());
                
                // Handle each request in a separate thread
                executor.execute(() -> handleClient(clientSocket));
            }
        } catch (IOException e) {
            System.err.println("[SERVER ERROR] Could not start server: " + e.getMessage());
        }
    }

    private static void handleClient(Socket socket) {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            // 1. Read the prompt sent by the Swing UI
            String userPrompt = in.readLine();
            System.out.println("[SERVER] Received prompt: " + userPrompt);

            // 2. Forward to Gemini API
            String aiResponse = callGeminiAPI(userPrompt);

            // 3. Send the AI response back to the Swing UI
            out.println(aiResponse);
            System.out.println("[SERVER] Response sent back to UI.");

        } catch (IOException e) {
            System.err.println("[SERVER ERROR] Client handling failed: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static String callGeminiAPI(String prompt) {
        try {
            // Construct JSON Payload
            String jsonBody = "{\"contents\": [{\"parts\": [{\"text\": \"" + prompt.replace("\"", "\\\"") + "\"}]}]}";

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            // Simple parsing (Note: In a real app, use a JSON library like Jackson or Gson)
            // This regex-style search finds the text between "text": " and the next "
            String body = response.body();
            int start = body.indexOf("\"text\": \"") + 9;
            int end = body.indexOf("\"", start);
            
            if (start > 8 && end > start) {
                return body.substring(start, end).replace("\\n", "\n");
            }
            return "Error: AI could not process the request. Status: " + response.statusCode();
            
        } catch (Exception e) {
            return "Server-side Error: " + e.getMessage();
        }
    }

    public String processUserMessage(String userMessage) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'processUserMessage'");
    }
}