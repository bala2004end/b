# AI Database Assistant using RAG

> Production-Ready AI-Powered Database Chat Assistant built with **Java 21**, **Spring Boot 3.x**, **LangChain4j**, **Google Gemini API**, **MySQL**, and **React 19 (Vite, JSX, Bootstrap 5)**.

---

## 🚀 Key Features

1. **RAG (Retrieval-Augmented Generation) Pipeline**:
   - Automatically extracts target MySQL schema metadata (tables, columns, data types, primary keys, foreign keys, relationships, views, and indexes).
   - Generates semantic text chunks and creates vector embeddings inside Vector Memory.
   - Vector similarity search retrieves the top-K relevant schema context for user questions before invoking LLM.

2. **Google Gemini API Integration**:
   - Converts natural language questions into valid, optimized MySQL 8.0 queries.
   - Provides natural language executive summaries explaining query result sets.
   - Recommends AI database index optimizations.

3. **Strict Security & Read-Only Guardrails**:
   - Default **Read-Only Mode** blocks `DROP`, `DELETE`, `UPDATE`, `TRUNCATE`, `ALTER`, `CREATE`, and `INSERT` statements.
   - Syntax validation and multi-statement SQL injection prevention.
   - Spring Security 6 with stateless JWT authentication.

4. **ChatGPT-Style Rich Frontend (React 19 Vite JSX)**:
   - Modern dark glassmorphic UI styled with Bootstrap 5 and custom CSS tokens.
   - SQL Code block preview with **Copy**, **Execute**, **Explain (`EXPLAIN`)**, and **Bookmark** controls.
   - Typing indicator animation while AI processes requests.
   - Automatic visual charts (**Bar**, **Line**, **Pie**) via Recharts.
   - Responsive Data Table result view with **CSV** and **JSON** export capability.
   - **Voice Input** using Web Speech API.

5. **Advanced Modules**:
   - **Dashboard**: System overview, active database status, query velocity metrics.
   - **Database Connection**: Configurable host, port, credentials, test connection, and live vector indexing progress bar.
   - **Schema Browser**: Interactive table tree, column definitions, data types, relationships, and foreign key badges.
   - **Saved Queries**: Bookmarked query repository for quick one-click execution.
   - **Query History**: Searchable audit trail of executed queries with timing metrics.
   - **Analytics & AI Insights**: Slow query detection, execution plan visualizer, index recommendations.

---

## 🛠️ Technology Stack

### Backend
- **Language**: Java 21
- **Framework**: Spring Boot 3.3.4
- **Security**: Spring Security 6 + JJWT (0.12.6)
- **Data Access**: Spring Data JPA + MySQL Connector/J + H2 (internal app database)
- **AI & RAG**: LangChain4j (`0.35.0`) + Google Gemini API (`langchain4j-google-ai-gemini`)
- **Utilities**: Lombok, Jackson

### Frontend
- **Framework**: React 19 + React DOM 19
- **Build Tool**: Vite (JSX format)
- **Styling**: Bootstrap 5 + Vanilla CSS Glassmorphism
- **HTTP Client**: Axios with JWT Interceptor
- **Routing**: React Router DOM 6/7
- **Visualization**: Recharts
- **Icons**: Lucide React

---

## 📁 Project Folder Structure

```
e:\Qfind\
├── backend/
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/com/aidb/assistant/
│       │   │   ├── AidbAssistantApplication.java
│       │   │   ├── config/          # SecurityConfig, CorsConfig, AppConfig
│       │   │   ├── controller/      # Auth, Connection, Chat, Schema, SavedQueries, Analytics, Settings
│       │   │   ├── dto/             # Request & Response Data Transfer Objects
│       │   │   ├── entity/          # User, ConnectionConfig, ChatMessage, Conversation, SavedQuery, AuditLog
│       │   │   ├── rag/             # DatabaseMetadataExtractor, SchemaVectorStore, GeminiLlm, SqlGenerator, SqlValidator, SqlExecution, AiInsight
│       │   │   ├── repository/      # Spring Data JPA Repositories
│       │   │   ├── security/        # JwtTokenProvider, JwtAuthFilter, CustomUserDetailsService
│       │   │   └── service/         # Business Logic Services
│       │   └── resources/
│       │       └── application.yml
├── frontend/
│   ├── package.json
│   ├── vite.config.js
│   ├── index.html
│   └── src/
│       ├── App.jsx
│       ├── main.jsx
│       ├── index.css
│       ├── components/          # Sidebar, Header, StatCard, SqlCodeBlock, DataTable, AiChart
│       ├── context/             # AuthContext, ThemeContext
│       ├── pages/               # Login, Dashboard, DatabaseConnection, SchemaBrowser, AIChat, SavedQueries, History, Analytics, Settings
│       └── services/            # api.js, authService.js, exportUtils.js
├── sample-db/
│   └── init_sample_db.sql        # E-Commerce & HR Sample MySQL database script
└── README.md
```

---

## 🚦 Getting Started & Local Setup

### Step 1: Initialize Sample Database (MySQL)

Run the included initialization script against your local MySQL server:

```bash
mysql -u root -p < e:\Qfind\sample-db\init_sample_db.sql
```

This creates the database `sample_company_db` with tables (`departments`, `employees`, `categories`, `products`, `customers`, `orders`, `order_items`) populated with rich sample data.

---

### Step 2: Configure & Run Spring Boot Backend

1. Navigate to the `backend/` directory:
   ```bash
   cd e:\Qfind\backend
   ```

2. (Optional) Set your Gemini API Key in `application.yml` or via environment variable:
   ```bash
   export GEMINI_API_KEY="your_actual_gemini_api_key_here"
   ```
   *(Note: If no API key is provided, the application falls back to intelligent rule-based generation).*

3. Build and launch the Spring Boot application:
   ```bash
   mvn clean spring-boot:run
   ```
   The backend server will start on **`http://localhost:8080`**.

---

### Step 3: Configure & Run React Frontend

1. Open a new terminal and navigate to the `frontend/` directory:
   ```bash
   cd e:\Qfind\frontend
   ```

2. Install Node dependencies:
   ```bash
   npm install
   ```

3. Start the Vite development server:
   ```bash
   npm run dev
   ```
   The frontend application will run on **`http://localhost:3000`** with automatic proxying to backend `http://localhost:8080`.

---

## 🧪 Sample Prompts to Try in AI Chat

Once connected to `sample_company_db`:

- `How many employees joined this month?`
- `Which department has the highest salary?`
- `Show products with stock below 20.`
- `Which customers purchased more than ₹50000?`
- `Show orders placed in July 2026.`

---

## 🔒 Security Features

- **JWT Token Authentication**: Secures all backend `/api/**` endpoints.
- **Read-Only Mode**: Default active guardrail preventing any data modification.
- **Query Sanitize**: Restricts multiple SQL statement chaining.
- **Audit Logging**: Saves timestamped records of all executed queries.

---

## 📄 License
Production-ready template built for high-performance AI Database operations.
