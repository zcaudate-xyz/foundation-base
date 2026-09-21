(ns lang.kondo-test
  (:use code.test)
  (:require [clojure.edn :as edn]
            [clojure.java.shell :as shell]))

(defn lint-source
  [source]
  (let [result (shell/sh "clj-kondo" "--lint" "-"
                         "--filename" "sample.clj"
                         "--config-dir" "resources/clj-kondo.exports/zcaudate/lang"
                         "--cache" "false"
                         "--config" "{:output {:format :edn}}"
                         :in source)]
    {:stderr (:err result)
     :findings (:findings (edn/read-string (:out result)))}))

(fact "the renamed export resolves PostgreSQL hooks and grouped SQL signatures"
  (let [result (lint-source
                 "(ns sample (:require [lang.core :as l]))
                  (l/script :postgres {})
                  (defn.pg ^{:%% :sql} ret-value
                    ([:text i-value] i-value))
                  (ret-value \"ok\")")]
    (:stderr result) => ""
    (filter #(= :error (:level %)) (:findings result)) => []))

(fact "the renamed export still reports PostgreSQL return violations"
  (let [result (lint-source
                 "(ns sample (:require [lang.core :as l]))
                  (l/script :postgres {})
                  (defn.pg ret-value [:text i-value]
                    (let [o-value i-value]
                      (return (str o-value))))")]
    (some #(= :lang.postgres/return-bound (:type %)) (:findings result)))
  => true)

(fact "the renamed export treats dynamically interned JavaScript definitions as XTalk"
  (let [result (lint-source
                 "(ns sample (:require [lang.core :as l]))
                  (l/script :js {})
                  (defn.js good [x]
                    (var value x)
                    (return value))
                  (def.js value (:? test 1 2))
                  (defglobal.js global-value (:? test 1 2))")]
    (:stderr result) => ""
    (filter #(= :error (:level %)) (:findings result)) => []))

(fact "the JavaScript definition hook reports XTalk grammar violations"
  (let [result (lint-source
                 "(ns sample (:require [lang.core :as l]))
                  (l/script :js {})
                  (defn.js bad []
                    (return (:? (if test 1 2) 3)))")]
    (some #(= :lang.xtalk/block-in-value (:type %)) (:findings result)))
  => true)

(fact "the XTalk hook preserves statement controls and target destructuring"
  (let [result (lint-source
                 "(ns sample (:require [lang.core :as l]))
                  (l/script :js {})
                  (defn.js good []
                    (var [current setCurrent #{stopCountdown startCountdown}] value)
                    (try
                      (return current)
                      (catch e
                        (when e
                          (return e)))
                      (finally
                        (return nil)))
                    (r/watch [current]
                      (j/delayed [100]
                        (setCurrent current))))")]
    (:stderr result) => ""
    (filter #(= :error (:level %)) (:findings result)) => []))
