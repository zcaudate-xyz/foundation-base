(ns std.log.profile-test
  (:use [code.test :exclude [run]])
  (:require [std.log.profile :refer :all]
            [std.log.common :as common]
            [std.log.core :as core]
            [std.log.console :as console]))

^{:refer std.log.profile/spy-fn :added "3.0"}
(fact "constructs a spy function with print on start and end"

  (spy-fn (common/default-logger) (fn [] 1) :info {})
  => 1)

^{:refer std.log.profile/bind-trace :added "3.0"}
(fact "binds a trace id to the log"

  (bind-trace true (fn [] common/*context*))
  => (contains {:trace/id string?}))

^{:refer std.log.profile/spy-form :added "3.0"}
(fact "helper function for the `spy` macro"

  (spy-form :info nil '(+ 1 2) '(+ 1 2) `(common/default-logger) {} {})
  => seq?)

^{:refer std.log.profile/defspy :added "3.0"}
(fact "creates a spy macro"

  (defspy myspy :info {})
  (myspy 1)
  => 1)

^{:refer std.log.profile/on-grey :added "3.0"}
(fact "produces an on-grey style for console label"

  (on-grey :red)
  => {:text :red :highlight false :bold true :background :grey})

^{:refer std.log.profile/on-white :added "3.0"}
(fact "produces an on-white style for console label"

  (on-white :red)
  => {:text :red :highlight true :bold false :background :white})

^{:refer std.log.profile/on-color :added "3.0"}
(fact "produces an on-color style for console label"

  (on-color :red)
  => {:text :grey :highlight true :bold false :background :red})

^{:refer std.log.profile/item-style :added "3.0"}
(fact "styles the actual log entry"

  (item-style {:label "L"})
  => (contains {:console/style {:header {:label "L"}}}))

^{:refer std.log.profile/meter-fn :added "3.0"}
(fact "constructs a meter function"

  (meter-fn (common/default-logger) (fn [] 1) :info {})
  => 1)

^{:refer std.log.profile/meter-form :added "3.0"}
(fact "helper to the `meter` macro"

  (meter-form :info nil '(+ 1 2) '(+ 1 2) `(common/default-logger) {} {})
  => seq?)

^{:refer std.log.profile/defmeter :added "3.0"}
(fact "creates a meter macro"

  (defmeter mymeter :info {})
  (mymeter 1)
  => 1)

(comment
  (note 1)

  (spy 2)
  (trace 1)
  (status (Thread/sleep 10000))
  (show {:a 1 :b 1})
  (show (ex-info "EUOEUeo" {}))
  (show (ex-info "EUOEUeo" {}))
  (todo 123)

  (binding [common/*trace* true]

    (section {:log/label "GETTING DONE WITH THIS"}
             (block
              (trace
               (trace
                (trace 1))))))
  (std.log/with-logger-basic
    (trace 1))
  (meter (+ 1 2 3))

  (std.log/with-logger-verbose
    (silent (do ;(Thread/sleep 1000)
              1)))
  (section
   (do (block 12)
       (block 12)
       (block 12)
       (block 12)
       (block 12))))

(comment
  (std.log/with-logger-basic
    (task 1))
  (std.log/fatal "oeuoe")
  (task "HELLO" nil)
  (block "OEUOEU")
  (meter "OEUOUEEO")
  (profile
      ;;(block "Oeoeu")
   (status "aeuoeu"))
  (note "THIS IS KIND OF COOL" nil)

  (silent "OEUOEUO")
  (track "UUEUE"
         :OEUOEUO)
  (status "CHECK FROM DATABASE"
          (comment (CHECK DATABASE {:host "localhost" :post 123}))
    ;;:HAEOUAOUE
          )
  (trace "OEUOEU")

  (meter "aeuoeu")
  (alter-var-root #'common/*trace* (fn [_] true))
  (alter-var-root #'common/*trace* (fn [_] false))
  (binding [common/*trace* true]
    (profile {:log/label "SOMETHING UP"}
             (spy "OUOEU"
                  (+ 1 2 3))))
  (meta #'meter)
  (./scaffold)
  (binding [common/*logger* (std.log.core/basic-logger)
            common/*custom* {:trace true :throw false}]
    (spy ;;"HELLO"
         ;;(throw (ex-info "OEUOEUOE" {}))
     (+ 1 2 3)))

  (std.log/with-logger-basic
    (std.log/info "OEUoeu"))

  (std.log/with-logger-basic
    (trace {:log/throw false :log/message "HELLO"}
           (trace "HELLO")
           ;;(+ 1 2 3)
           ))

  (std.log/with-logger-basic

    (profile {:log/throw false :log/message "HELLO"}
             (profile
              (trace
               (spy (println "HELLO"))))
             ;;(+ 1 2 3)
             )

    (show "OEUOEU")
    (meter "OEUOeu")))

(comment

  (profile (trace (+ 1 2 3))))

(comment

  (macroexpand-1 '(spy ;;"hello there"
                   {:a (+ 1 2 3)
                    :b (+ 2 3 4)}))

  (trace {:log/level :info}
         (trace {:log/level :debug}
                (trace {:log/level :fatal}
                       (+ 1 2))))

  (spy ;;"hello there"
   {:a (+ 1 2 3)
    :b (+ 2 3 4)})
  (try
    (spy ;;"hello there"
     (throw (ex-info "HELLO" {})))
    (catch Throwable t))

  (binding [common/*logger* (std.log.core/basic-logger)]
    (meter "HELLO"
           {:log/level :fatal
            :log/value 3}
           (+ 1 2 3)))
  6
  (test

   (spy (do (spy (+ 1 2 3))
            (spy (throw (ex-info "HELLO" {}))))))

  (log "hello there"
       (+ 1 2 3)))
