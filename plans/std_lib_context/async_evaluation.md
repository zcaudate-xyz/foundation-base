# Async Evaluation for AI Agents

Yes, you **do need** to account for async evaluation when building AI agents, primarily because Large Language Model (LLM) calls are network-bound and often slow (taking seconds or even minutes). Blocking the main thread for these operations is generally unacceptable in interactive or high-throughput systems.

## Analysis of Current Capability

The `std.lib.context` and `std.lib.apply` libraries **already contain the primitives** to support async execution without major refactoring.

### 1. `std.lib.return` Abstraction
The library `std.lib.return` (referenced in `std.lib.apply`) abstracts over values and `CompletableFuture`s (via `std.lib.future`).

*   It implements `IReturn` for `Object` (synchronous values) and `CompletionStage` (futures).
*   It provides `return-chain`:
    ```clojure
    (defn return-chain [out f]
      (cond (f/future? out)
            (f/on:success out f)

            :else
            (f out)))
    ```
    This function automatically handles "mapping" over the result, whether it's an immediate value or a future.

### 2. `apply-in` Implementation
The `apply-in` function in `std.lib.apply` uses `return-chain`:

```clojure
(defn apply-in [app rt args]
  (let [input  (protocol.apply/-transform-in app rt args)
        output (protocol.apply/-apply-in app rt input)] ;; <--- Can return a Future
    (r/return-chain output
                    (partial protocol.apply/-transform-out app rt args))))
```

**Conclusion:** If your Runtime's implementation of `-apply-in` (or `-invoke-ptr`) returns a `CompletableFuture` (or `std.lib.future`), the transformation logic (`-transform-out`) will automatically be chained onto that future. The consumer of the API will simply receive a Future back.

## Recommendation for AI Agents

You do **not** need to add explicit `async` flags to the `std.lib.context` protocols. Instead, you should adopt an **"Async-First"** or **"Async-Transparent"** contract for your AI Runtimes.

### 1. Implement Async Runtimes
When creating the Runtime for an AI provider (e.g., OpenAI), ensure `-invoke-ptr` returns a future.

```clojure
(defimpl OpenAIRuntime [api-key]
  :protocols [std.protocol.context/IContext
              :body {-invoke-ptr (std.lib.future/future
                                   (http/post "https://api.openai.com/..." ...))
                     ;; ...
                     }])
```

### 2. Handling Streams (Token Streaming)
For AI agents, you often want streaming responses (tokens appearing as they generate). `CompletableFuture` returns a single value.

**Proposal:** Use `std.lib.stream` or `manifold` concepts if streaming is required.
*   If the result is a **Stream/Channel**, `return-chain` might need to be extended or the Runtime might return a "Stream Pointer" that consumers can read from.
*   *Simpler approach:* The `Pointer` refers to the *Job*, and `deref` returns the current status/buffer.

### 3. Agent "Think" Loops
If an Agent's logic involves multiple steps (Chain of Thought), use `std.lib.future` composition (`then-compose` / `on:success`) to chain these async calls.

```clojure
(defn think-step [agent input]
  (-> (invoke-as agent input)               ;; Returns Future<Response>
      (f/on:success (fn [resp]
                      (if (tool-call? resp)
                        (execute-tool resp) ;; Returns Future<ToolResult>
                        resp)))))
```

## Summary
*   **Yes**, async is required.
*   **No**, you don't need to change the core `invoke-as` code.
*   **Yes**, `std.lib.apply` transparently supports it via `std.lib.return`.
*   **Action**: Ensure your AI Runtime implementations return Futures.
