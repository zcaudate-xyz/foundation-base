;; src/code/ai/mcp_runtime_tool.clj
(ns code.ai.mcp-runtime-tool
  (:require [std.lang :as l]
            [std.lib :as h]
            [rt.basic.type-basic :as basic]
            [std.lang.base.impl :as impl]
            [code.ai.lua-runtime :as lua-rt])) ; Assuming lua-runtime is available

;; A map to hold active runtime instances
(defonce ^:private *runtimes* (atom {}))

(defn- get-or-create-runtime
  "Gets an existing runtime or creates a new one for the given language."
  [lang]
  (or (get @*runtimes* lang)
      (let [new-rt (case lang
                     :lua (basic/rt-basic {:lang :lua})
                     ;; Add other languages here
                     (throw (ex-info (str "Unsupported language: " lang) {:lang lang})))]
        (swap! *runtimes* assoc lang new-rt)
        ;; Emit necessary functions for the runtime
        (case lang
          :lua (h/once (impl/emit-as new-rt '(lua-rt/evaluate-string)))
          nil) ; No-op for other languages for now
        new-rt)))

(defn- handle-eval [lang code]
  (let [rt (get-or-create-runtime lang)]
    (case lang
      :lua (l/rt:invoke rt `(lua-rt/evaluate-string ~code))
      ;; Add other language eval logic
      (throw (ex-info (str "Eval not supported for language: " lang) {:lang lang})))))

(defn- handle-restart [lang]
  (let [rt (get @*runtimes* lang)]
    (if rt
      (do
        ;; Assuming rt.basic runtimes have a restart mechanism
        ;; For now, just remove and recreate
        (swap! *runtimes* dissoc lang)
        (get-or-create-runtime lang)
        {:status :success :message (str lang " runtime restarted.")})
      {:status :error :message (str lang " runtime not found.")})))

(defn- handle-debug [lang code]
  ;; Debugging is complex and highly language-specific.
  ;; For now, this will be a placeholder.
  {:status :error :message (str "Debugging not implemented for " lang)})

(defn mcp-runtime-tool
  "TODO"
  {:added "4.0"}
  [type op input]
  (try
    (case op
      :eval (handle-eval type input)
      :restart (handle-restart type)
      :debug (handle-debug type input)
      (throw (ex-info (str "Unsupported operation: " op) {:op op})))
    (catch Exception e
      {:status :error :message (str "Tool error: " (.getMessage e))})))