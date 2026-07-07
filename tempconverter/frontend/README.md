# Unit Converter Hub — Frontend

A single-page frontend that connects to **both** Spring Boot backends:

| Backend              | Port | Endpoints                                                   |
| -------------------- | ---- | ----------------------------------------------------------- |
| Temperature Converter | 8181 | `POST /api/temperatures/convert`, `GET /api/temperatures/history` |
| Currency Converter    | 8081 | `POST /api/currency/convert`, `GET /api/currency/history`        |

The frontend is plain HTML + CSS + vanilla JS — no build step, no npm install.

## Project layout

```
SOC/
├── lab2/tempconverter/            <-- Temperature backend (Spring Boot, port 8181)
│   └── frontend/                  <-- This frontend lives here
│       ├── index.html
│       ├── styles.css
│       └── app.js
└── doller converter/currencyconverter/  <-- Currency backend (Spring Boot, port 8081)
```

## How to run everything

### 1. Start MongoDB

Both apps point at `mongodb://localhost:27018`. Make sure your Mongo container / service is up on port **27018**.

### 2. Start the Temperature backend

```powershell
cd C:\Users\DELL\OneDrive\Desktop\SOC\lab2\tempconverter
.\mvnw.cmd spring-boot:run
```

Runs on **http://localhost:8181**.

### 3. Start the Currency backend (in a second terminal)

```powershell
cd "C:\Users\DELL\OneDrive\Desktop\SOC\doller converter\currencyconverter"
.\mvnw.cmd spring-boot:run
```

Runs on **http://localhost:8081**.

### 4. Open the frontend

The simplest way — just double-click `index.html`. It works from `file://` because both backends now allow CORS from any origin.

If you prefer serving it (recommended), pick one of these from inside the `frontend` folder:

```powershell
# Python
python -m http.server 5500

# Node (no install if you already have npx)
npx serve .
```

Then visit **http://localhost:5500**.

## What you get

- **Temperature tab** — Convert Celsius ↔ Fahrenheit. Each conversion is saved to MongoDB by the Temperature backend.
- **Currency tab** — Convert USD ↔ LKR (rate 1 USD = 300 LKR). Each conversion is saved by the Currency backend.
- **History tab** — Shows the saved conversion history from **both** backends side by side. Click *Refresh* to re-fetch.
- **Status dots** in the footer show whether each backend is reachable.

## CORS

Both backends now have a `WebConfig.java` that enables CORS for `/api/**` from any origin. That's the only change made to the backends — controllers, services and data models are untouched.

- `lab2/tempconverter/src/main/java/com/example/tempconverter/config/WebConfig.java`
- `doller converter/currencyconverter/src/main/java/com/hiranya/currencyconverter/config/WebConfig.java`

## Troubleshooting

- **Backend offline dot is red** → That backend isn't running, or Mongo isn't up on port 27018.
- **"Could not reach … backend"** → Same as above; check the terminal where you ran `mvnw spring-boot:run`.
- **Want to change the API URLs?** → Edit the two constants at the top of `app.js`:
  ```js
  const TEMP_API = "http://localhost:8181/api/temperatures";
  const CURRENCY_API = "http://localhost:8081/api/currency";
  ```
