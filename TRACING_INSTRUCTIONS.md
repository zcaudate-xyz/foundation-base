### How to Use the HTTP Tracer and Server

This system has two distinct parts that you will run in two separate terminal windows:

1.  **The Trace Server**: A simple web server that listens for trace messages.
2.  **Your REPL**: Your normal Clojure development environment where you will activate the tracer.

---

#### Step 1: Run the Trace Server

In your **first terminal window**, navigate to the root of this project and run the following command:

```bash
./lein exec -p src/my_trace_server.clj -e "(my-trace-server/start-server)"
```

You should see a message confirming that the server is running:

```
Trace server started on port 8080
```

This terminal will now print any trace messages it receives. Leave it running.

---

#### Step 2: Use the Tracer in Your REPL

In your **second terminal window**, start a regular Clojure REPL from the project root:

```bash
./lein repl
```

Now, within the REPL, you can dynamically trace any namespace.

1.  **Require the tracer namespace:**
    ```clojure
    (require '[my-http-tracer :as tracer])
    ```

2.  **Start watching a namespace.** Let's use the `user` namespace for this example.
    ```clojure
    (tracer/watch-ns 'user)
    ```

3.  **Define and run a new function** in that namespace.
    ```clojure
    (defn factorial [n]
      (if (zero? n) 1 (* n (factorial (dec n)))))

    (factorial 4)
    ```

4.  **Check the server terminal!** When you run `(factorial 4)`, you will see the full trace output appear in your *first* terminal window (the one running the server), looking something like this:

    ```
    --- TRACE RECEIVED ---
    (user/factorial 4)
    => 24
    ----------------------

    --- TRACE RECEIVED ---
    (user/factorial 3)
    => 6
    ----------------------

    ... and so on for the rest of the recursive calls.
    ```

5.  **Stop tracing** when you are finished.
    ```clojure
    (tracer/unwatch-ns 'user)
    ```

### Configuration (Optional)

If you need to run the server on a different port, you can change the `*trace-server-url*` in your REPL *before* you start watching:

```clojure
(require '[my-http-tracer :as tracer])

;; Change the target URL
(alter-var-root #'tracer/*trace-server-url* (constantly "http://localhost:9000/trace"))

;; Start the server on the new port in the other terminal
;; ./lein exec -p src/my_trace_server.clj -e "(my-trace-server/start-server {:port 9000})"

(tracer/watch-ns 'user)
```
