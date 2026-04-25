import os
import sys
import json
import re
import os
from dotenv import load_dotenv
load_dotenv(override=True)
from datetime import datetime
import requests
from langchain_google_genai import ChatGoogleGenerativeAI
from langchain_core.prompts import ChatPromptTemplate
from rich.console import Console

script_dir = os.path.dirname(os.path.abspath(__file__))
env_path = os.path.join(script_dir, '.env')
load_dotenv(dotenv_path=env_path, override=True)

gemini_key = os.getenv("GOOGLE_API_KEY")

# Initialize Rich Console for beautiful terminal UI
console = Console()

# Set API key
if not gemini_key:
    print(f"\n❌ ERROR: Checked for .env at '{env_path}' but GEMINI_API_KEY is missing.")
    sys.exit(1)
os.environ["GOOGLE_API_KEY"] = gemini_key

# Using the preview model for multimodal handling
llm = ChatGoogleGenerativeAI(model="gemini-3-flash-preview", temperature=0)

today = datetime.today().strftime("%Y-%m-%d (%A)")
current_year = datetime.today().year

extract_prompt = ChatPromptTemplate.from_template("""
You are a flight booking assistant.

Determine the user's intent: "book", "cancel", or "history".

If intent is "book", extract:
- origin
- destination
- date (strict YYYY-MM-DD format, relative to {today} or year {current_year})
- time_of_day (morning, afternoon, evening, night). Map times (05:00-11:59=morning, 12:00-16:59=afternoon, 17:00-20:59=evening, 21:00-04:59=night).
- condition (cheap, fastest, etc.)

If intent is "cancel", extract:
- booking_id (the number they want to cancel)

If intent is "history", extract:
- passenger_name

Return ONLY a JSON dictionary. Put 0 for missing fields:
{{
  "intent": "...",
  "origin": "...", 
  "destination": "...", 
  "date": "...", 
  "time_of_day": "...", 
  "condition": "...",
  "booking_id": "...",
  "passenger_name": "..."
}}
User text: "{text}"
""")

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
    formatted_prompt = extract_prompt.format_messages(text=user_text, today=today, current_year=current_year)
    response = llm.invoke(formatted_prompt)
    try:
        content = response.content
        if isinstance(content, list):
            content = content[0].get("text", "")
            
        # Bulletproof JSON extraction using Regex
        clean_text = re.sub(r"```(json)?", "", content).strip()
        return json.loads(clean_text)
    except Exception as e:
        console.print(f"\n[bold red][Debug] JSON Parsing Error:[/bold red] {e}")
        return {"intent": 0, "origin": 0, "destination": 0, "date": 0, "time_of_day": 0, "condition": 0, "booking_id": 0, "passenger_name": 0}

def generate_clarification(info_dict: dict):
    clean_dict = {k: v for k, v in info_dict.items() if k not in ["intent", "booking_id", "passenger_name", "condition"]}
    formatted_prompt = clarify_prompt.format_messages(text=json.dumps(clean_dict))
    response = llm.invoke(formatted_prompt)
    content = response.content
    if isinstance(content, list):
        content = content[0].get("text", "")
    return content.strip()

def call_search_api(info):
    url = "http://localhost:8081/api/flights/search"
    params = {"origin": info["origin"], "destination": info["destination"], "date": info["date"], "timeOfDay": info["time_of_day"], "condition": info.get("condition", "cheap")}
    try:
        response = requests.get(url, params=params)
        data = response.json() if response.status_code == 200 else []
    except Exception as e:
        console.print(f"[bold red]Error contacting search API:[/bold red] {e}")
        return False

    if data:
        console.print(f"\n[bold green]🟢 Found flights for {info['time_of_day']}:[/bold green]")
        for f in data:
            console.print(f"✈️  [cyan]{f['flightNumber']}[/cyan] | {f['origin']} → {f['destination']} | {f['date']} | [yellow]₹{f['price']}[/yellow] | {f['airline']}")
        return True

    console.print(f"\n[bold yellow]⚠️ Sorry, no flights available in the {info['time_of_day']}.[/bold yellow]")
    console.print("[italic]🔍 Scanning other timings for the same day...[/italic]")

    all_times = ["morning", "afternoon", "evening", "night"]
    alternatives_found = False

    for fallback_time in all_times:
        if fallback_time == info["time_of_day"]: continue
        params["timeOfDay"] = fallback_time
        try:
            resp = requests.get(url, params=params)
            fallback_data = resp.json() if resp.status_code == 200 else []
            if fallback_data:
                if not alternatives_found:
                    console.print("\n[bold cyan]💡 Good news! Here are alternative flights on that day:[/bold cyan]")
                    alternatives_found = True
                for f in fallback_data:
                    console.print(f"✈️  [cyan]{f['flightNumber']}[/cyan] | {fallback_time.upper()} | [yellow]₹{f['price']}[/yellow] | {f['airline']}")
        except: pass

    if not alternatives_found:
        console.print("\n[bold red]❌ No flights found for that entire day.[/bold red]")
        return False
    return True

