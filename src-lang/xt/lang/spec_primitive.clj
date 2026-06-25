(ns xt.lang.spec-primitive
  (:require [hara.lang :as l :refer [defspec.xt]])
  (:refer-clojure :exclude [* + - / < <= == > >= await break mod not= return -> ->> and case comment cond doto for let letfn not or when while]))

(l/script :xtalk)

(defmacro.xt ^{:standalone true} 
  !:G
  "reads and writes global values"
  {:added "4.1"}
  [x & more] (apply list (quote !:G) x more))

(defmacro.xt ^{:standalone true} 
  $
  "accesses static members"
  {:added "4.1"}
  [x & more] (apply list (quote $) x more))

(defmacro.xt ^{:standalone true} 
  %
  "emits internal expressions directly"
  {:added "4.1"}
  [x & more] (apply list (quote %) x more))

(defmacro.xt ^{:standalone true} 
  *
  "multiplies values"
  {:added "4.1"}
  [x & more] (apply list (quote *) x more))

(defmacro.xt ^{:standalone true} 
  +
  "adds values"
  {:added "4.1"}
  [x & more] (apply list (quote +) x more))

(defmacro.xt ^{:standalone true} 
  -
  "subtracts values"
  {:added "4.1"}
  [x & more] (apply list (quote -) x more))

(defmacro.xt ^{:standalone true} 
  -%%-
  "emits raw internal strings"
  {:added "4.1"}
  [x & more] (apply list (quote -%%-) x more))

(defmacro.xt ^{:standalone true} 
  .
  "indexes values"
  {:added "4.1"}
  [x & more] (apply list (quote .) x more))

(defmacro.xt ^{:standalone true} 
  /
  "divides values"
  {:added "4.1"}
  [x & more] (apply list (quote /) x more))

(defmacro.xt ^{:standalone true} 
  <
  "compares less-than"
  {:added "4.1"}
  [x & more] (apply list (quote <) x more))

(defmacro.xt ^{:standalone true} 
  <=
  "compares less-than-or-equal"
  {:added "4.1"}
  [x & more] (apply list (quote <=) x more))

(defmacro.xt ^{:standalone true} 
  ==
  "compares equality"
  {:added "4.1"}
  [x & more] (apply list (quote ==) x more))

(defmacro.xt ^{:standalone true} 
  >
  "compares greater-than"
  {:added "4.1"}
  [x & more] (apply list (quote >) x more))

(defmacro.xt ^{:standalone true} 
  >=
  "compares greater-than-or-equal"
  {:added "4.1"}
  [x & more] (apply list (quote >=) x more))

(defmacro.xt ^{:standalone true} 
  async
  "creates async functions"
  {:added "4.1"}
  [x & more] (apply list (quote async) x more))

(defmacro.xt ^{:standalone true} 
  await
  "awaits promise results"
  {:added "4.1"}
  [x & more] (apply list (quote await) x more))

(defmacro.xt ^{:standalone true} 
  b:<<
  "bit-shifts left"
  {:added "4.1"}
  [x & more] (apply list (quote b:<<) x more))

(defmacro.xt ^{:standalone true} 
  b:>>
  "bit-shifts right"
  {:added "4.1"}
  [x & more] (apply list (quote b:>>) x more))

(defmacro.xt ^{:standalone true} 
  b:xor
  "bitwise xors values"
  {:added "4.1"}
  [x & more] (apply list (quote b:xor) x more))

(defmacro.xt ^{:standalone true} 
  br*
  "branches across control clauses"
  {:added "4.1"}
  [x & more] (apply list (quote br*) x more))

(defmacro.xt ^{:standalone true} 
  break
  "breaks out of loops"
  {:added "4.1"}
  [x & more] (apply list (quote break) x more))

(defmacro.xt ^{:standalone true} 
  forange
  "loops over a numeric range"
  {:added "4.1"}
  [x & more] (apply list (quote forange) x more))

(defmacro.xt ^{:standalone true} 
  letrec
  "binds recursive locals"
  {:added "4.1"}
  [x & more] (apply list (quote letrec) x more))

(defmacro.xt ^{:standalone true} 
  match
  "matches values against clauses"
  {:added "4.1"}
  [x & more] (apply list (quote match) x more))

(defmacro.xt ^{:standalone true} 
  mod
  "calculates modulo"
  {:added "4.1"}
  [x & more] (apply list (quote mod) x more))

(defmacro.xt ^{:standalone true} 
  new
  "constructs new values"
  {:added "4.1"}
  [x & more] (apply list (quote new) x more))

(defmacro.xt ^{:standalone true} 
  not=
  "compares inequality"
  {:added "4.1"}
  [x & more] (apply list (quote not=) x more))

