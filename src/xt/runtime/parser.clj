(ns xt.runtime.parser
  (:require [std.lang :as l])
  (:refer-clojure :exclude [read read-string]))

(l/script :xtalk
  {:require [[xt.runtime.parser-common :as pc]
             [xt.runtime.parser-core :as core]
             [xt.runtime.parser-forms :as forms]
             [xt.runtime.reader :as rdr]]})

(defn.xt read
  "reads a single runtime form from the reader"
  {:added "4.1"}
  [reader]
  (return (core/read reader)))

(defn.xt read-delimited
  "reads values until a closing delimiter"
  {:added "4.1"}
  [reader delim]
  (return (core/read-delimited reader delim)))

(defn.xt read-list
  "reads a list form"
  {:added "4.1"}
  [reader]
  (return (forms/read-list reader core/read)))

(defn.xt read-vector
  "reads a vector form"
  {:added "4.1"}
  [reader]
  (return (forms/read-vector reader core/read)))

(defn.xt read-map
  "reads a map form"
  {:added "4.1"}
  [reader]
  (return (forms/read-map reader core/read)))

(defn.xt read-set
  "reads a set form"
  {:added "4.1"}
  [reader]
  (return (forms/read-set reader core/read)))

(defn.xt read-quote
  "reads a quoted form"
  {:added "4.1"}
  [reader]
  (return (forms/read-quote reader core/read)))

(defn.xt read-syntax-quote
  "reads a syntax-quote form"
  {:added "4.1"}
  [reader]
  (return (forms/read-syntax-quote reader core/read)))

(defn.xt read-deref
  "reads a deref form"
  {:added "4.1"}
  [reader]
  (return (forms/read-deref reader core/read)))

(defn.xt read-unquote
  "reads an unquote form"
  {:added "4.1"}
  [reader]
  (return (forms/read-unquote reader core/read)))

(defn.xt read-unquote-splicing
  "reads an unquote-splicing form"
  {:added "4.1"}
  [reader]
  (return (forms/read-unquote-splicing reader core/read)))

(defn.xt read-meta
  "reads metadata and wraps the following form"
  {:added "4.1"}
  [reader]
  (return (forms/read-meta reader core/read)))

(defn.xt read-var-quote
  "reads a var-quote form"
  {:added "4.1"}
  [reader]
  (return (forms/read-var-quote reader core/read)))

(defn.xt read-discard
  "reads and discards the next form"
  {:added "4.1"}
  [reader]
  (return (forms/read-discard reader core/read)))

(defn.xt read-dispatch
  "reads dispatch macro forms"
  {:added "4.1"}
  [reader]
  (return (forms/read-dispatch reader core/read)))

(defn.xt read-string
  "reads a single runtime form from a string"
  {:added "4.1"}
  [s]
  (return (core/read-string s)))
