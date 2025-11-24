(ns code.ai.server
  (:require [mcp-clj.mcp-server.core :as mcp-server]
            [mcp-clj.tools.clj-eval :as clj-eval]
            [std.lib :as h]
            [std.lang :as l]
            [rt.basic.impl.process-lua]
            [rt.basic.impl.process-js])
  (:import [java.io StringWriter]))

(defonce ^:dynamic *server* (atom nil))

(defn echo-fn
  [_ {:keys [text]}]
  {:content [{:type "text" :text text}]
   :isError false})

(def echo-tool
  {:name "echo"
   :description "Echo the input text"
   :inputSchema {:type "object"
                 :properties {"text" {:type "string"}}
                 :required ["text"]}
   :implementation #'echo-fn})

(defn ping-fn
  [_ _]
  {:content [{:type "text" :text "ping"}]
   :isError false})

(def ping-tool
  {:name "ping"
   :description "Ping the input text"
   :inputSchema {:type "object"
                 :properties {"text" {:type "string"}}
                 :required ["text"]}
   :implementation #'ping-fn})

(defn lang-emit-as-safe
  "Safely evaluate Clojure code, returning a result string or error"
  [type code-str]
  (try
    (let [form (read-string code-str)]
      (with-out-str
        (binding [*err* *out*] 
          (try
            (print (l/emit-as type [form]))
            (catch Throwable e
              (println "EVAL FAILED:")
              (println (ex-message e) (pr-str (ex-data e)))
              (.printStackTrace e))))))
    (catch Throwable e
      (str "Error: " (.getMessage e) "\n"
           "ex-data : " (pr-str (ex-data e)) "\n"
           (with-out-str
             (binding [*err* *out*]
               (.printStackTrace ^Throwable (ex-info "err" {}))))))))

(defn lang-emit-as-fn
  [_ {:keys [type
             code]}]
  {:content [{:type "text"
              :text
              (lang-emit-as-safe (keyword type)
                                 code)}] 
   :isError false})

(def lang-emit-as-tool
  {:name "lang-emit-as"
   :description "Emits code given clojure dsl"
   :inputSchema {:type "object"
                 :properties {"code" {:type "string"}
                              "type" {:type "string"}}
                 :required ["code" "type"]}
   :implementation #'lang-emit-as-fn})

(defn create-server
  ([]
   (create-server {:port 3001 :type :sse}))
  ([{:keys [port type] :or {port 3001 type :sse} :as opts}]
   (mcp-server/create-server
    {:transport opts
     :tools {"echo" echo-tool
             "ping" ping-tool
             "lang-emit-as" lang-emit-as-tool
             "clj-eval" clj-eval/clj-eval-tool}})))

(defn start-server
  ([] (start-server {}))
  ([opts]
   (swap! *server*
          (fn [val]
            (if val
              val
              (create-server opts))))))

(defn stop-server
  []
  (swap! *server*
         (fn [val]
           (if val
             (do ((:stop val)) nil)
             nil))))

(comment
  (start-server)
  (stop-server)
  (lang-emit-as-safe "lua" "(+ 1 2 3)")
  
  (l/emit-as :lua ['(+ 1 2 3)])
  )