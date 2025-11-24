(ns std.log-test
  (:use code.test)
  (:require [std.log :as log]
            [std.log.common :as common]
            [std.print.format.common :as format.common]
            [std.protocol.log :as protocol.log]))

^{:refer std.log/create :added "3.0"}
(fact "creates a component compatible logger"

  (log/create {:type :basic})
  => (partial satisfies? protocol.log/ILogger))

^{:refer std.log/logger :added "3.0"}
(fact "creates an starts a logger"

  (log/logger {:type :basic})
  => (partial satisfies? protocol.log/ILogger))

^{:refer std.log/logger? :added "3.0"}
(fact "checks if the object is a logger"

  (log/logger? (log/create {:type :basic}))
  => true)

^{:refer std.log/with-indent :added "3.0"}
(fact "executes body with a given indent"

  (log/with-indent 2
    format.common/*indent*)
  => 2)

^{:refer std.log/with-level :added "3.0"}
(fact "executes body with a given level"

  (log/with-level :info
    common/*level*)
  => :info)

^{:refer std.log/with-logger :added "3.0"}
(fact "executes code with a logger"

  (let [l (log/create {:type :basic})]
    (log/with-logger l
      common/*logger*))
  => (partial satisfies? protocol.log/ILogger))

^{:refer std.log/with-overwrite :added "3.0"}
(fact "executes code with a given overwrite"

  (log/with-overwrite {:a 1}
    common/*overwrite*)
  => {:a 1})

^{:refer std.log/current-context :added "3.0"}
(fact "returns the current context"

  (log/with-context {:a 1}
    (log/current-context))
  => (contains {:a 1}))

^{:refer std.log/with-context :added "3.0"}
(fact "executes code with a given context"

  (log/with-context {:a 1}
    common/*context*)
  => (contains {:a 1}))

^{:refer std.log/with :added "3.0"}
(fact "enables targeted printing of statements" ^:hidden

  (binding [common/*logger* (log/logger {:type :identity :level :debug})]
    (log/with {:level :info}
              (log/debug "hello" {})))
  => nil

  (binding [common/*logger* (log/logger {:type :identity :level :debug})]
    (log/with {:level :debug}
              (log/debug "hello" {})))
  => map?

  (binding [common/*logger* (log/logger {:type :identity :level :debug})]
    (log/with {:level #{:debug}}
              (log/info "hello" {})))
  => nil)

^{:refer std.log/with-logger-basic :added "3.0"}
(fact "executes code with the basic logger (for debugging the logger)"

  (log/with-logger-basic
    common/*logger*)
  => (partial satisfies? protocol.log/ILogger))

^{:refer std.log/with-logger-verbose :added "3.0"}
(fact "executes code with a verbose logger (for printing out everything)"

  (log/with-logger-verbose
    common/*level*)
  => :verbose)

(comment
  (./import)
  (./scaffold)

  (defn hello-entry
    ([]
     (log/log :debug "Hello World" {:a 1 :b 2})))

  (log/with-logger (log/logger {:type :basic
                                :level :debug})
    (hello-entry))

  (log/with {:level :debug}
            (hello-entry))

  (log/thrown {:level :debug})

  (log/with-debug
    (log/spy {:stats/id 1 :stats/event-id 2}
             (+ 1 2 3)))

  (log/with-debug
    (log/status {:stats/id 1 :stats/event-id 2}
                (+ 1 2 3)))

  (log/with-debug
    (log/info {:stats/id 1 :stats/event-id 2}
              (+ 1 2 3)))

  (log/with-debug
    (log/note (+ 1 2 3)))

  (log/with-debug
    (log/todo (+ 1 2 3)))

  (log/with-debug
    (log/code (+ 1 2 3)))

  (log/with-debug
    (log/debug (+ 1 2 3)))

  (log/with-debug
    (log/trace (+ 1 2 3)))

  (log/with-debug
    (log/spy (+ 1 2 3)))

  (log/with-debug
    (log/todo (+ 1 2 3)))

  (log/with-debug
    (log/status (+ 1 2 3)))

  (log/with-debug
    (log/status (+ 1 2 3)))

  (log/with-debug
    (log/warn (+ 1 2 3)))

  (log/with-debug
    (log/error (+ 1 2 3)))

  (log/with-debug
    (log/fatal (+ 1 2 3)))

  (log/with-debug
    (log/fatal (+ 1 2 3)))

  (log/with-debug
    (log/note {:stats/id 1 :stats/event-id 2}
              (+ 1 2 3)))

  (log/with-debug
    (log/note {:stats/id 1 :stats/event-id 2}
              (+ 1 2 3)))

  (log/warn "THis is a Warning" {:stats/id "eee"})

  (time (log-function))
  (str/split "std.log$eval60639" #"\$")
  cid
  (time (printe-entry :debug "neueu" {}))

  (error "Hello" {:log/tags #{:hello}}))

(comment
  (with-logger (console/basic-logger)
    (todo "hello" {:rx 1}))

  (with-logger (console/console-logger)
    (todo "hello" {:rx 1}))
  common

  (do (def -cache- (atom {}))
      (def -logger- (logger {:type :cache
                             :publish "HELLO"
                             :cache -cache-})))

  (std.lib.component/stop -logger-)
  (binding [common/*logger* -logger-]
    (info "HELLO"))

  ("HELLO")

  (std.dispatch/submit (:executor @(:instance -logger-))
                       {:data 1})
  (prn (into {} -logger-))

  (info  "HELLO" common/*logger*)
  (core/logger-info-core common/*logger*)
  (with-debug {:namespace {:include [#"log"]}}
    (todo "SERVER STARTED" {:rx 1}))
  (with-debug {:namespace {:include [#"log"]}}
    (trace {:aoeuoaeu 1 "aeouaoeuao" 1} (+ 3 4))))

(comment
  (with-context {:meter/form nil}
    (-> (+ 2 1)
        (+ 1 2 3)
        (spy)
        (+ 1 2 3)))

  (./reset '[std.log])

  (with-logger common/*logger*
    (spy "HELoeuLO"))

  (with-logger (core/basic-logger)
    ;;(with-indent 2)
    (with-context {:meter/form '(+ 1 2)
                   :meter/start 0
                   ;;:meter/duration 10
                   :meter/outcome :success
                   :meter/end 10
                   ;;:meter/end 1
                   :trace/id "a" :trace/root "c" :trace/parent "b"
                   :log/label "METER"
                   :log/progress "START"
                   :log/level :debug}
      (info "MESSAGE" ["HELLO"])))

  (with-context {:meter/form '(+ 1 2)
                 :meter/start 0
                 :meter/duration 10
                 :meter/outcome :success
                 :meter/end 10
                 ;;:meter/end 1
                 :trace/display true
                 :trace/id "a" :trace/root "c" :trace/parent "b"
                 :log/label "METER"
                 :log/progress "END"
                 :log/level :debug}
    (trace "MESSAGE" ["HELLO"]))

  (with-logger-basic
    (profile "HELLO")
    (profile (profile (profile "HELLO"))))

  (profile {:log/message "HELLO" :meter/form nil} ;;{:log/display false}
           {:a 1 :b 2})

  (meter {:log/message "HELLO" :meter/form nil} ;;{:log/display false}
         "HELLO")

  (block "OUOeu")

  (meter {:log/message "HELLO"} ;;{:log/display false}
         "HELLO")

  (spy {:log/message "HELLO"}
       "HELLO")

  (run (status (trace (profile (meter (run (+ 1 2 3)))))))
  (status)

  (with-logger (core/basic-logger)
    (error ""
           {:meter/form '(+ 1 2)
            :meter/start 0
            :meter/duration 10
            :meter/outcome :success
            :meter/end 10
            ;;:meter/end 1
            :trace/display true
            :trace/id "a" :trace/root "c" :trace/parent "b"}
           (ex-info "HELLO" {}))))
