(ns std.lib.signal-test
  (:use code.test)
  (:require [std.lib.signal :refer :all]))

^{:refer std.lib.signal/new-id :added "3.0"}
(fact "creates a random id with a keyword base"
  (new-id)
  ;;=> :06679506-1f87-4be8-8cfb-c48f8579bc00
  )

^{:refer std.lib.signal/expand-data :added "3.0"}
(fact "expands shorthand data into a map"

  (expand-data :hello)
  => {:hello true}

  (expand-data [:hello {:world "foo"}])
  => {:world "foo", :hello true})

^{:refer std.lib.signal/check-data :added "3.0"}
(fact "checks to see if the data corresponds to a template"

  (check-data {:hello true} :hello)
  => true

  (check-data {:hello true} {:hello true?})
  => true

  (check-data {:hello true} '_)
  => true

  (check-data {:hello true} #{:hello})
  => true)

^{:refer std.lib.signal/manager :added "3.0"}
(fact "creates a new manager"
  (manager)
  ;; => #std.lib.signal.Manager{:id :b56eb2c9-8d21-4680-b3e1-0023ae685d2b,
  ;;                               :store [], :options {}}

  => any?
  => any?)

^{:refer std.lib.signal/remove-handler :added "3.0"}
(fact "adds a handler to the manager"
  (-> (add-handler (manager) :hello {:id :hello
                                     :handler identity})
      (remove-handler :hello)
      (match-handlers {:hello "world"}))
  => ())

^{:refer std.lib.signal/add-handler :added "3.0"}
(fact "adds a handler to the manager"
  (-> (add-handler (manager) :hello {:id :hello
                                     :handler identity})
      (match-handlers {:hello "world"})
      (count))
  => 1)

^{:refer std.lib.signal/list-handlers :added "3.0"}
(fact "list handlers that are present for a given manager"

  (list-handlers (manager))
  => [])

^{:refer std.lib.signal/match-handlers :added "3.0"}
(fact "match handlers for a given manager"

  (-> (add-handler (manager) :hello {:id :hello
                                     :handler identity})
      (match-handlers {:hello "world"}))
  => (contains-in [{:id :hello
                    :handler fn?
                    :checker :hello}]))

^{:refer std.lib.signal/signal:clear :added "3.0"}
(fact "clears all signal handlers"
  (resolve 'signal:clear) => var?)

^{:refer std.lib.signal/signal:list :added "3.0"}
(fact "lists all signal handlers"
  (resolve 'signal:list) => var?)

^{:refer std.lib.signal/signal:install :added "3.0"}
(fact "installs a signal handler")

^{:refer std.lib.signal/signal:uninstall :added "3.0"}
(fact "uninstalls a signal handler")

^{:refer std.lib.signal/signal :added "3.0"}
(fact "signals an event")

^{:refer std.lib.signal/signal:with-temp :added "3.0"
  :style/indent 1}
(fact "uses a temporary signal manager for testing")
