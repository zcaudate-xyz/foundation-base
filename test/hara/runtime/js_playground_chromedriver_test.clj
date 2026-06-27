(ns hara.runtime.js-playground-chromedriver-test
  (:require [clojure.string]
            [hara.runtime.js-playground :as playground]
            [hara.runtime.chromedriver :as chromedriver]
            [hara.lang :as l]
            [hara.common.util :as ut]
            [std.json :as json]
            [std.lib.component :as component]
            [std.lib.network :as network]
            [std.lib :as h])
  (:use code.test))

(l/script- :js
  {:runtime :playground
   :config {:port 0
            :host "localhost"
            :title "JS Playground"
            :tabs [{:id "stage" :label "Stage"}]}})

(defn- playground-rt []
  (ut/lang-rt :js))

(defn- wait-for-status
  "polls the browser until PLAYGROUND status is connected"
  [browser timeout-ms]
  (let [step 200
        max (/ timeout-ms step)]
    (loop [i 0]
      (let [res @(chromedriver/evaluate browser "window.PLAYGROUND && window.PLAYGROUND.getStatus()")
            status (get res "value")]
        (cond (= status "connected")
              status

              (>= i max)
              (str "timeout:" status)

              :else
              (do (Thread/sleep step)
                  (recur (inc i))))))))

(defn- capture-logs-script
  "returns a JS snippet that records console.warn/error and uncaught errors"
  []
  (str "window.__pgLogs = {warn: [], error: []};\n"
       "window.__pgErrors = [];\n"
       "var origWarn = console.warn;\n"
       "console.warn = function() {\n"
       "  var args = Array.prototype.slice.call(arguments);\n"
       "  window.__pgLogs.warn.push(args.map(String).join(' '));\n"
       "  if (origWarn) origWarn.apply(console, args);\n"
       "};\n"
       "var origError = console.error;\n"
       "console.error = function() {\n"
       "  var args = Array.prototype.slice.call(arguments);\n"
       "  window.__pgLogs.error.push(args.map(String).join(' '));\n"
       "  if (origError) origError.apply(console, args);\n"
       "};\n"
       "window.addEventListener('error', function(e) { window.__pgErrors.push(e.message); });\n"
       "window.addEventListener('unhandledrejection', function(e) { window.__pgErrors.push(String(e.reason)); });"))

^{:refer hara.runtime.js-playground.client/mount! :added "4.0"
  :timeout 60000
  :setup [(def +playground+ (playground-rt))
          (def +browser-port+ (network/port:check-available 0))
          (def +browser+
            (chromedriver/browser
             {:port +browser-port+
              :bench
              {:exec ["chromium-browser"
                      "--no-sandbox"
                      "--headless=new"
                      "--disable-extensions"
                      "--disable-gpu"
                      (str "--remote-debugging-port=" +browser-port+)
                      "--remote-debugging-address=0.0.0.0"]}}))
          ;; serve a page that captures console warnings/errors before the client module runs
          (spit (str (:root +playground+) "/index.html")
                (playground/page-html
                 {:title "JS Playground"
                  :tabs [{:id "stage" :label "Stage"}]
                  :head [:script (capture-logs-script)]}))
          (chromedriver/goto (playground/play-url +playground+) 30000 +browser+)]
  :teardown [(h/stop +browser+)
             (component/stop +playground+)]}
(fact "js-playground client loads and evaluates in chromedriver"

  (wait-for-status +browser+ 45000)
  => "connected"

  ;; eval through the playground websocket
  (!.js (+ 1 2 3))
  => 6

  ;; the React UI rendered the topbar
  @(chromedriver/evaluate +browser+ "document.getElementById('topbar') !== null")
  => {"value" true "type" "boolean"}

  ;; no EventEmitter / ObjectMultiplex warnings were captured
  (let [logs @(chromedriver/evaluate +browser+ "JSON.stringify(window.__pgLogs)")
        errs @(chromedriver/evaluate +browser+ "JSON.stringify(window.__pgErrors)")
        data (json/read (get logs "value"))
        all (->> (concat (get data "warn") (get data "error")
                         (json/read (get errs "value")))
                 (filter #(or (clojure.string/includes? % "MaxListenersExceededWarning")
                              (clojure.string/includes? % "ObjectMultiplex")))
                 (vec))]
    all
    => []))
