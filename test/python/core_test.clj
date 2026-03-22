(ns python.core-test
  (:require [std.json :as json]
            [std.lang :as l]
            [std.lib.component :as component])
  (:use code.test))

(l/script- :python
  {:runtime :basic
   :require [[python.core :as y]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})




(comment
  ^{:refer python.core/eval-return :added "4.0"}
(fact "evaluates a function based on return"
  ^:hidden

  (json/read 
   (y/eval-return '(fn [] (return 1))
                  []))
  => {"type" "data"
      "value" 1, }

  (json/read
   (y/eval-return '(fn [] (exec "1+in"))
                  []))
  => {"type" "error"
      "value" "invalid syntax (<string>, line 1)",}

  (json/read
   (y/eval-return '(fn [] (return (__import__ "os")))
                  []))
  => (contains
      {"type" "raw"
       "value" #"<module 'os'"}))

  ^{:refer python.core/eval-fn :added "4.0"}
  (fact "evaluation function with"

    (y/eval-fn "globals()[\"OUT\"] = 1+1")
    => 2)
  
  ^{:refer python.core/wrepl-connect :added "4.0"
    :setup [(def -w- (wrepl/wrepl-python))]
    :teardown [(component/stop -w-)]}
  (fact "connects to an wrepl server, allows for multiple connections"
    ^:hidden
    
    (do (y/wrepl-connect (wrepl/get-port :python (:id -w-))
                         {})
        (wrepl/wait-ready :python (:id -w-)))
    => true
    
    (defn.py add [x y]
      (return (+ x y)))
    
    (:body @(wrepl/raw-eval -w- "globals()[\"OUT\"] = 10+10"))
    => "{\"type\": \"data\", \"value\": 20}"
    
    (wrepl/invoke-ptr -w- add [1 2])
    => 3))
