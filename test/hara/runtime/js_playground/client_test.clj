(ns hara.runtime.js-playground.client-test
  (:use code.test)
  (:require [hara.runtime.js-playground.client :refer :all]
            [hara.runtime.js-playground :as playground]
            [hara.runtime.chromedriver :as chromedriver]
            [hara.common.util :as ut]
            [hara.lang :as l]
            [std.lib.component :as component]
            [std.lib.env :as env]
            [std.lib :as h]))

(l/script- :js
  {:runtime :playground
   :config {:port 0
            :host "localhost"}})

(defn- playground-rt []
  (ut/lang-rt :js))

(defn- chrome-available? []
  (or (System/getenv "CHROME")
      (env/program-exists? "google-chrome-stable")
      (env/program-exists? "google-chrome")
      (env/program-exists? "chromium")
      (env/program-exists? "chromium-browser")
      (env/program-exists? "chrome-headless-shell")))

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

(defn- playground-connected?
  "true if the playground page has connected over the websocket"
  [browser]
  (= "connected" (wait-for-status browser 30000)))

(defn- sidebar-text [browser]
  (get @(chromedriver/evaluate browser "document.getElementById('sidebar').textContent")
       "value"))

(defn- message-count [browser]
  (get @(chromedriver/evaluate browser "window.PLAYGROUND.getMessages().length")
       "value"))

(fact:global
 {:skip (not (chrome-available?))
  :setup [(def +playground+ (playground-rt))
          (def +browser+ (chromedriver/browser {}))
          (chromedriver/goto (playground/play-url +playground+) 30000 +browser+)
          (wait-for-status +browser+ 30000)]
  :teardown [(h/stop +browser+)
             (component/stop +playground+)]})

^{:refer hara.runtime.js-playground.client/mount! :added "4.0" :timeout 60000}
(fact "mount! loads the playground and exposes the global PLAYGROUND api"

  (playground-connected? +browser+)
  => true

  @(chromedriver/evaluate +browser+ "typeof window.PLAYGROUND")
  => (contains {"value" "object"})

  @(chromedriver/evaluate +browser+ "typeof window.PLAYGROUND.send")
  => (contains {"value" "function"})

  @(chromedriver/evaluate +browser+ "document.getElementById('root').childElementCount > 0")
  => (contains {"value" true}))

^{:refer hara.runtime.js-playground.client/get-config :added "4.0" :timeout 60000}
(fact "get-config reads the playground config from window.PLAYGROUND_CONFIG"

  @(chromedriver/evaluate +browser+ "window.PLAYGROUND_CONFIG.title")
  => (contains {"value" "hara.runtime js playground"})

  @(chromedriver/evaluate +browser+ "window.PLAYGROUND_CONFIG.tabs[0].id")
  => (contains {"value" "stage"})

  @(chromedriver/evaluate +browser+ "window.PLAYGROUND_CONFIG.tabs[0].label")
  => (contains {"value" "Stage"}))

^{:refer hara.runtime.js-playground.client/App :added "4.0" :timeout 60000}
(fact "App renders the playground shell with topbar, tab bar and sidebar"

  @(chromedriver/evaluate +browser+ "document.getElementById('topbar') !== null")
  => (contains {"value" true})

  @(chromedriver/evaluate +browser+ "document.querySelectorAll('button').length > 0")
  => (contains {"value" true})

  @(chromedriver/evaluate +browser+ "document.getElementById('sidebar') !== null")
  => (contains {"value" true}))

^{:refer hara.runtime.js-playground.client/TopMenu :added "4.0" :timeout 60000}
(fact "TopMenu displays title, status and message controls"

  @(chromedriver/evaluate +browser+ "document.querySelector('#topbar').textContent.includes('hara.runtime js playground')")
  => (contains {"value" true})

  @(chromedriver/evaluate +browser+ "document.querySelector('#topbar').textContent.includes('connected')")
  => (contains {"value" true})

  @(chromedriver/evaluate +browser+ "document.querySelector('#topbar').textContent.includes('Messages')")
  => (contains {"value" true}))

