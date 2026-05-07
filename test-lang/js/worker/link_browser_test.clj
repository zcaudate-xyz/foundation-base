(ns js.worker.link-browser-test
  (:use code.test)
  (:require [clojure.walk :as walk]
            [hara.lang :as l]
            [hara.runtime.chromedriver.connection :as conn]
            [hara.runtime.chromedriver.connection-test :as conn-test]
            [hara.runtime.chromedriver.util :as util]
            [js.worker.link]
            [std.lib :as h]))

(defonce +page-server+ (atom nil))

(def ^:private +page-html+
  "<!doctype html><html><head><meta charset=\"utf-8\"><title>js.worker.link browser test</title></head><body>js.worker.link browser test</body></html>")

(def ^:private +webworker-script+
  (l/emit-script
   '(do
      (. self (addEventListener
               "message"
               (fn [e]
                 (. self (postMessage {"signal" "echo"
                                       "kind" "webworker"
                                       "body" (. e ["data"])}))
                 (return nil))
               false))
      (. self (postMessage {"signal" "ready"
                            "kind" "webworker"})))
   {:lang :js
    :layout :flat}))

(def ^:private +sharedworker-script+
  (l/emit-script
   '(do
      (:= (. globalThis ["onconnect"])
          (fn [e]
            (var port (. e ["ports"] [0]))
            (. port (start))
            (. port (addEventListener
                     "message"
                     (fn [evt]
                       (. port (postMessage {"signal" "echo"
                                             "kind" "sharedworker"
                                             "body" (. evt ["data"])}))
                       (return nil))
                     false))
            (. port (postMessage {"signal" "ready"
                                  "kind" "sharedworker"}))
            (return port))))
   {:lang :js
    :layout :flat}))

(defn start-page-server
  []
  (or @+page-server+
      (let [port    (h/port:check-available 0)
            script  (str "const http = require('http');"
                         "const html = " (pr-str +page-html+) ";"
                         "http.createServer((req, res) => {"
                         "res.writeHead(200, {'Content-Type': 'text/html; charset=utf-8'});"
                         "res.end(html);"
                         "}).listen(" port ", '127.0.0.1');")
            process (h/sh {:args ["node" "-e" script]
                           :wait false})
            result  (h/future (h/sh-wait process))
            _       (h/wait-for-port "127.0.0.1" port {:timeout 2000})]
        (reset! +page-server+
                {:port port
                 :process process
                 :result result}))))

(defn stop-page-server
  []
  (when-let [{:keys [process result]} @+page-server+]
    (h/sh-kill process)
    @result
    (reset! +page-server+ nil)))

(defn page-url
  []
  (str "http://127.0.0.1:" (:port (start-page-server)) "/"))

(defn start-tab
  []
  (let [port (:port (conn-test/start-scaffold))
        tab  (conn/conn-create {:port port
                                :attach :new})]
    @(util/page-navigate tab (page-url))
    (Thread/sleep 200)
    tab))

(defn eval-value
  [tab expression]
  (get @(util/runtime-evaluate tab expression
                               {:return-by-value true})
       "value"))

(defn await-worker-result
  [tab]
  (loop [i 0]
    (let [result (eval-value tab "globalThis.__worker_link_state && globalThis.__worker_link_state.last")]
      (cond (and (map? result)
                 (contains? result "body"))
            result

            (>= i 40)
            result

            :else
            (do (Thread/sleep 100)
                (recur (inc i)))))))

(def ^:private +browser-link-start-template+
  '((fn []
      (var state {"messages" []})
      (:= (. globalThis ["__worker_link_state"]) state)
      (var link (__LINK_FN__ __WORKER_SCRIPT__))
      (var worker ((. link ["create_fn"])
                   (fn [data]
                     (. (. state ["messages"]) (push data))
                     (:= (. state ["last"]) data)
                     (return data))))
      (. worker (postMessage {"tab" __TAB_ID__}))
      (return {"started" true
               "tab" __TAB_ID__}))))

(defn browser-link-start-script
  [link-fn worker-script tab-id]
  (l/emit-script
   (walk/prewalk-replace {'__LINK_FN__       link-fn
                          '__WORKER_SCRIPT__ worker-script
                          '__TAB_ID__        tab-id}
                         +browser-link-start-template+)
   {:lang :js
    :layout :flat}))

(fact:global
  {:setup    [(conn-test/restart-scaffold)
              (start-page-server)]
   :teardown [(stop-page-server)
              (conn-test/stop-scaffold)]})

(def +webworker-start-tab-a+
  (browser-link-start-script 'js.worker.link/make-webworker-link
                             +webworker-script+
                             "tab-a"))

(def +webworker-start-tab-b+
  (browser-link-start-script 'js.worker.link/make-webworker-link
                             +webworker-script+
                             "tab-b"))

(def +sharedworker-start-tab-a+
  (browser-link-start-script 'js.worker.link/make-sharedworker-link
                             +sharedworker-script+
                             "tab-a"))

(def +sharedworker-start-tab-b+
  (browser-link-start-script 'js.worker.link/make-sharedworker-link
                             +sharedworker-script+
                             "tab-b"))

(defn run-two-tab-browser-check
  [start-script-a start-script-b]
  (let [tab-a (start-tab)
        tab-b (start-tab)]
    (try
      (do @(util/runtime-evaluate tab-a start-script-a
                                  {:return-by-value true})
          @(util/runtime-evaluate tab-b start-script-b
                                  {:return-by-value true})
          {"tab-a" (await-worker-result tab-a)
           "tab-b" (await-worker-result tab-b)})
      (finally
        (conn/conn-close tab-a)
        (conn/conn-close tab-b)))))

(fact "make-webworker-link works in chromedriver across two tabs"
  (run-two-tab-browser-check +webworker-start-tab-a+
                             +webworker-start-tab-b+)
  => {"tab-a" {"signal" "echo"
               "kind" "webworker"
               "body" {"tab" "tab-a"}}
      "tab-b" {"signal" "echo"
               "kind" "webworker"
               "body" {"tab" "tab-b"}}})

(fact "make-sharedworker-link works in chromedriver across two tabs"
  (run-two-tab-browser-check +sharedworker-start-tab-a+
                             +sharedworker-start-tab-b+)
  => {"tab-a" {"signal" "echo"
               "kind" "sharedworker"
               "body" {"tab" "tab-a"}}
      "tab-b" {"signal" "echo"
               "kind" "sharedworker"
               "body" {"tab" "tab-b"}}})
