(ns hara.runtime.js-playground.client-test
  (:use code.test)
  (:require [hara.runtime.js-playground :as playground]
            [hara.runtime.js-playground.client :refer :all]
            [hara.runtime.chromedriver :as chromedriver]
            [hara.lang :as l]
            [org.httpkit.server :as server]
            [std.lib.env :as env]
            [std.lib.network :as network]
            [std.fs :as fs]
            [std.lib :as h]))

(l/script- :js
  {:runtime :chromedriver.instance
   :require [[hara.runtime.js-playground.client :as client]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]]
   :test-mode true
   :emit {:lang/jsx false}})

(defn- chrome-candidates []
  ["google-chrome-stable"
   "google-chrome"
   "chromium"
   "chromium-browser"
   "chrome-headless-shell"])

(defn- chrome-exec []
  (or (System/getenv "CHROME")
      (some #(when (env/program-exists? %) %) (chrome-candidates))
      "chromium"))

(defn- chrome-available? []
  (or (System/getenv "CHROME")
      (some env/program-exists? (chrome-candidates))))

(defn- start-static-server
  "serves a generated playground page from a temp root"
  []
  (let [root (str (fs/create-tmpdir))
        _    (spit (str root "/index.html")
                   (playground/page-html {:title "JS Playground"
                                          :tabs [{:id "stage" :label "Stage"}]}))
        port (network/port:check-available 0)
        stop (server/run-server (fn [req]
                                  (if (= "/" (:uri req))
                                    {:status 200
                                     :headers {"Content-Type" "text/html"}
                                     :body (java.io.File. root "index.html")}
                                    {:status 404 :body "not found"}))
                                {:port port :ip "127.0.0.1"})]
    {:port port :url (str "http://127.0.0.1:" port "/") :stop stop}))

(defn- wait-for-playground
  "polls the browser until globalThis.PLAYGROUND is ready"
  [browser timeout-ms]
  (let [step 200
        max (/ timeout-ms step)]
    (loop [i 0]
      (let [res @(chromedriver/evaluate browser "typeof globalThis.PLAYGROUND")
            ready (= "object" (get res "value"))]
        (cond ready
              true

              (>= i max)
              false

              :else
              (do (Thread/sleep step)
                  (recur (inc i))))))))

(fact:global
 {:skip (not (chrome-available?))
  :setup [(l/rt:restart :js)
          (def +server+ (start-static-server))
          (def +browser-port+ (network/port:check-available 0))
          (def +browser+ (chromedriver/browser
                          {:port +browser-port+
                           :bench
                           {:exec [(chrome-exec)
                                   "--no-sandbox"
                                   "--headless=new"
                                   "--disable-extensions"
                                   "--disable-gpu"
                                   (str "--remote-debugging-port=" +browser-port+)
                                   "--remote-debugging-address=0.0.0.0"]}}))
          (chromedriver/goto (:url +server+) 30000 +browser+)
          (wait-for-playground +browser+ 30000)]
  :teardown [(h/stop +browser+)
             ((:stop +server+))]})

^{:refer hara.runtime.js-playground.client/mount! :added "4.0" :timeout 60000}
(fact "mount! loads the playground and exposes the global PLAYGROUND api"

  @(chromedriver/evaluate +browser+ "typeof globalThis.PLAYGROUND")
  => (contains {"value" "object"})

  @(chromedriver/evaluate +browser+ "typeof globalThis.PLAYGROUND.send")
  => (contains {"value" "function"})

  @(chromedriver/evaluate +browser+ "document.getElementById('root').childElementCount > 0")
  => (contains {"value" true}))

^{:refer hara.runtime.js-playground.client/get-config :added "4.0" :timeout 60000}
(fact "get-config reads the playground config from globalThis.PLAYGROUND_CONFIG"

  @(chromedriver/evaluate +browser+ "globalThis.PLAYGROUND_CONFIG.title")
  => (contains {"value" "JS Playground"})

  @(chromedriver/evaluate +browser+ "globalThis.PLAYGROUND_CONFIG.tabs[0].id")
  => (contains {"value" "stage"}))

^{:refer hara.runtime.js-playground.client/App :added "4.0" :timeout 60000}
(fact "App renders the playground shell with topbar, tab bar and sidebar"

  @(chromedriver/evaluate +browser+ "document.getElementById('topbar') !== null")
  => (contains {"value" true})

  @(chromedriver/evaluate +browser+ "document.querySelectorAll('button').length > 0")
  => (contains {"value" true})

  @(chromedriver/evaluate +browser+ "document.getElementById('sidebar') !== null")
  => (contains {"value" true}))

^{:refer hara.runtime.js-playground.client/TopMenu :added "4.0" :timeout 60000}
(fact "TopMenu displays title and compact connected status"

  @(chromedriver/evaluate +browser+ "document.querySelector('#topbar').textContent.includes('JS Playground')")
  => (contains {"value" true})

  @(chromedriver/evaluate +browser+ "document.querySelector('#topbar button').textContent.includes('connected')")
  => (contains {"value" true}))

^{:refer hara.runtime.js-playground.client/TabBar :added "4.0" :timeout 60000}
(fact "TabBar renders configured tabs and tab lifecycle functions work"

  @(chromedriver/evaluate +browser+ "Array.from(document.querySelectorAll('button')).some(b => b.textContent === 'Stage')")
  => (contains {"value" true})

  @(chromedriver/evaluate +browser+ "globalThis.PLAYGROUND.createTab('custom', 'Custom').id")
  => (contains {"value" "custom"})

  @(chromedriver/evaluate +browser+ "Array.from(document.querySelectorAll('button')).some(b => b.textContent === 'Custom')")
  => (contains {"value" true})

  @(chromedriver/evaluate +browser+ "globalThis.PLAYGROUND.closeTab('custom')")
  => (contains {"value" "custom"})

  @(chromedriver/evaluate +browser+ "globalThis.PLAYGROUND.getActiveTab()")
  => (contains {"value" "stage"}))

^{:refer hara.runtime.js-playground.client/ActiveTabPanel :added "4.0" :timeout 60000}
(fact "ActiveTabPanel renders stage and custom tab content"

  @(chromedriver/evaluate +browser+ "globalThis.PLAYGROUND.switchTab('stage')")
  => (contains {"value" nil})

  @(chromedriver/evaluate +browser+ "globalThis.PLAYGROUND.setStage(React.createElement('div', null, 'custom-stage-xyz'))")
  => (contains {"value" nil})

  @(chromedriver/evaluate +browser+ "document.body.textContent.includes('custom-stage-xyz')")
  => (contains {"value" true})

  @(chromedriver/evaluate +browser+ "JSON.stringify(globalThis.PLAYGROUND.createTab('custom', 'Custom'))")
  => (contains {"value" "{\"id\":\"custom\",\"label\":\"Custom\"}"})

  @(chromedriver/evaluate +browser+ "globalThis.PLAYGROUND.setTabContent('custom', React.createElement('div', null, 'custom-tab-xyz'))")
  => (contains {"value" nil})

  @(chromedriver/evaluate +browser+ "document.body.textContent.includes('custom-tab-xyz')")
  => (contains {"value" true})

  @(chromedriver/evaluate +browser+ "globalThis.PLAYGROUND.switchTab('stage')")
  => (contains {"value" nil}))

^{:refer hara.runtime.js-playground.client/format-body :added "4.0" :timeout 60000}
(fact "format-body formats strings and json values for display"

  (!.js (client/format-body "hello"))
  => "hello"

  (!.js (client/format-body {"a" 1}))
  => #"\"a\"\s*:\s*1")

^{:refer hara.runtime.js-playground.client/eval-body :added "4.0" :timeout 60000}
(fact "eval-body evaluates js expressions and returns status/value pairs"

  (!.js (client/eval-body "1 + 2"))
  => ["ok" 3]

  (!.js (client/eval-body "'hello'"))
  => ["ok" "hello"])

^{:refer hara.runtime.js-playground.client/make-add-message :added "4.0" :timeout 60000}
(fact "make-add-message prepends new messages so the latest is first"

  (!.js
    (var messagesRef {"current" []})
    (var setMessages (fn [ms] (:= (. messagesRef current) ms)))
    (var add (client/make-add-message messagesRef setMessages))
    (add {"id" "a"})
    (add {"id" "b"})
    (. messagesRef current))
  => [{"id" "b"} {"id" "a"}])

^{:refer hara.runtime.js-playground.client/make-tab-id :added "4.1" :timeout 60000}
(fact "make-tab-id returns a unique tab id"

  (!.js (client/make-tab-id "hello"))
  => #"tab-hello-")

^{:refer hara.runtime.js-playground.client/find-tab-index :added "4.1" :timeout 60000}
(fact "find-tab-index finds the index of a tab by id"

  (!.js (client/find-tab-index [{"id" "a"} {"id" "b"}] "b"))
  => 1

  (!.js (client/find-tab-index [{"id" "a"}] "b"))
  => -1)

^{:refer hara.runtime.js-playground.client/has-tab? :added "4.1" :timeout 60000}
(fact "has-tab? checks whether a tab exists"

  (!.js (client/has-tab? [{"id" "a"}] "a"))
  => true

  (!.js (client/has-tab? [{"id" "a"}] "b"))
  => false)

^{:refer hara.runtime.js-playground.client/run-eval :added "4.0" :timeout 60000}
(fact "run-eval records eval status and reply on the message entry"

  (!.js
    (var ws {"send" (fn [data] data)})
    (var messagesRef {"current" []})
    (var setMessages (fn [ms] (:= (. messagesRef current) ms)))
    (var add (client/make-add-message messagesRef setMessages))
    (client/run-eval ws add messagesRef setMessages {"id" "x" "body" "1 + 1"})
    (. messagesRef current))
  => (contains [(contains {"status" "ok" "sent" "1 + 1"})]))

^{:refer hara.runtime.js-playground.client/MessageItem :added "4.0" :timeout 60000}
(fact "MessageItem is a React component function"

  (!.js (typeof client/MessageItem))
  => "function")

^{:refer hara.runtime.js-playground.client/MessageList :added "4.0" :timeout 60000}
(fact "MessageList is a React component function"

  (!.js (typeof client/MessageList))
  => "function")

^{:refer hara.runtime.js-playground.client/send-response :added "4.0" :timeout 60000}
(fact "send-response delivers websocket replies back to the runtime"

  (!.js
    (var sent nil)
    (var ws {"send" (fn [data] (:= sent data))})
    (client/send-response ws "abc" "ok" 42)
    sent)
  => #"\"id\".*\"abc\".*\"status\".*\"ok\".*\"body\".*42")

^{:refer hara.runtime.js-playground.client/make-websocket :added "4.0" :timeout 60000}
(fact "make-websocket creates a WebSocket and wires handlers"

  (!.js (typeof (client/make-websocket "ws://localhost/ws" (fn [s]) (fn [m]) {"current" []} (fn [ms]))))
  => "object")
