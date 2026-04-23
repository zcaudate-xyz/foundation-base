(ns xt.runtime.parser-core
  (:require [std.lang :as l])
  (:refer-clojure :exclude [read read-string]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.runtime.parser-common :as pc]
             [xt.runtime.parser-forms :as forms]
             [xt.runtime.reader :as rdr]]})

(defn.xt read
  "reads a single runtime form from the reader"
  {:added "4.1"}
  [reader]
  (pc/skip-whitespace reader)
  (var ch (rdr/read-char reader))
  (cond (xt/x:nil? ch)
        (return nil)

        (== ch "\"")
        (return (pc/read-string-body reader))

        (== ch "(")
        (return (forms/read-list reader -/read))

        (== ch "[")
        (return (forms/read-vector reader -/read))

        (== ch "{")
        (return (forms/read-map reader -/read))

        (== ch "'")
        (return (forms/read-quote reader -/read))

        (== ch "`")
        (return (forms/read-syntax-quote reader -/read))

        (== ch "@")
        (return (forms/read-deref reader -/read))

        (== ch "~")
        (return (forms/read-unquote reader -/read))

        (== ch "^")
        (return (forms/read-meta reader -/read))

        (== ch "#")
        (return (forms/read-dispatch reader -/read))

        (or (== ch ")")
            (== ch "]")
            (== ch "}"))
        (rdr/throw-reader reader (xt/x:cat "Unmatched delimiter: " ch))

        :else
        (return (pc/interpret-token reader
                                    (pc/read-token reader ch)))))

(defn.xt read-delimited
  "reads values until a closing delimiter"
  {:added "4.1"}
  [reader delim]
  (return (forms/read-delimited reader delim -/read)))

(defn.xt read-string
  "reads a single runtime form from a string"
  {:added "4.1"}
  [s]
  (return (-/read (rdr/create s))))
