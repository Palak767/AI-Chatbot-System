# AI Chatbot System (Java GUI + Servlet)

## Project Overview
This project is a Java-based AI Chatbot System developed as a college assignment.
It uses a Swing-based GUI frontend and a Servlet-based backend to communicate
with an AI model using REST APIs.

## Technologies Used
- Java (Swing for GUI)
- Java Servlet (Backend)
- HTTP & JSON
- Gemini AI API
- Apache Tomcat Server

## System Architecture
User Interface (Swing)
        ↓
HTTP POST Request (JSON)
        ↓
ChatServlet (Backend)
        ↓
ChatProxyServer
        ↓
Gemini AI API
        ↓
JSON Response back to GUI

## Core Features
- Interactive AI Chat Interface
- Backend Servlet Processing
- JSON-based Communication
- Input Validation & Error Handling
- Configurable AI Persona
- Clean Modular Code Structure

## Error Handling & Validation
- Empty input validation
- Message length restriction
- Backend exception handling
- Safe JSON escaping

## How to Run the Project
1. Deploy the project on Apache Tomcat
2. Start the Tomcat server
3. Run `AIChatbotSystem.java`
4. Enter messages in the chat interface

## Innovation
The project integrates an AI-powered chatbot with a Java Swing interface using servlet-based backend communication, demonstrating real-world client-server architecture.