(defmacro.xt ^{:standalone true} 
  pow
  "raises powers"
  {:added "4.1"}
  [x & more] (apply list (quote xt.lang.spec-base/x:m-pow) x more))

(defmacro.xt ^{:standalone true} 
  return
  "returns from functions"
  {:added "4.1"}
  [x & more] (apply list (quote return) x more))

(defmacro.xt ^{:standalone true} 
  super
  "accesses the parent receiver"
  {:added "4.1"}
  [x & more] (apply list (quote super) x more))

(defmacro.xt ^{:standalone true} 
  switch
  "switches across explicit cases"
  {:added "4.1"}
  [x & more] (apply list (quote switch) x more))

(defmacro.xt ^{:standalone true} 
  tab
  "creates tables from pairs"
  {:added "4.1"}
  [x & more] (apply list (quote tab) x more))

(defmacro.xt ^{:standalone true} 
  this
  "accesses the current receiver"
  {:added "4.1"}
  [x & more] (apply list (quote this) x more))

(defmacro.xt ^{:standalone true} 
  throw
  "throws values"
  {:added "4.1"}
  [x & more] (apply list (quote throw) x more))

(defmacro.xt ^{:standalone true} 
  var
  "declares local variables"
  {:added "4.1"}
  [x & more] (apply list (quote var) x more))

(defmacro.xt ^{:standalone true} 
  xor
  "computes logical xor"
  {:added "4.1"}
  [x & more] (apply list (quote xor) x more))

(defmacro.xt ^{:standalone true} 
  yield
  "yields values from generators"
  {:added "4.1"}
  [x & more] (apply list (quote yield) x more))

(defmacro.xt ^{:standalone true} 
  ->
  "threads the first argument"
  {:added "4.1"}
  [x & more] (apply list (quote ->) x more))

(defmacro.xt ^{:standalone true} 
  ->>
  "threads the last argument"
  {:added "4.1"}
  [x & more] (apply list (quote ->>) x more))

(defmacro.xt ^{:standalone true} 
  and
  "computes logical and"
  {:added "4.1"}
  [x & more] (apply list (quote and) x more))

(defmacro.xt ^{:standalone true} 
  case
  "selects matching case clauses"
  {:added "4.1"}
  [x & more] (apply list (quote case) x more))

(defmacro.xt ^{:standalone true} 
  comment
  "discards commented forms"
  {:added "4.1"}
  [x & more] (apply list (quote comment) x more))

(defmacro.xt ^{:standalone true} 
  cond
  "selects the first matching branch"
  {:added "4.1"}
  [x & more] (apply list (quote cond) x more))

(defmacro.xt ^{:standalone true} 
  do
  "runs sequential expressions"
  {:added "4.1"}
  [x & more] (apply list (quote do) x more))

(defmacro.xt ^{:standalone true} 
  doto
  "threads a value as the first argument"
  {:added "4.1"}
  [x & more] (apply list (quote doto) x more))

(defmacro.xt ^{:standalone true} 
  for
  "loops with init, condition and step"
  {:added "4.1"}
  [x & more] (apply list (quote for) x more))

(defmacro.xt ^{:standalone true} 
  if
  "selects between branches"
  {:added "4.1"}
  [x & more] (apply list (quote if) x more))

(defmacro.xt ^{:standalone true} 
  let
  "binds locals"
  {:added "4.1"}
  [x & more] (apply list (quote let) x more))

(defmacro.xt ^{:standalone true} 
  letfn
  "binds local named functions"
  {:added "4.1"}
  [x & more] (apply list (quote letfn) x more))

(defmacro.xt ^{:standalone true} 
  not
  "negates truthiness"
  {:added "4.1"}
  [x & more] (apply list (quote not) x more))

(defmacro.xt ^{:standalone true} 
  or
  "computes logical or"
  {:added "4.1"}
  [x & more] (apply list (quote or) x more))

(defmacro.xt ^{:standalone true} 
  quote
  "returns quoted literals"
  {:added "4.1"}
  [x & more] (apply list (quote quote) x more))

(defmacro.xt ^{:standalone true} 
  try
  "runs catch and finally handlers"
  {:added "4.1"}
  [x & more] (apply list (quote try) x more))

(defmacro.xt ^{:standalone true} 
  when
  "runs truthy branches"
  {:added "4.1"}
  [x & more] (apply list (quote when) x more))

(defmacro.xt ^{:standalone true} 
  while
  "loops while conditions hold"
  {:added "4.1"}
  [x & more] (apply list (quote while) x more))

(defmacro.xt ^{:standalone true} 
  fn
  "creates functions"
  {:added "4.1"}
  [x & more] (apply list (quote fn) x more))
