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
      (:= (. globalThis ["__js_worker_tabs_counter__"]) 0)
      (:= (. globalThis ["onconnect"])
          (fn [e]
            (var port (. e ["ports"] [0]))
            (. port (start))
            (var counter (+ 1 (. globalThis ["__js_worker_tabs_counter__"])))
            (:= (. globalThis ["__js_worker_tabs_counter__"]) counter)
            (:= (. port ["onmessage"])
                (fn [evt]
                  (var data (. evt ["data"]))
                  (. port (postMessage {"echo" data
                                        "counter" counter})))))))
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
  :added "4.1"}
(fact "minimum sharedworker interaction posts and receives a message"
  (notify/wait-on [:js 10000]
    (var blob (new Blob [(@! +sharedworker-script+)] {"type" "text/javascript"}))
    (var url (. (!:G URL) (createObjectURL blob)))
    (var shared (new SharedWorker url))
    (var port (. shared ["port"]))
    (. port (start))
    (:= (. port ["onmessage"])
        (fn [e]
          (repl/notify (. e ["data"]))))
    (. port (postMessage "hello")))
  => {"echo" "hello"
      "counter" 1})

^{:refer js.worker.e2e-sharedworker-basic-test/two-tabs-share-sharedworker
  :added "4.1"}
(fact "two chromedriver tabs talk to the same sharedworker instance"
  (def +tab-a+ (chromedriver/current-tab (l/rt :js)))
  (def +tab-b+ (chromedriver/tab-create (l/rt :js) +notify-url+))

  ;; ensure tab b is fully loaded on the same origin before it reads localStorage
  (chromedriver/tab-switch (l/rt :js) +tab-b+)
  (chromedriver/goto +notify-url+ 4000)
  (chromedriver/tab-switch (l/rt :js) +tab-a+ {:bootstrap false})

  ;; create the shared blob url once in tab a; both tabs will load the same worker
  (create-worker-url)

  (def result-a
    (chromedriver/with-tab (l/rt :js) +tab-a+
      (notify/wait-on [:js 10000]
        (var shared (new SharedWorker (. (!:G localStorage) (getItem "__js_worker_basic_url__"))))
        (var port (. shared ["port"]))
        (. port (start))
        (:= (. globalThis ["__basic_port__"]) port)
        (:= (. port ["onmessage"])
            (fn [e]
              (repl/notify (. e ["data"]))))
        (. port (postMessage "tab-a")))))

  (def result-b
    (chromedriver/with-tab (l/rt :js) +tab-b+
      (notify/wait-on [:js 10000]
        (var shared (new SharedWorker (. (!:G localStorage) (getItem "__js_worker_basic_url__"))))
        (var port (. shared ["port"]))
        (. port (start))
        (:= (. globalThis ["__basic_port__"]) port)
        (:= (. port ["onmessage"])
            (fn [e]
              (repl/notify (. e ["data"]))))
        (. port (postMessage "tab-b")))))

  (chromedriver/tab-close (l/rt :js) +tab-b+)
  (chromedriver/tab-switch (l/rt :js) +tab-a+ {:bootstrap false})

  result-a => {"echo" "tab-a"
               "counter" 1}
  result-b => {"echo" "tab-b"
               "counter" 2})
