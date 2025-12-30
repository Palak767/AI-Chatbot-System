import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;


@WebServlet("/chat")
public class ChatServlet extends HttpServlet {

    private ChatProxyServer chatServer;

    @Override
    public void init() throws ServletException {
        chatServer = new ChatProxyServer();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try {
            StringBuilder jsonBody = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;

            while ((line = reader.readLine()) != null) {
                jsonBody.append(line);
            }

            String userMessage = extractMessage(jsonBody.toString());

            // Validation
            if (userMessage == null || userMessage.trim().isEmpty()) {
                out.print("{\"error\":\"Message cannot be empty\"}");
                return;
            }

            if (userMessage.length() > 500) {
                out.print("{\"error\":\"Message too long\"}");
                return;
            }

            String botReply = chatServer.processUserMessage(userMessage);
            out.print("{\"reply\":\"" + escape(botReply) + "\"}");

        } catch (Exception e) {
            out.print("{\"error\":\"Server error occurred\"}");
        }
    }

    private String extractMessage(String json) {
        // very simple JSON parsing to avoid extra libraries
        if (json.contains("message")) {
            return json.replaceAll(".*\"message\"\\s*:\\s*\"(.*?)\".*", "$1");
        }
        return null;
    }

    private String escape(String text) {
        return text.replace("\"", "\\\"");
    }
}
