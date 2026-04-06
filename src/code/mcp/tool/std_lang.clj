(ns code.mcp.tool.std-lang
  (:require [std.lang :as l]
            [std.lang.base.book :as book]
            [std.lang.base.library :as lib]))

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
   :description (str "Transpile a single Clojure DSL form into a target `std.lang` language. Use this for quick "
                     "emit probes, syntax verification, or experimenting with generated output before editing "
                     "language models or runtime code.")
   :inputSchema {:type "object"
                 :properties {"code" {:type "string"
                                      :description "A single Clojure DSL form to emit, such as `(+ 1 2)` or `[:+ 1 2]`."}
                              "type" {:type "string"
                                      :description "The target language key, such as `js`, `lua`, `python`, or `rust`."}}
                 :required ["code" "type"]}
   :implementation #'lang-emit-as-fn})

(defn list-languages-fn
  [_ _]
  {:content [{:type "text"
              :text (pr-str (keys (lib/get-snapshot (l/default-library))))}]
   :isError false})

(def list-languages-tool
  {:name "std-lang-list"
   :description "List the currently installed `std.lang` language books available to the MCP session."
   :inputSchema {:type "object"}
   :implementation #'list-languages-fn})

(defn list-modules-fn
  [_ {:keys [lang]}]
  {:content [{:type "text"
              :text (pr-str (lib/list-modules (l/default-library) (keyword lang)))}]
   :isError false})

(def list-modules-tool
  {:name "std-lang-modules"
   :description (str "List registered modules for a specific `std.lang` language. Use this when an agent needs to "
                     "discover the loaded modules before doing emit or manage work.")
   :inputSchema {:type "object"
                 :properties {"lang" {:type "string"
                                      :description "The language key whose modules should be listed, such as `js`, `lua`, or `python`."}}
                 :required ["lang"]}
   :implementation #'list-modules-fn})
