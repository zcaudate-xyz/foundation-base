(ns hara.runtime.basic.impl.process-js-test
  (:require [clojure.string]
            [hara.runtime.basic.impl.process-js :refer :all]
            [hara.lang :as l]
            [std.lib.env :as env])
  (:use code.test))

(l/script- :js
  {:runtime :oneshot
   :config {:program :nodejs}})

(fact:global
 {:skip (not (env/program-exists? "node"))})

^{:refer hara.runtime.basic.impl.process-js/CANARY :adopt true :added "4.0"}
(fact "EVALUATE js code"

  (!.js (+ 1 2 3 4))
  => 10

  (default-oneshot-wrap "1")
  => string?)


^{:refer hara.runtime.basic.impl.process-js/node-path :added "4.1"}
(fact "builds a NODE_PATH including the project-local node_modules"
  (let [path (node-path)
        sep (str java.io.File/pathSeparator)
        segments (clojure.string/split path (re-pattern (java.util.regex.Pattern/quote sep)))]
    [(string? path)
     (not (clojure.string/blank? path))
     (clojure.string/includes? path
                               (str (or (System/getenv "PWD")
                                        (System/getProperty "user.dir"))
                                    "/node_modules"))
     (every? (complement clojure.string/blank?) segments)
     (= (count segments) (count (distinct segments)))])
  => [true true true true true])