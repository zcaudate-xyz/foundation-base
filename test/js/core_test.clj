(ns js.core-test
  (:use code.test)
  (:require [std.lang :as l]
            [xt.lang.base-notify :as notify]))

(l/script- :js
  {:runtime :basic
   :require [[js.core :as j]
             [xt.lang.base-lib :as k]
             [xt.lang.base-repl :as repl]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer js.core/assignNew :added "4.0"}
(fact "assigns new object"
  ^:hidden
  
  (j/assignNew {:a 1} {:b 2})
  => {"a" 1, "b" 2})

^{:refer js.core/future :added "4.0"}
(fact "creates a future"
  ^:hidden
  
  (notify/wait-on :js
    (j/future (repl/notify true)))
  => true)

^{:refer js.core/future-delayed :added "4.0"}
(fact "creates a future delayed call"
  ^:hidden
  
  (notify/wait-on :js
                  (j/future-delayed [100]
      (repl/notify true)))
  => true)

^{:refer js.core/timeout :added "4.0"}
(fact "creates a timeout call")

^{:refer js.core/isWeb :added "4.0"}
(fact "checks that platform is web")

^{:refer js.core/randomColor :added "4.0"}
(fact "creates a random color"
  ^:hidden
  
  (j/randomColor)
  => string?)

^{:refer js.core/randomId :added "4.0"}
(fact "creates a random id"
  ^:hidden
  
  (j/randomId 10)
  => string?)

^{:refer js.core/asyncFn :added "4.0"}
(fact "creates an async function")

^{:refer js.core/import-missing :added "4.0"}
(fact "generates all depenent imports missing from current namespace")

^{:refer js.core/import-set-global :added "4.0"}
(fact "sets all dependent imports to global")

^{:refer js.core/arrayify :added "4.0"}
(fact "makes an array"

  (j/arrayify 1)
  => [1])

^{:refer js.core/identity :added "4.0"}
(fact "identity function"
  ^:hidden
  
  (j/identity 1)
  => 1)

^{:refer js.core/async :added "4.0"}
(fact "performs an async call"
  ^:hidden
  
  (j/<! (j/async
         (j/future
           (return (+ 1 2 3)))
         [[x] (+ 1 x)]
         [[x] (+ 1 x)]
         [[x] (+ 1 x)]))
  => 9

  (j/<! (j/async
         (j/future
           (return (+ 1 2 3)))
         [[x]
          (+ 1 x)
          (+ 1 x)]))
  => 7)

^{:refer js.core/wrap:print :added "4.0"}
(fact "wraps a form in a print statement"
  ^:hidden
  
  ((:template @j/wrap:print) 'f)
  => '(fn [...args]
        (console.log "INPUT" args)
        (var out (f ...args))
        (console.log "OUTPUT" out) (return out)))

^{:refer js.core/settle :added "4.0"}
(fact "notify on future"
  ^:hidden
  
  (notify/wait-on :js
    (j/settle (repl/>notify)
              (j/future-delayed [100]
                (return 1))))
  => 1)

^{:refer js.core/notify :added "4.0"}
(fact "notify on future"
  ^:hidden
  
  (notify/wait-on :js
    (j/notify (j/future-delayed [100]
                (return 1))))
  => 1)

^{:refer js.core/notify-api :added "4.0"}
(fact "notify on api return"
  ^:hidden
  
  (notify/wait-on :js
    (j/notify-api (j/future-delayed [100]
                    (return {:status "ok"
                             :data 1}))))
  => 1

  (notify/wait-on :js
    (j/notify-api (j/future-delayed [100]
                    (return {:status "error"
                             :data 1}))))
  => {"status" "error", "data" 1})

^{:refer js.core/STACKTRACE! :added "4.0"}
(fact "Adds a trace entry with stack infomation"
  ^:hidden
  
  (!.js
   (j/STACKTRACE! "hello")
   (k/trace-last))
  => string?)

^{:refer js.core/LOG! :added "4.0"}
(fact "like `xt.lang.base-lib/LOG!` but also for promises")

^{:refer js.core/<! :added "4.0"}
(fact "shortcut for notify/wait-on"
  ^:hidden
  
  (j/<! (j/future-delayed [100]
          (return 1)))
  => 1)

^{:refer js.core/<api :added "4.0"}
(fact "shortcut for notify/wait-on for api calls"
  ^:hidden
  
  (j/<api 
   (j/future-delayed [100]
     (return {:status "ok"
              :data 1})))
  => 1)

^{:refer js.core/module:async :added "4.0"}
(fact "wraps an esm import to split out components"
  ^:hidden
  
  (j/<!
   (. (j/module:async
       (j/future-delayed [100]
         (return
          {"__esMODULE" true
           :default {:hello "HELLO"}})))
      ["hello"]))
  => {"__esMODULE" true, "default" "HELLO"})

^{:refer js.core/module:demos :added "4.0"}
(fact "adds a demos module")

(comment
  (!.js
   (j/parseFloat "xoeko"))
  (!.js
   (k/last "hello.")))
