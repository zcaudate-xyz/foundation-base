(ns js.worker.e2e-sharedworker-basic-test
  (:use code.test)
  (:require [hara.lang :as l]
            [hara.runtime.chromedriver :as chromedriver]
            [xt.lang.common-notify :as notify]))

(l/script- :js
  {:runtime :chromedriver.instance
   :require [[xt.lang.common-repl :as repl]
             [xt.lang.spec-base :as xt]]})

(def ^:private +sharedworker-script+
  (l/emit-script
   '(do
      (:= (. globalThis ["onconnect"])
          (fn [e]
            (var port (. e ["ports"] [0]))
            (. port (start))
            (:= (. port ["onmessage"])
                (fn [evt]
                  (. port (postMessage (. evt ["data"]))))))))
   {:lang :js
    :layout :flat}))

(def ^:private +notify-url+
  (str "http://127.0.0.1:" (:http-port (l/default-notify)) "/"))

(defn create-worker-url
  "Creates a fresh blob URL for the sharedworker script in the current tab."
  {:added "4.1"}
  []
  (!.js
    (var blob (new Blob [(@! +sharedworker-script+)]
                       {"type" "text/javascript"}))
    (var url (. (!:G URL) (createObjectURL blob)))
    (. (!:G localStorage) (setItem "__js_worker_basic_url__" url))
    (return url)))

(fact:global
 {:setup [(l/rt:restart :js)
          (l/rt:scaffold-imports :js)
          (chromedriver/goto +notify-url+ 4000)]
  :teardown [(l/rt:stop)]})

^{:refer js.worker.e2e-sharedworker-basic-test/basic-sharedworker-message-passing
  :added "4.1"
  :setup [(create-worker-url)]}
(fact "minimum sharedworker interaction posts and receives a message"
  (notify/wait-on [:js 5000]
    (var worker-url (. (!:G localStorage) (getItem "__js_worker_basic_url__")))
    (var shared (new SharedWorker worker-url))
    (var port (. shared ["port"]))
    (. port (start))
    (:= (. port ["onmessage"])
        (fn [e]
          (repl/notify (. e ["data"]))))
    (. port (postMessage "hello")))
  => "hello")


