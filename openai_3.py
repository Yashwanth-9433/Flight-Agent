import os
import json
from datetime import datetime
from langchain_google_genai import ChatGoogleGenerativeAI
from langchain_core.prompts import ChatPromptTemplate
import requests
import time

# Set API key
os.environ["GOOGLE_API_KEY"] = ""

llm = ChatGoogleGenerativeAI(model="gemini-1.5-flash", temperature=0)

today = datetime.today().strftime("%Y-%m-%d (%A)")
current_year = datetime.today().year

# Prompt to extract structured info
extract_prompt = ChatPromptTemplate.from_template("""
You are a flight booking assistant.

Extract details from the user request:
- origin city
- destination city
- travel date in strict YYYY-MM-DD format
    * If relative date ("tomorrow", "next Saturday", "this Wednesday"), calculate relative to {today}.
    * If "September 17" without year, assume {current_year}.
- time_of_day must be one of: "morning", "afternoon", "evening", "night".
    * If user says "morning", "afternoon", "evening", or "night", map directly. Ignore case.
    * If user gives a clock time:
        - 05:00–11:59 → morning
        - 12:00–16:59 → afternoon
        - 17:00–20:59 → evening
        - 21:00–04:59 → night
    * If none provided, return 0.
- condition → look for "cheap", "cheapest", "best", "fastest". If not mentioned, return 0.

Return ONLY a JSON dictionary:
{{
  "origin": "...", 
  "destination": "...", 
  "date": "...", 
  "time_of_day": "...", 
  "condition": "..."
}}

If not found, put 0 (not "unknown").
User text: "{text}"
""")

# Prompt for clarification if data is missing
clarify_prompt = ChatPromptTemplate.from_template("""
You are a helpful flight booking assistant.
The extracted info so far is:
{text}

Some fields are missing (marked as 0).
Generate a short, natural question to the user asking ONLY about the missing fields.
Do not ask about fields that are already filled.
Return just the question.
""")

def extract_flight_info(user_text: str):
    """Extract structured info from user query"""
    formatted_prompt = extract_prompt.format_messages(
        text=user_text, today=today, current_year=current_year
    )
    response = llm.invoke(formatted_prompt)
    try:
        return json.loads(response.content)
    except:
        return {"origin": 0, "destination": 0, "date": 0, "time_of_day": 0, "condition": 0}

def generate_clarification(info_dict: dict):
    """Ask LLM to generate clarification question for missing fields"""
    formatted_prompt = clarify_prompt.format_messages(text=json.dumps(info_dict))
    response = llm.invoke(formatted_prompt)
    return response.content.strip()

def call_search_api(info):
    """Call the Spring Boot Search API"""
    url = "http://localhost:8081/api/flights/search"
    params = {
        "origin": info["origin"],
        "destination": info["destination"],
        "date": info["date"],
        "timeOfDay":info["time_of_day"],
        "condition":"cheap"
        
    }
    response = requests.get(url, params=params)
#   print("Raw response from API:", response.text)  👈 ADD THIS LINE

    try:
        data = response.json()
        if data:
            print("\n🟢 Found flights:")
            for f in data:
                print(f"✈️  {f['flightNumber']} | {f['origin']} → {f['destination']} | {f['date']} | ₹{f['price']} | {f['airline']}")
        else:
            print("⚠️  No flights found.")
            exit(0);
    except Exception as e:
        print("Error contacting search API:", e)

def call_book_api(flight_number, passenger_name):
    """Call the Spring Boot Booking API"""
    url = "http://localhost:8081/flights/book"
    params = {"flightNumber": flight_number, "passengerName": passenger_name}
    response = requests.get(url, params=params)
    try:
        data = response.json()
        print("\n✅ Booking Confirmed!")
        print(json.dumps(data, indent=2))
    except Exception as e:
        print("Error contacting booking API:", e)


# ---------------- Workflow ----------------
print("!!!CHAT BEGINS!!!")
print("Enter message: ") 
info = extract_flight_info(input("User: "))#"Book me a flight from Chennai to Delhi"
#print("Book me a flight from Chennai to Delhi")
#print("Step 1:", info)
print("")
required_fields = ["origin", "destination", "date", "time_of_day"]

while any(info[field] == 0 or info[field] == "0" for field in required_fields):  
    question = generate_clarification(info)
    print("Agent:", question)
    
    # Simulate user response (in real app: input() from user)
    user_reply = input("User: ")
    
    new_info = extract_flight_info(user_reply)
    
    # Update missing fields only
    for key, value in new_info.items():
        if info[key] in [0, "0"] and value not in [0, "0"]:
            info[key] = value
    
    print("Updated Info:", info)

#______________________________________________________________
print("\n✅ Final extracted info:", info)

# Step 2: Perform Search
call_search_api(info)

# Optional: Simulate user choosing a flight to book
flight_number = input("\nEnter flight number to book: ")
passenger_name = input("Enter passenger name: ")

call_book_api(flight_number, passenger_name)
time.sleep(300)
