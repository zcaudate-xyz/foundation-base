(ns xt.runtime.parser-common
  (:require [std.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.common-spec :as xt]
             [xt.lang.common-string :as xts]
             [xt.runtime.reader :as rdr]
             [xt.runtime.type-hashmap :as hm]
             [xt.runtime.type-keyword :as kw]
             [xt.runtime.type-symbol :as sym]]})

(defn.xt whitespace?
  "checks if a char should be treated as reader whitespace"
  {:added "4.1"}
  [ch]
  (return (or (== ch " ")
              (== ch "\n")
              (== ch "\r")
              (== ch "\t")
              (== ch ","))))

(defn.xt token-boundary?
  "checks if a char terminates a token"
  {:added "4.1"}
  [ch]
  (return (or (xt/x:nil? ch)
              (-/whitespace? ch)
              (== ch "(")
              (== ch ")")
              (== ch "[")
              (== ch "]")
              (== ch "{")
              (== ch "}")
              (== ch "\"")
              (== ch ";")
              (== ch "'")
              (== ch "`")
              (== ch "^")
              (== ch "#")
              (== ch "@")
              (== ch "~"))))

(defn.xt read-comment
  "reads a line comment"
  {:added "4.1"}
  [reader]
  (while true
    (var ch (rdr/read-char reader))
    (when (or (xt/x:nil? ch)
              (== ch "\n")
              (== ch "\r"))
      (return nil))))

(defn.xt skip-whitespace
  "advances the reader past whitespace and comments"
  {:added "4.1"}
  [reader]
  (while true
    (var ch (rdr/peek-char reader))
    (cond (xt/x:nil? ch)
          (return nil)

          (-/whitespace? ch)
          (rdr/read-char reader)

          (== ch ";")
          (do (rdr/read-char reader)
              (-/read-comment reader))

          :else
          (return ch))))

(defn.xt digit?
  "checks if a char is numeric"
  {:added "4.1"}
  [ch]
  (return (and (not= ch nil)
               (<= "0" ch)
               (<= ch "9"))))

(defn.xt numeric-leading?
  "checks if a token looks numeric from the first char"
  {:added "4.1"}
  [token]
  (var first (rdr/impl-char-at token 0))
  (var second (rdr/impl-char-at token 1))
  (return (or (-/digit? first)
              (and (or (== first "+")
                       (== first "-"))
                   (or (-/digit? second)
                       (== second ".")))
              (== first "."))))

(defn.xt match-number
  "matches simple integer and decimal number tokens"
  {:added "4.1"}
  [token]
  (var n (xt/x:len token))
  (when (== 0 n)
    (return nil))
  (var idx 0)
  (var dot? false)
  (var digits 0)
  (var first (rdr/impl-char-at token 0))
  (when (or (== first "+")
            (== first "-"))
    (:= idx 1))
  (while (< idx n)
    (var ch (rdr/impl-char-at token idx))
    (cond (-/digit? ch)
          (:= digits (+ digits 1))

          (and (== ch ".")
               (not dot?))
          (:= dot? true)

          :else
          (return nil))
    (:= idx (+ idx 1)))
  (if (== 0 digits)
    (return nil)
    (do (var out (xt/x:to-number token))
        (if (and (not= out nil)
                 (== out out))
          (return out)
          (return nil)))))

(defn.xt read-token
  "reads a token that starts with the given character"
  {:added "4.1"}
  [reader initch]
  (var out initch)
  (while true
    (var ch (rdr/peek-char reader))
    (if (-/token-boundary? ch)
      (return out)
      (:= out (xt/x:cat out (rdr/read-char reader))))))

(defn.xt interpret-token
  "interprets a token into a runtime value"
  {:added "4.1"}
  [reader token]
  (cond (== token "nil")
        (return nil)

        (== token "true")
        (return true)

        (== token "false")
        (return false)

        :else
        (do (var number (-/match-number token))
            (when (not= number nil)
              (return number))
            (when (and (-/numeric-leading? token)
                       (== number nil))
              (rdr/throw-reader reader (xt/x:cat "Invalid number: " token)))
            (if (== ":" (rdr/impl-char-at token 0))
              (do (when (or (== token ":")
                            (== token ":/"))
                    (rdr/throw-reader reader (xt/x:cat "Invalid token: " token)))
                  (var [ns name] (xts/sym-pair (xt/x:str-substring token (xt/x:offset 1))))
                  (return (kw/keyword ns name)))
              (do (var [ns name] (xts/sym-pair token))
                  (return (sym/symbol ns name)))))))

(defn.xt normalise-meta
  "normalises metadata shorthand into map-like runtime values"
  {:added "4.1"}
  [meta]
  (cond (xt/x:is-string? meta)
        (return (hm/hashmap (kw/keyword nil "tag") meta))

        (and (xt/x:is-object? meta)
             (== "symbol" (. meta ["::"])))
        (return (hm/hashmap (kw/keyword nil "tag") meta))

        (and (xt/x:is-object? meta)
             (== "keyword" (. meta ["::"])))
        (return (hm/hashmap meta true))

        :else
        (return meta)))

(defn.xt read-string-body
  "reads the body of a double-quoted string"
  {:added "4.1"}
  [reader]
  (var out "")
  (while true
    (var ch (rdr/read-char reader))
    (cond (xt/x:nil? ch)
          (rdr/throw-reader reader "EOF while reading string")

          (== ch "\"")
          (return out)

          (== ch "\\")
          (do (var esc (rdr/read-char reader))
              (cond (xt/x:nil? esc)
                    (rdr/throw-reader reader "EOF while reading string")

                    (== esc "n")
                    (:= out (xt/x:cat out "\n"))

                    (== esc "r")
                    (:= out (xt/x:cat out "\r"))

                    (== esc "t")
                    (:= out (xt/x:cat out "\t"))

                    (== esc "b")
                    (:= out (xt/x:cat out "\b"))

                    (== esc "f")
                    (:= out (xt/x:cat out "\f"))

                    :else
                    (:= out (xt/x:cat out esc))))

          :else
          (:= out (xt/x:cat out ch)))))
