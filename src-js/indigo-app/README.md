# Indigo Frontend

## Prerequisites
- Node.js (v18 or higher)
- Clojure (Leiningen)

## Setup

1.  **Install Dependencies**:
    Navigate to `src-js/indigo` and run:
    ```bash
    npm install
    ```

## Running the Project

1.  **Start the Clojure Server**:
    From the root of `foundation-base`, start the REPL or run the server:
    ```bash
    lein run -m indigo.server
    ```
    The server will start on port `1311`.

2.  **Start the Vite Development Server**:
    In a new terminal, navigate to `src-js/indigo` and run:
    ```bash
    npm run dev
    ```
    The frontend will be available at `http://localhost:5173`.

## Features
- **Component Browser**: Browse and import components.
- **Theme Editor**: Customize the look and feel.
- **Viewport Canvas**: Drag and drop components to build your UI.
- **Proxy**: API requests to `/api` are proxied to the Clojure server.
