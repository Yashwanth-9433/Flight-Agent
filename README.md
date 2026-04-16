# Agentic AI Flight Reservation System

This repository contains the source code for an autonomous, conversational flight booking agent. Instead of forcing users through static web forms, this system leverages Large Language Models to parse natural language, manage context, handle missing information via clarification loops, and dynamically execute backend APIs. 

The architecture and methodology behind this project were published in IEEE through the 5th ICMNWC ("Agentic AI for Conversational Flight Reservation Systems”; [Publication link](https://ieeexplore.ieee.org/document/11354415)).

## The Stack
* **Agent Core:** Python, LangChain, OpenAI GPT-4o-mini
* **Backend:** Java, Spring Boot, REST APIs
* **Database:** MySQL

## System Architecture 
The system operates across three decoupled layers:
1. **Conversational Layer (Python):** Uses LangChain to intercept user queries, extract structured entities (origin, destination, date, time), and manage conversation memory. If a user omits required data, the agent autonomously triggers a clarification loop before proceeding.
2. **Backend API Layer (Java/Spring Boot):** Exposes endpoints for flight searches, wallet deductions, and booking record generation. 
3. **Data Layer (MySQL):** A relational database storing flight schedules, pricing, and persistent passenger booking records.

## Core Capabilities
* **Autonomous Entity Extraction:** Converts messy, informal user text into strict JSON payloads.
* **Dynamic Tool Invocation:** The LLM independently determines when it has enough context to trigger the Java REST APIs.
* **End-to-End Execution:** Handles the complete workflow from initial search to final database commit for booking confirmation.
