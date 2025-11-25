# AI Agents Architecture using `std.lib.context`

This document outlines how the primitives of `std.lib.context` (Space, Context, Runtime, Pointer) and the `invoke-as` pattern can be used to build robust, modular AI Agents.

## Concept Mapping

| `std.lib.context` | AI Agent Concept | Description |
| :--- | :--- | :--- |
| **Space** | **Agent Memory / State** | The container for the agent's existence. It holds the "Context Window" (conversation history), Working Memory (current plan), and configuration. |
| **Context** | **LLM Provider / Brain** | The intelligence backend (e.g., `:openai`, `:anthropic`, `:llama.local`). |
| **Runtime (Rt)** | **Session / Thread** | The active connection to the LLM. It manages the accumulation of tokens, system prompts, and API connection state. |
| **Pointer** | **Tool / Skill** | A reference to an executable capability (e.g., `web-search`, `run-code`) that the agent can "dereference" (read docs) or "invoke" (execute). |

## 1. The Agent as a Space

An Agent is fundamentally a stateful entity. We can model an Agent as a `Space` that has specific Runtimes configured.

```clojure
(ns agent.core
  (:require [std.lib.context.space :as space]
            [std.lib.context.registry :as reg]))

;; 1. Define the LLM Context
(reg/registry-install :llm.openai
  {:rt {:default {:resource :ai.client/connect
                  :config   {:model "gpt-4"}}}})

;; 2. Create the Agent (Space)
(defn create-agent [id system-prompt]
  (let [agent (space/space-create {:namespace (str "agent." id)})]
    ;; Mount the Brain
    (space/space-context-set agent :llm :default {:config {:system system-prompt}})
    ;; Mount Tools (as other contexts or pointers)
    (space/space-context-set agent :tool.shell :default {})
    agent))
```

## 2. The "Think" Loop (Invoke-As)

We can treat the act of "asking the agent" as an `invoke-as` operation. The Agent Object itself is an Applicative.

```clojure
(defimpl Agent [space]
  :invoke apply/invoke-as
  :protocols [std.protocol.apply/IApplicable
              :body {-apply-default  (:llm (space/space-rt-active space)) ;; Default to thinking
                     -apply-in       (chat-with-llm rt args)
                     -transform-in   (format-prompt args)}])

;; Usage
(def coder (create-agent "coder" "You are a Clojure expert."))

;; "Invoke" the agent -> Triggers the LLM Runtime
(coder "How do I reverse a list?")
;; => "You can use the `reverse` function..."
```

## 3. Tool Use via Pointers

Tools are simply Pointers to other Contexts. This decouples the Agent from the tool implementation.

*   **Definition**: A Pointer to `{:context :tool.shell :id "ls"}`.
*   **Discovery**: The Agent can "dereference" the pointer to get the tool definition (JSON Schema for function calling).
*   **Execution**: The Agent "invokes" the pointer to run the tool.

```clojure
(defn execute-tool-call [agent tool-name args]
  (let [tool-ptr (get-tool agent tool-name)] ;; Resolves pointer from Space
    (apply/apply-in tool-ptr nil args)))     ;; Invokes tool in its own Runtime
```

## 4. Multi-Agent Systems (Swarms)

Since `Space`s are isolated, we can nest them or link them.

### A. The "Room" Pattern (Shared Space)
A `Room` is a Space. Agents are Pointers within that Space.
*   When Agent A speaks, it invokes the Room's `:broadcast` runtime.
*   The Room relays the message to other Agents (Pointers).

### B. Hierarchical Agents (Manager/Worker)
A Manager Agent is a Space that contains Pointers to Worker Agents.
*   **Manager**: "I need to scrape this site and summarize it."
*   **Delegate**: Manager invokes `worker-scraper` (Pointer).
*   **Worker**: Runs in its own Space/Runtime, returns result.
*   **Manager**: Invokes `worker-writer` (Pointer) with the result.

## 5. Implementation Example: The Recursive Agent

An agent that can "fork" itself to solve sub-problems.

```clojure
(defn recursive-solve [goal depth]
  (binding [*runtime* (current-llm-session)]
    (let [plan (ask-llm "Break this down:" goal)]
      (if (simple? plan)
        (execute plan)
        (let [sub-agents (map create-sub-agent (:steps plan))]
          (mapv invoke-as sub-agents))))))
```

## Summary
Using `std.lib.context` for AI Agents provides:
1.  **Isolation**: Each agent has its own memory (Space) and connection state (Runtime).
2.  **Modularity**: Brains (LLMs) and Hands (Tools) are hot-swappable Runtimes.
3.  **Composability**: Agents are just Applicatives; they can be composed, piped, or mocked like standard functions.
