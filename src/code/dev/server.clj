(ns code.dev.server
  (:require [org.httpkit.server :as http]
            [code.dev.server.router :as router]
            [code.dev.server.pages :as pages]
            [code.heal :as heal]
            [std.json :as json]
            [std.block :as block]
            [std.lib :as h]
            [std.lib.bin :as bin]
            [std.html :as html]
            [std.string :as str])
  (:import (java.awt Desktop)
           (java.net URI)))

(def ^:dynamic *port* 1311)

(def ^:dynamic *public-path* "assets/code.dev/public")

(defonce ^:dynamic *instance*
  (atom nil))

(defn from-html
  [body]
  (let [full (std.html/tree
              (str "<div>" body "</div>"))
        full (if (= 2 (count full))
               (second full)
               full)
        full (std.lib.walk/postwalk
              (fn [x]
                (if (map? x)
                  (let [v (or (:class x)
                              (:classname x))
                        v (if (string? v)
                            [v]
                            (vec (keep (fn [v]
                                         (not-empty (str/trim v)))
                                       v)))]
                    (cond-> x
                      :then (dissoc :classname :class)
                      (seq v) (assoc :class v)))
                  x))
              full)]
    (block/string (block/layout full))))

(defn to-html
  [body]
  (std.html/html
   (try 
     (read-string body)
     (catch Throwable t
       ""))))

(defn translate-js-prompt
  [body]
  (str/join-lines
   ["SYSTEM PROMPT START ----"
    "You are an expert programming language translator and std.lang expert, being able to translate js/ts/jsx/tsx code"
    "into a clojure compatible javascript dsl. The dsl spec is presented in the SYSTEM INFO section. You will take code"
    "presented in USER PROMPT and translate it to std.lang dsl. Only output dsl code with no explainations. Only output"
    "the function/functions available in the input. Do not output the MODULE form, the ns form or the l/script form as they"
    "are only there for setup."
    "SYSTEM PROMPT END ----"
    "SYSTEM INFO START ----"
    (slurp ".prompts/plans/translate_js.md")
    "SYSTEM INFO END ----"
    "USER PROMPT START ----"
    body
    "USER PROMPT END ----"]))

(defn translate-js
  [body]
  (let [input  (translate-js-prompt body)
        output @(h/sh {:args ["gemini" "<<" "EOF" input "EOF"]})]
    (->> (str/split-lines output)
         (filter (fn [line]
                   (not (str/starts-with? line "```"))))
         (str/join-lines)
         (heal/heal))))

(defn translate-plpgsql-prompt
  [body]
  (str/join-lines
   ["SYSTEM PROMPT START ----"
    "You are an expert programming language translator and std.lang expert, being able to translate plpgsql code"
    "into a clojure compatible javascript dsl. The dsl spec is presented in the SYSTEM INFO section. You will take code"
    "presented in USER PROMPT and translate it to std.lang dsl. Only output dsl code with no explainations. Only output"
    "the function/functions available in the input. Do not output the MODULE form, the ns form or the l/script form as they"
    "are only there for setup."
    "SYSTEM PROMPT END ----"
    "SYSTEM INFO START ----"
    (slurp ".prompts/plans/translate_pg.md")
    "SYSTEM INFO END ----"
    "USER PROMPT START ----"
    body
    "USER PROMPT END ----"]))

(defn translate-plpgsql
  [body]
  (let [input  (translate-plpgsql-prompt body)
        output @(h/sh {:args ["gemini" "<<" "EOF" input "EOF"]})]
    (->> (str/split-lines output)
         (filter (fn [line]
                   (not (str/starts-with? line "```"))))
         (str/join-lines)
         (heal/heal))))

(defn translate-jsxc-prompt
  [body]
  (str/join-lines
   ["SYSTEM PROMPT START ----"
    "You are an expert programming language.  being able to translate jsxc/ts/jsxcx/tsx code"
    "into a clojure compatible javascript dsl tree form. There is a decomposition process and a reconstruction"
    "process, breaking down the components into a managable flat structure. The spec is presented in the SYSTEM INFO section."
    "You will take code presented in USER PROMPT"
    "the function/functions available in the input. Do not output the MODULE form, the ns form or the l/script form as they"
    "are only there for setup."
    "SYSTEM PROMPT END ----"
    "SYSTEM INFO START ----";
    (slurp ".prompts/plans/translate_jsxc.md")
    "SYSTEM INFO END ----"
    "USER PROMPT START ----"
    body
    "USER PROMPT END ----"]))

(defn translate-jsxc
  [body]
  (let [input  (translate-jsxc-prompt body)
        output @(h/sh {:args ["gemini" "<<" "EOF" input "EOF"]})]
    (->> (str/split-lines output)
         (filter (fn [line]
                   (not (str/starts-with? line "```"))))
         (str/join-lines)
         (heal/heal))))

(def dev-route-handler
  (router/router
   {#_#_#_#_
    "GET /" (fn [req] (html/html (#'pages/index-page)))
    "GET /page/demo" (fn [req] (html/html (#'pages/demo-page)))
    "POST /api/translate/from-html"  (fn [req]
                                       (json/write
                                        {:data (from-html (:body req))}))
    "POST /api/translate/to-html"    (fn [req]
                                       (json/write
                                        {:data (to-html (:body req))}))

    "POST /api/heal"                 (fn [req]
                                       (json/write
                                        {:data (heal/heal (:body req))}))
    "POST /api/translate/js"         (fn [req]
                                       (json/write
                                        {:data  (translate-js (:body req))}))
    "POST /api/translate/jsxc"       (fn [req]
                                       (json/write
                                        {:data  (translate-jsxc (:body req))}))
    "POST /api/translate/python"     (fn [req] (json/write
                                                {:op :translate-python}))
    "POST /api/translate/plpgsql"    (fn [req]
                                       (json/write
                                        {:data  (translate-plpgsql (:body req))}))}))

(defn dev-handler
  [req]
  (let [{:keys [uri request-method ^org.httpkit.BytesInputStream body]} req
        body (if body
               (String. (. body (readAllBytes)))
               {})]
    (or (#'dev-route-handler
         (assoc req :body body))

        (cond (= uri "/")
              {:status 200
               :headers {"Content-Type" "text/html"}
               :body    
               (#'pages/index-page)}

              (= uri "/page/demo")
              {:status 200
               :headers {"Content-Type" "text/html"}
               :body    
               (#'pages/demo-page)}

              (= request-method :get)
              (router/serve-resource uri *public-path*)
              
              :else
              {:status 404 :body "Not Found"}))))

(defn server-start
  []
  (swap! *instance*
         (fn [stop-fn]
           (when (not stop-fn)
             (http/run-server #'dev-handler {:port *port*})))))

(defn server-stop
  "Stops the HTTP server"
  {:added "4.0"}
  ([]
   (swap! *instance*
          (fn [stop-fn]
            (when stop-fn (stop-fn :timeout 100))
            nil))))

(defn server-toggle
  []
  (if @*instance*
    (server-stop)
    (server-start)))


(defn open-client
  []
  (. (Desktop/getDesktop)
     (browse (URI. (str "http://localhost:" *port*)))))

(comment
  (h/sh "curl" "-X" "POST" (str "http://localhost:" *port*
                                "/api/translate/js"))
  (server-toggle)
  (open-client)
  )

