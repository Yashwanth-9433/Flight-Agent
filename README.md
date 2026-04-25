
# Agentic AI Flight Reservation System

This repository contains the source code for an autonomous, conversational flight booking agent. Instead of forcing users through static web forms, this system leverages Large Language Models to parse natural language, manage context, handle missing information via clarification loops, and dynamically execute backend APIs. 

🏆 **Published Architecture:** The architecture and methodology behind this project were peer-reviewed and published in the IEEE Xplore Digital Library through the 5th ICMNWC ("Agentic AI for Conversational Flight Reservation Systems"). [View Publication Here](https://ieeexplore.ieee.org/document/11354415).

---

## 🛠 The Stack
* **Agent Core:** Python, LangChain, Google Gemini 3 Flash
* **Backend:** Java, Spring Boot, REST APIs
* **Database:** MySQL

## 🧠 System Architecture 
The system operates across three decoupled layers:
1. **Conversational Layer (Python):** Uses LangChain to intercept user queries, extract structured entities (origin, destination, date, time), and manage conversation memory. If a user omits required data, the agent autonomously triggers a clarification loop before proceeding.
2. **Backend API Layer (Java/Spring Boot):** Exposes endpoints for flight searches, wallet deductions, and booking record generation. 
3. **Data Layer (MySQL):** A relational database storing flight schedules, pricing, and persistent passenger booking records.

## ✨ Core Capabilities
* **Autonomous Entity Extraction:** Converts messy, informal user text into strict JSON payloads.
* **Dynamic Tool Invocation:** The LLM independently determines when it has enough context to trigger the Java REST APIs.
* **End-to-End Execution:** Handles the complete workflow from initial search to final database commit for booking confirmation.

---

## 📸 The System in Action

Here is the conversational agent dynamically executing backend operations in real-time.

### 1. Dynamic Fallback & Context Management
When a user requests a timeframe that doesn't exist in the database, the agent doesn't crash or return a raw SQL error. It autonomously intercepts the null result, maintains conversational context, and queries the database for the closest available alternatives.

<img width="711" alt="Smart Fallback Logic" src="https://github.com/user-attachments/assets/5c61b272-9ac1-44fd-b305-466214b4d9c6" />

### 2. Transactional Integrity (Booking & Wallet Provisioning)
The agent translates natural language into a strict JSON payload to trigger the Java REST APIs. Here, it successfully creates a new user profile, provisions a digital wallet with an initial deposit, and commits the flight booking to the MySQL database in a single seamless flow.

<img width="606" alt="Wallet and Booking Execution" src="https://github.com/user-attachments/assets/a5970646-d989-4fab-95df-3ecc619da27d" />

### 3. Database Mutation & State Reversal
The system handles booking cancellations by querying the generated Booking ID, reversing the database state, and processing the simulated refund back to the user's wallet.

<img width="535" alt="Cancellation and Refund" src="https://github.com/user-attachments/assets/92e7ca31-90f5-45c4-8633-98d621b409f2" />

---

## 🚀 Local Quick Start Guide

Follow these steps to run the code in your system: spin up the database, backend, and AI agent.

### Prerequisites
* Java 17+ and Maven

* Python 3.10+
* MySQL Server running on `localhost:3306`

### 1. Database Setup
1. Open MySQL Workbench or your preferred database client.
2. Execute the `schema.sql` file located inside the `/Database` folder. This will automatically generate the required `flights`, `bookings`, and `wallets` tables with dummy data.

### 2. Launch the Java Backend
1. Open your terminal and navigate to the backend directory:
   ```bash
   cd Springboot-Backend
2. (Optional) Update src/main/resources/application.properties with your specific MySQL root username and password if they differ from the defaults.
3. Boot the server using the Maven wrapper:
   ```bash
   ./mvnw spring-boot:run
  The REST API will initialize and run on http://localhost:8081.

### 3. Launch the AI Agent
1. Open a new Command line/Terminal tab and navigate to client directory:
   cd AI-Agent-Client
2. Install the required Python dependencies
   ```bash
   pip install -r requirements.txt
4. Create your local environment variables:
   * Copy the .env.example file and rename it to .env
   * Open the .env file and insert your actual Google/Gemini API key: GOOGLE_API_KEY="your_secret_key_here"
5. Run the Conversational Orchestration Engine:
   ```bash
   python3 agent-prompt.py
  The terminal will output !!! CHAT BEGINS !!! and wait for your natural language prompt.
