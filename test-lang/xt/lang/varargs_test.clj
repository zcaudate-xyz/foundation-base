(ns xt.lang.varargs-test
  (:require [hara.lang :as l])
  (:use code.test))

(def +rest-form+
  '[(defn collect [head (:.. tail)]
      (return (x:len tail)))])

(fact "emits canonical xtalk rest arguments for algol targets"
  (let [dart   (l/emit-as :dart +rest-form+)
        ruby   (l/emit-as :ruby +rest-form+)
        python (l/emit-as :python +rest-form+)
        lua    (l/emit-as :lua +rest-form+)
        php    (l/emit-as :php +rest-form+)]
    [(boolean (re-find #"collect\\(head,\\[tail = const \\[\\]\\]\\)" dart))
     (boolean (re-find #"collect\\(head,\\*tail\\)" ruby))
     (boolean (re-find #"collect\\(head,\\*tail\\)" python))
     (boolean (re-find #"collect\\(head,\\.\\.\\.\\)" lua))
     (boolean (re-find #"tail.*\\{\\.\\.\\.\\}" lua))
     (boolean (re-find #"collect\\(\\$head,\\.\\.\\.\\$tail\\)" php))])
  => [true true true true true true])

(fact "emits canonical xtalk rest arguments for Lisp targets"
  (let [scheme (l/emit-as :scheme +rest-form+)
        elisp  (l/emit-as :elisp +rest-form+)]
    [(boolean (re-find #"\\(collect head \\. tail\\)" scheme))
     (boolean (re-find #"\\(set! tail \\(list->vector tail\\)\\)" scheme))
     (boolean (re-find #"\\(head &rest tail\\)" elisp))
     (boolean (re-find #"\\(setq tail \\(vconcat tail\\)\\)" elisp))])
  => [true true true true])