^{:refer hara.runtime.js-playground.client/TabBar :added "4.0" :timeout 60000}
(fact "TabBar renders configured tabs and switchTab changes the active tab"

  @(chromedriver/evaluate +browser+ "Array.from(document.querySelectorAll('button')).some(b => b.textContent === 'Stage')")
  => (contains {"value" true})

  (!.js (window.PLAYGROUND.switchTab "stage"))
  => nil

  @(chromedriver/evaluate +browser+ "window.PLAYGROUND.getActiveTab()")
  => (contains {"value" "stage"}))

^{:refer hara.runtime.js-playground.client/ActiveTabPanel :added "4.0" :timeout 60000}
(fact "ActiveTabPanel renders stage and custom tab content"

  (!.js (window.PLAYGROUND.switchTab "stage"))
  => nil

  (!.js (window.PLAYGROUND.setStage (React.createElement "div" null "custom-stage-xyz")))
  => nil

  @(chromedriver/evaluate +browser+ "document.body.textContent.includes('custom-stage-xyz')")
  => (contains {"value" true})

  (!.js (window.PLAYGROUND.switchTab "custom"))
  => nil

  (!.js (window.PLAYGROUND.setTabContent "custom" (React.createElement "div" null "custom-tab-xyz")))
  => nil

  @(chromedriver/evaluate +browser+ "document.body.textContent.includes('custom-tab-xyz')")
  => (contains {"value" true})

  (!.js (window.PLAYGROUND.switchTab "stage"))
  => nil)

^{:refer hara.runtime.js-playground.client/eval-body :added "4.0" :timeout 60000}
(fact "eval-body evaluates js expressions and returns status/value pairs"

  (!.js (+ 1 2 3))
  => 6

  (!.js "hello")
  => "hello")

^{:refer hara.runtime.js-playground.client/send-response :added "4.0" :timeout 60000}
(fact "send-response delivers websocket replies back to the runtime"

  (let [before (message-count +browser+)]
    (!.js (+ 10 20))
    => 30

    (message-count +browser+)
    => (+ before 1)))

^{:refer hara.runtime.js-playground.client/run-eval :added "4.0" :timeout 60000}
(fact "run-eval records eval status and reply on the message entry"

  (!.js (+ 1 1))
  => 2

  (get @(chromedriver/evaluate +browser+ "window.PLAYGROUND.getMessages()[0].status")
       "value")
  => "ok"

  (get @(chromedriver/evaluate +browser+ "window.PLAYGROUND.getMessages()[0].reply")
       "value")
  => "2"

  (!.js (throw (new Error "boom")))
  => (contains {:status "error"})

  (get @(chromedriver/evaluate +browser+ "window.PLAYGROUND.getMessages()[0].status")
       "value")
  => "error")

^{:refer hara.runtime.js-playground.client/make-add-message :added "4.0" :timeout 60000}
(fact "make-add-message prepends new messages so the latest is first"

  (let [before (message-count +browser+)]
    (!.js 1)
    => 1

    (!.js 2)
    => 2

    (message-count +browser+)
    => (+ before 2)

    (get @(chromedriver/evaluate +browser+ "window.PLAYGROUND.getMessages()[0].sent")
         "value")
    => "2"))

^{:refer hara.runtime.js-playground.client/format-body :added "4.0" :timeout 60000}
(fact "format-body formats strings and json values for display in the message list"

  (!.js (JSON.parse "{\"a\":1}"))
  => map?

  (sidebar-text +browser+)
  => #"\"a\"\s*:\s*1")

^{:refer hara.runtime.js-playground.client/MessageList :added "4.0" :timeout 60000}
(fact "MessageList renders messages and shows empty state when there are none"

  @(chromedriver/evaluate +browser+ "document.getElementById('sidebar').textContent.includes('No messages yet')")
  => (contains {"value" false})

  (let [fresh (chromedriver/browser {})]
    (chromedriver/goto (playground/play-url +playground+) 30000 fresh)
    (try
      (wait-for-status fresh 30000)
      (get @(chromedriver/evaluate fresh "document.getElementById('sidebar').textContent.includes('No messages yet')")
           "value")
      => true
      (finally
        (h/stop fresh)))))

^{:refer hara.runtime.js-playground.client/MessageItem :added "4.0" :timeout 60000}
(fact "MessageItem renders sent and reply labels for a message"

  (sidebar-text +browser+)
  => #"Sent"

  (sidebar-text +browser+)
  => #"Reply")
