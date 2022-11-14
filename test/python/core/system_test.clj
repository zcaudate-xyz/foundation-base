(ns python.core.system-test
  (:use code.test)
  (:require [std.lib :as h]
            [std.lang :as l]))

(l/script- :python
  {:runtime :basic #_:websocket
   :config {:bench true}
   :require [[python.core :as y]]})

(fact:global
 {:setup    [(l/rt:restart)
             (Thread/sleep 500)]
  :teardown [(l/rt:stop)]})

^{:refer python.core.system/thread :added "4.0"}
(fact "creates a thread"
  ^:hidden

  (str (y/thread '(fn [] 1)))
  => #"<Thread")

^{:refer python.core.system/thread:run :added "4.0"}
(fact "runs a thread"
  ^:hidden
  
  (str (y/thread:run '(fn [] 1)))
  => "")

^{:refer python.core.system/pkg-load :added "4.0"}
(fact "loads a package using __loader__"
  ^:hidden

  (str (y/pkg-load "os"))
  => #"<module 'os'")

^{:refer python.core.system/pkg :added "4.0"}
(fact "loads a package using importlib"
  ^:hidden

  (str (y/pkg "os"))
  => #"<module 'os'")

^{:refer python.core.system/pkg:dir :added "4.0"}
(fact "returns a listing of package members"
  ^:hidden
  
  (y/pkg:dir "os")
  => vector?)

^{:refer python.core.system/sys:version-info :added "4.0"}
(fact "gets the system version"
  ^:hidden
  
  (y/sys:version-info)
  => (contains [3]))

^{:refer python.core.system/sys:path :added "4.0"}
(fact "gets the system path"
  ^:hidden
  
  (y/sys:path)
  => vector?)

^{:refer python.core.system/site:packages :added "4.0"}
(fact "list all site packages"
  ^:hidden
  
  (y/site:packages)
  => vector?)

^{:refer python.core.system/site:builtins :added "4.0"}
(fact "list all builtin modules"
  ^:hidden
  
  (y/site:builtins)
  => vector?)

^{:refer python.core.system/site:install :added "4.0"}
(fact "installs a package with pip")

^{:refer python.core.system/site:uninstall :added "4.0"}
(fact "uninstalls a package with pip")

^{:refer python.core.system/site:list-all :added "4.0"}
(fact "lists all site packages"
  ^:hidden
  
  (y/site:list-all)
  => vector?)

^{:refer python.core.system/site:list-toplevel :added "4.0"}
(fact "lists alll top level packages"
  ^:hidden
  
  (y/site:list-toplevel)
  => vector?)

^{:refer python.core.system/fn:signature :added "4.0"}
(fact "returns the signature of a function"
  ^:hidden
  
  (read-string
   (str (y/fn:signature '(fn [x] (+ 1 x)))))
  => '(x))

^{:refer python.core.system/fn:argspec :added "4.0"}
(fact "gets the argspect of a function"
  ^:hidden
  
  (y/fn:argspec '(fn [x] (+ 1 x)))
  => [["x"] nil nil nil [] nil {}]

  (y/fn:argspec '(. '(1) __lt__))
  => [["self" "value"] nil nil nil [] nil {}])

^{:refer python.core.system/js:dumps :added "4.0"}
(fact "converts python to json"
  ^:hidden
  
  (y/js:dumps {"a" 1})
  => "{\"a\": 1}")

^{:refer python.core.system/js:loads :added "4.0"}
(fact "loads python from json"
  ^:hidden
  
  (y/js:loads "{\"a\": 1}")
  => {"a" 1})

^{:refer python.core.system/uuid :added "4.0"}
(fact "returns a uuid"
  ^:hidden
  
  (y/uuid)
  => string?)

^{:refer python.core.system/g:out :added "4.0"}
(fact "gets the current global output"
  ^:hidden
  
  (y/g:out)
  => anything)


(comment
  (./import)
  )
