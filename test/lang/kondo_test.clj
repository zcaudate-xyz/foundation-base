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

(fact "tagged XTalk function definitions are linted across target suffixes"
  (let [result (lint-source
                 "(ns sample (:require [lang.core :as l]))
                  (l/script :lua.nginx {})
                  (defn.lua bad-lua [] (return (:? (if test 1 2) 3)))
                  (l/script :go {})
                  (defn.go bad-go [] (return (:? (if test 1 2) 3)))
                  (l/script :c {})
                  (defn.c bad-c [] (return (:? (if test 1 2) 3)))")]
    (:stderr result) => ""
    (count (filter #(= :lang.xtalk/block-in-value (:type %))
                   (:findings result))) => 3
    (filter #(= :unresolved-symbol (:type %)) (:findings result)) => []))

(fact "tagged function definitions are linted for every target suffix"
  (let [targets [[:javascript "js"] [:xtalk "xt"] [:typescript "ts"]
                 [:dart "dt"] [:julia "jl"] [:python "py"] [:ruby "rb"]
                 [:rust "rs"] [:golang "go"] [:c "c"] [:cpp "cpp"]
                 [:lua "lua"] [:sql "sql"] [:oracle "oracle"]]
        source (str "(ns xt.sample (:require [lang.core :as l]))\n"
                    (clojure.string/join
                      "\n"
                      (map (fn [[language suffix]]
                             (str "(l/script :" (name language) " {})\n"
                                  "(defn." suffix " target-" suffix " []\n"
                                  "  (return (:? (if test 1 2) 3)))"))
                           targets)))
        result (lint-source source)]
    (:stderr result) => ""
    (count (filter #(= :lang.xtalk/block-in-value (:type %))
                   (:findings result))) => (count targets)
    (filter #(= :unresolved-symbol (:type %)) (:findings result)) => []))

(fact "the Lua script hook resolves every generated definition family"
  (let [result (lint-source
                 "(ns xt.sample (:require [lang.core :as l]))
                  (l/script :lua.nginx {})
                  (defrun.lua run [] missing-run)
                  (defn.lua function [arg] missing-function)
                  (defn-.lua private-function [] missing-private-function)
                  (defglobal.lua global-value missing-global)
                  (defvar.lua variable-value missing-variable)
                  (defgen.lua generated [] missing-generated)
                  (defimpl.lua implementation missing-implementation)
                  (defprotocol.lua protocol (method [arg]))
                  (defspec.lua specification missing-specification)
                  (deftemp.lua template missing-template)
                  (defclass.lua lua-class missing-class)
                  (defabstract.lua abstract missing-abstract)
                  (def.lua value missing-value)
                  (def$.lua dynamic-value missing-dynamic)
                  (defmacro.lua macro [arg] missing-macro)
                  (defptr.lua pointer missing-pointer)")]
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
