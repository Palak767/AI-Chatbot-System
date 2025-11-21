AI Chatbot System - Command Line Interface (CLI)

==================================================

Project Description:This is a single-file Java application designed to simulate a complete AI chatbot system dashboard. It includes a user-facing chat interface for real-time interaction and an administrative interface for configuration management and simulated model updates.

It demonstrates resilient API communication with the Google Gemini API using the built-in Java 11+ HttpClient and implements an exponential backoff strategy for handling network and rate-limiting issues.
Requirements:
.Java Development Kit (JDK) 11 or later
.Internet connectivity to communicate with the Gemini API.

API Key:The GeminiApiClient class includes a placeholder for the API key (private static final String API_KEY = "";). In a production environment, this should be loaded securely from environment variables. For this execution environment, it is designed to use the system-provided key.

Files in this Project:

1. AIChatbotSystem.java: The complete, single-file source code for the application.
2. build.sh: A simple shell script for compilation and execution (for Unix-like systems).
3. README.txt: This file.


EXECUTION INSTRUCTIONS (Linux/macOS):
1. Compile the Java file:./build.sh compile

    OR: javac AIChatbotSystem.java
2. Run the compiled application:./build.sh run

    OR: java AIChatbotSystem


EXECUTION INSTRUCTIONS (Windows Command Prompt):
1. Compile the Java file:javac AIChatbotSystem.java
2. Run the compiled application:java AIChatbotSystem

Application Features:
. User Dashboard: Start a live chat session with the AI
. Admin Dashboard: Update the AI's internal response templates and knowledge base (which affect the system prompt)
. History Viewer: See a log of all previous interactions
. Exponential Backoff: The API client automatically retries failed requests with increasing delays.