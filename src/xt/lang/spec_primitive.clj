(ns xt.lang.spec-primitive
  (:require [std.lang :as l :refer [defspec.xt]])
  (:refer-clojure :exclude [* + - / < <= == > >= await break mod not= return -> ->> and case comment cond doto for let letfn not or when while]))

(l/script :xtalk)

(defmacro.xt ^{:standalone true} 
  !:G
  [x & more] (apply list (quote !:G) x more))

(defmacro.xt ^{:standalone true} 
  $
  [x & more] (apply list (quote $) x more))

(defmacro.xt ^{:standalone true} 
  %
  [x & more] (apply list (quote %) x more))

(defmacro.xt ^{:standalone true} 
  *
  [x & more] (apply list (quote *) x more))

(defmacro.xt ^{:standalone true} 
  +
  [x & more] (apply list (quote +) x more))

(defmacro.xt ^{:standalone true} 
  -
  [x & more] (apply list (quote -) x more))

(defmacro.xt ^{:standalone true} 
  -%%-
  [x & more] (apply list (quote -%%-) x more))

(defmacro.xt ^{:standalone true} 
  .
  [x & more] (apply list (quote .) x more))

(defmacro.xt ^{:standalone true} 
  /
  [x & more] (apply list (quote /) x more))

(defmacro.xt ^{:standalone true} 
  <
  [x & more] (apply list (quote <) x more))

(defmacro.xt ^{:standalone true} 
  <=
  [x & more] (apply list (quote <=) x more))

(defmacro.xt ^{:standalone true} 
  ==
  [x & more] (apply list (quote ==) x more))

(defmacro.xt ^{:standalone true} 
  >
  [x & more] (apply list (quote >) x more))

(defmacro.xt ^{:standalone true} 
  >=
  [x & more] (apply list (quote >=) x more))

(defmacro.xt ^{:standalone true} 
  async
  [x & more] (apply list (quote async) x more))

(defmacro.xt ^{:standalone true} 
  await
  [x & more] (apply list (quote await) x more))

(defmacro.xt ^{:standalone true} 
  b:<<
  [x & more] (apply list (quote b:<<) x more))

(defmacro.xt ^{:standalone true} 
  b:>>
  [x & more] (apply list (quote b:>>) x more))

(defmacro.xt ^{:standalone true} 
  b:xor
  [x & more] (apply list (quote b:xor) x more))

(defmacro.xt ^{:standalone true} 
  block
  [x & more] (apply list (quote block) x more))

(defmacro.xt ^{:standalone true} 
  br*
  [x & more] (apply list (quote br*) x more))

(defmacro.xt ^{:standalone true} 
  break
  [x & more] (apply list (quote break) x more))

(defmacro.xt ^{:standalone true} 
  do:>
  [x & more] (apply list (quote do:>) x more))

(defmacro.xt ^{:standalone true} 
  fn.inner
  [x & more] (apply list (quote fn.inner) x more))

(defmacro.xt ^{:standalone true} 
  fn:>
  [x & more] (apply list (quote fn:>) x more))

(defmacro.xt ^{:standalone true} 
  forange
  [x & more] (apply list (quote forange) x more))

(defmacro.xt ^{:standalone true} 
  letrec
  [x & more] (apply list (quote letrec) x more))

(defmacro.xt ^{:standalone true} 
  match
  [x & more] (apply list (quote match) x more))

(defmacro.xt ^{:standalone true} 
  mod
  [x & more] (apply list (quote mod) x more))

(defmacro.xt ^{:standalone true} 
  new
  [x & more] (apply list (quote new) x more))

(defmacro.xt ^{:standalone true} 
  not=
  [x & more] (apply list (quote not=) x more))

(defmacro.xt ^{:standalone true} 
  pow
  [x & more] (apply list (quote xt.lang.spec-base/x:m-pow) x more))

(defmacro.xt ^{:standalone true} 
  return
  [x & more] (apply list (quote return) x more))

(defmacro.xt ^{:standalone true} 
  super
  [x & more] (apply list (quote super) x more))

(defmacro.xt ^{:standalone true} 
  switch
  [x & more] (apply list (quote switch) x more))

(defmacro.xt ^{:standalone true} 
  tab
  [x & more] (apply list (quote tab) x more))

(defmacro.xt ^{:standalone true} 
  this
  [x & more] (apply list (quote this) x more))

(defmacro.xt ^{:standalone true} 
  throw
  [x & more] (apply list (quote throw) x more))

(defmacro.xt ^{:standalone true} 
  var
  [x & more] (apply list (quote var) x more))

(defmacro.xt ^{:standalone true} 
  var.inner
  [x & more] (apply list (quote var.inner) x more))

(defmacro.xt ^{:standalone true} 
  xor
  [x & more] (apply list (quote xor) x more))

(defmacro.xt ^{:standalone true} 
  yield
  [x & more] (apply list (quote yield) x more))

(defmacro.xt ^{:standalone true} 
  ->
  [x & more] (apply list (quote ->) x more))

(defmacro.xt ^{:standalone true} 
  ->>
  [x & more] (apply list (quote ->>) x more))

(defmacro.xt ^{:standalone true} 
  and
  [x & more] (apply list (quote and) x more))

(defmacro.xt ^{:standalone true} 
  case
  [x & more] (apply list (quote case) x more))

(defmacro.xt ^{:standalone true} 
  comment
  [x & more] (apply list (quote comment) x more))

(defmacro.xt ^{:standalone true} 
  cond
  [x & more] (apply list (quote cond) x more))

(defmacro.xt ^{:standalone true} 
  do
  [x & more] (apply list (quote do) x more))

(defmacro.xt ^{:standalone true} 
  doto
  [x & more] (apply list (quote doto) x more))

(defmacro.xt ^{:standalone true} 
  for
  [x & more] (apply list (quote for) x more))

(defmacro.xt ^{:standalone true} 
  if
  [x & more] (apply list (quote if) x more))

(defmacro.xt ^{:standalone true} 
  let
  [x & more] (apply list (quote let) x more))

(defmacro.xt ^{:standalone true} 
  letfn
  [x & more] (apply list (quote letfn) x more))

(defmacro.xt ^{:standalone true} 
  not
  [x & more] (apply list (quote not) x more))

(defmacro.xt ^{:standalone true} 
  or
  [x & more] (apply list (quote or) x more))

(defmacro.xt ^{:standalone true} 
  quote
  [x & more] (apply list (quote quote) x more))

(defmacro.xt ^{:standalone true} 
  try
  [x & more] (apply list (quote try) x more))

(defmacro.xt ^{:standalone true} 
  when
  [x & more] (apply list (quote when) x more))

(defmacro.xt ^{:standalone true} 
  while
  [x & more] (apply list (quote while) x more))

(defmacro.xt ^{:standalone true} 
  fn
  [x & more] (apply list (quote fn) x more))
