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

(defn.js create-worker-url
  "Creates a fresh blob URL for the sharedworker script in the current tab."
  {:added "4.1"}
  []
  (var blob (new Blob [(@! +sharedworker-script+)]
                 {"type" "text/javascript"}))
  (var url (. (!:G URL) (createObjectURL blob)))
  (. (!:G localStorage) (setItem "__js_worker_basic_url__" url))
  (return url))

(fact:global
 {:setup [(l/rt:restart :js)
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
  
  #_(chromedriver/tab-close (l/rt :js) +tab-b+)
  #_(chromedriver/tab-switch (l/rt :js) +tab-a+ {:bootstrap false})

  result-a => {"echo" "tab-a"
               "counter" 1}
  result-b => {"echo" "tab-b"
               "counter" 2})

^{:refer js.worker.e2e-sharedworker-basic-test/three-tabs-sharedworker-persists
  :added "4.1"}
(fact "sharedworker keeps counting after connecting tabs are closed"

  ;; use the current tab as a manager; it creates the blob url and stays open
  ;; so the url remains valid after the worker tabs are closed
  (def +manager-tab+ (chromedriver/current-tab (l/rt :js)))
  (create-worker-url)

  (def +tab-a+ (chromedriver/tab-create (l/rt :js) +notify-url+))
  (chromedriver/tab-switch (l/rt :js) +tab-a+)
  (chromedriver/goto +notify-url+ 4000)

  (def +tab-b+ (chromedriver/tab-create (l/rt :js) +notify-url+))
  (chromedriver/tab-switch (l/rt :js) +tab-b+)
  (chromedriver/goto +notify-url+ 4000)

  ;; connect tab a
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

  ;; connect tab b
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

  ;; close both connected tabs. the sharedworker may outlive the tabs, so tab c
  ;; reconnecting to the same url should see the counter continue from 3.
  (chromedriver/tab-close (l/rt :js) +tab-a+)
  (chromedriver/tab-close (l/rt :js) +tab-b+)

  ;; switch back to the manager tab so the runtime has a valid active tab
  (chromedriver/tab-switch (l/rt :js) +manager-tab+ {:bootstrap false})

  ;; open a third tab and connect to the same stored url
  (def +tab-c+ (chromedriver/tab-create (l/rt :js) +notify-url+))
  (chromedriver/tab-switch (l/rt :js) +tab-c+)
  (chromedriver/goto +notify-url+ 4000)

  (def result-c
    (chromedriver/with-tab (l/rt :js) +tab-c+
      (notify/wait-on [:js 10000]
        (var shared (new SharedWorker (. (!:G localStorage) (getItem "__js_worker_basic_url__"))))
        (var port (. shared ["port"]))
        (. port (start))
        (:= (. globalThis ["__basic_port__"]) port)
        (:= (. port ["onmessage"])
            (fn [e]
              (repl/notify (. e ["data"]))))
        (. port (postMessage "tab-c")))))

  (chromedriver/tab-close (l/rt :js) +tab-c+)
  (chromedriver/tab-switch (l/rt :js) +manager-tab+ {:bootstrap false})

  result-a => {"echo" "tab-a"
               "counter" 1}
  result-b => {"echo" "tab-b"
               "counter" 2}
  result-c => {"echo" "tab-c"
               "counter" 3})
