(ns xt.runtime.parser-forms
  (:require [std.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.runtime.parser-common :as pc]
             [xt.runtime.reader :as rdr]
             [xt.runtime.type-hashmap :as hm]
             [xt.runtime.type-hashset :as hs]
             [xt.runtime.type-list :as list]
             [xt.runtime.type-symbol :as sym]
             [xt.runtime.type-syntax :as syn]
             [xt.runtime.type-vector :as vec]]})

(defn.xt read-delimited
  "reads values until a closing delimiter"
  {:added "4.1"}
  [reader delim read-fn]
  (var out [])
  (while true
    (pc/skip-whitespace reader)
    (var ch (rdr/peek-char reader))
    (cond (xt/x:nil? ch)
          (rdr/throw-reader reader "EOF while reading")

          (== ch delim)
          (do (rdr/read-char reader)
              (return out))

          :else
          (xt/x:arr-push out (read-fn reader)))))

(defn.xt read-list
  "reads a list form"
  {:added "4.1"}
  [reader read-fn]
  (return (list/list (xt/x:unpack (-/read-delimited reader ")" read-fn)))))

(defn.xt read-vector
  "reads a vector form"
  {:added "4.1"}
  [reader read-fn]
  (return (vec/vector (xt/x:unpack (-/read-delimited reader "]" read-fn)))))

(defn.xt read-map
  "reads a map form"
  {:added "4.1"}
  [reader read-fn]
  (var items (-/read-delimited reader "}" read-fn))
  (when (not= 0 (xt/x:m-mod (xt/x:len items) 2))
    (rdr/throw-reader reader "Map literal must contain an even number of forms"))
  (return (hm/hashmap (xt/x:unpack items))))

(defn.xt read-set
  "reads a set form"
  {:added "4.1"}
  [reader read-fn]
  (return (hs/hashset (xt/x:unpack (-/read-delimited reader "}" read-fn)))))

(defn.xt read-required
  "reads the next form or throws on EOF"
  {:added "4.1"}
  [reader read-fn]
  (var out (read-fn reader))
  (when (xt/x:nil? out)
    (rdr/throw-reader reader "EOF while reading"))
  (return out))

(defn.xt read-quote
  "reads a quoted form"
  {:added "4.1"}
  [reader read-fn]
  (return (list/list (sym/symbol nil "quote")
                     (-/read-required reader read-fn))))

(defn.xt read-syntax-quote
  "reads a syntax-quote form"
  {:added "4.1"}
  [reader read-fn]
  (return (list/list (sym/symbol nil "syntax-quote")
                     (-/read-required reader read-fn))))

(defn.xt read-deref
  "reads a deref form"
  {:added "4.1"}
  [reader read-fn]
  (return (list/list (sym/symbol nil "deref")
                     (-/read-required reader read-fn))))

(defn.xt read-unquote-splicing
  "reads an unquote-splicing form"
  {:added "4.1"}
  [reader read-fn]
  (return (list/list (sym/symbol nil "unquote-splicing")
                     (-/read-required reader read-fn))))

(defn.xt read-unquote
  "reads an unquote form"
  {:added "4.1"}
  [reader read-fn]
  (if (== "@" (rdr/peek-char reader))
    (do (rdr/read-char reader)
        (return (-/read-unquote-splicing reader read-fn)))
    (return (list/list (sym/symbol nil "unquote")
                       (-/read-required reader read-fn)))))

(defn.xt read-meta
  "reads metadata and wraps the following form"
  {:added "4.1"}
  [reader read-fn]
  (var meta (pc/normalise-meta (-/read-required reader read-fn)))
  (var form (-/read-required reader read-fn))
  (return (syn/syntax-create form meta)))

(defn.xt read-var-quote
  "reads a var-quote form"
  {:added "4.1"}
  [reader read-fn]
  (return (list/list (sym/symbol nil "var")
                     (-/read-required reader read-fn))))

(defn.xt read-discard
  "reads and discards the next form"
  {:added "4.1"}
  [reader read-fn]
  (-/read-required reader read-fn)
  (return (-/read-required reader read-fn)))

(defn.xt read-dispatch
  "reads dispatch macro forms"
  {:added "4.1"}
  [reader read-fn]
  (var ch (rdr/read-char reader))
  (cond (xt/x:nil? ch)
        (rdr/throw-reader reader "EOF while reading dispatch")

         (== ch "{")
         (return (-/read-set reader read-fn))

         (== ch "'")
         (return (-/read-var-quote reader read-fn))

        (== ch "_")
        (return (-/read-discard reader read-fn))

        :else
        (rdr/throw-reader reader (xt/x:cat "No dispatch macro for: " ch))))
