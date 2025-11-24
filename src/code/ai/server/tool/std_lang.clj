(ns code.ai.server.tool.std-lang
  (:require [std.lang :as l]
            [std.lang.base.library :as lib]
            [std.lang.base.book :as book]))

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

(defn list-languages-fn
  [_ _]
  {:content [{:type "text"
              :text (pr-str (keys (lib/get-snapshot (l/default-library))))}]
   :isError false})

(def list-languages-tool
  {:name "std-lang-list"
   :description "Lists available languages in std.lang"
   :inputSchema {:type "object"
                 :properties {}}
   :implementation #'list-languages-fn})

(defn list-modules-fn
  [_ {:keys [lang]}]
  {:content [{:type "text"
              :text (pr-str (lib/list-modules (l/default-library) (keyword lang)))}]
   :isError false})

(def list-modules-tool
  {:name "std-lang-modules"
   :description "Lists modules for a given language"
   :inputSchema {:type "object"
                 :properties {"lang" {:type "string"}}
                 :required ["lang"]}
   :implementation #'list-modules-fn})