def call_book_api(flight_number, passenger_name):
    url = "http://localhost:8081/api/flights/book"
    try:
        response = requests.post(url, params={"flightNumber": flight_number, "passengerName": passenger_name})
        data = response.json()
        if response.status_code == 200:
            
            # ✅ NEW: If Java says it made a new wallet, print the alert!
            if data.get("newWalletCreated"):
                console.print(f"\n[bold cyan]🏦 '{passenger_name}' Profile added to wallet with initial deposit ₹15000[/bold cyan]")
                
            console.print(f"[bold green]✅ {data.get('message', 'Booking Confirmed!')}[/bold green]")
        else:
            console.print(f"\n[bold red]❌ Booking FAILED! Reason:[/bold red] {data.get('error', 'Unknown Error')}") 
    except Exception as e: console.print(f"[bold red]Error contacting booking API:[/bold red] {e}")

def call_cancel_api(booking_id):
    url = "http://localhost:8081/api/flights/cancel"
    try:
        response = requests.post(url, params={"bookingId": booking_id})
        data = response.json()
        if response.status_code == 200:
            console.print(f"\n[bold green]✅ {data.get('message', 'Cancellation successful!')}[/bold green]")
        else:
            console.print(f"\n[bold red]❌ Cancellation FAILED! Reason:[/bold red] {data.get('error', 'Unknown Error')}")
    except Exception as e: console.print(f"[bold red]Error contacting cancellation API:[/bold red] {e}")

def call_history_api(passenger_name):
    url = "http://localhost:8081/api/flights/history"
    try:
        response = requests.get(url, params={"passengerName": passenger_name})
        data = response.json()
        if response.status_code == 200:
            if not data:
                console.print(f"\n[bold yellow]📂 No booking history found for {passenger_name}.[/bold yellow]")
            else:
                console.print(f"\n[bold cyan]📂 Booking History for {passenger_name}:[/bold cyan]")
                for b in data:
                    status_color = "green" if b['status'] == "CONFIRMED" else "red"
                    console.print(f"  🆔 ID: {b['id']} | ✈️ Flight: {b['flight_number']} | [{status_color}]{b['status']}[/{status_color}]")
        else:
            console.print(f"\n[bold red]❌ Failed to fetch history:[/bold red] {data.get('error', 'Unknown')}")
    except Exception as e:
        console.print(f"[bold red]Error contacting history API:[/bold red] {e}")

# ---------------- Workflow ----------------
try:
    console.print("\n[bold magenta]!!! CHAT BEGINS !!![/bold magenta]")
    
    # Ensure user doesn't just hit Enter on an empty string
    user_input = ""
    while not user_input.strip():
        user_input = console.input("[bold blue]User:[/bold blue] ")
        
    info = extract_flight_info(user_input)

    # ROUTER: Intent Detection
    if info.get("intent") == "cancel":
        console.print("\n[bold yellow]🛠️  Cancellation Mode Activated[/bold yellow]")
        b_id = info.get("booking_id")
        if b_id in [0, "0", None]:
            b_id = console.input("[bold cyan]Agent:[/bold cyan] Please provide your Booking ID to cancel.\n[bold blue]User:[/bold blue] ")
        call_cancel_api(b_id)

    elif info.get("intent") == "history":
        console.print("\n[bold cyan]📜 History Mode Activated[/bold cyan]")
        p_name = info.get("passenger_name")
        if p_name in [0, "0", None]:
            p_name = console.input("[bold cyan]Agent:[/bold cyan] Please provide your name to view your booking history.\n[bold blue]User:[/bold blue] ")
        call_history_api(p_name)

    else:
        # BOOKING MODE
        console.print("")
        required_fields = ["origin", "destination", "date", "time_of_day"]

        while any(info.get(field) in [0, "0", None] for field in required_fields):  
            question = generate_clarification(info)
            console.print(f"[bold cyan]Agent:[/bold cyan] {question}")
            user_reply = console.input("[bold blue]User:[/bold blue] ")
            new_info = extract_flight_info(user_reply)
            for key, value in new_info.items():
                if info.get(key) in [0, "0"] and value not in [0, "0"]:
                    info[key] = value

        if call_search_api(info):
            raw_flight = console.input("\n[bold]Enter flight number to book:[/bold] ")
            flight_number = raw_flight.upper().replace(" ", "")
            
            passenger_name = console.input("[bold]Enter passenger name:[/bold] ")
            call_book_api(flight_number, passenger_name)

except KeyboardInterrupt:
    console.print("\n\n[bold red]Session Terminated by User. Goodbye![/bold red]")
