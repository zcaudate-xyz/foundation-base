(ns std.lang.model.spec-lua.rewrite
  (:require [std.lang.rewrite.hoist :as hoist]
            [std.lang.rewrite.lift-named-lambda :as lift]))

(defn- lua-lambda-compatible?
  [form _grammar]
  (let [[name] (lift/fn-parts form)]
    (nil? name)))

(def ^:private +lua-rewriter+
  (hoist/create-rewriter
   {:symbol-prefix "lua_callback__"
    :lambda-compatible? lua-lambda-compatible?}))

(def lua-rewrite-expression
  (:rewrite-expression +lua-rewriter+))

(def lua-rewrite-statement
  (:rewrite-statement +lua-rewriter+))

(def lua-rewrite-statements
  (:rewrite-statements +lua-rewriter+))

(def lua-rewrite-stage
  (:rewrite-stage +lua-rewriter+))
