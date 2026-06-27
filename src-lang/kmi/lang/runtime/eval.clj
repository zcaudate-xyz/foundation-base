(ns kmi.lang.runtime.eval
  (:require [hara.lang :as l])
  (:refer-clojure :exclude [eval apply read symbol? keyword? list? vector?]))

(l/script :xtalk
  {:require [[kmi.lang.common-util :as util]
             [kmi.lang.common-hash :as common-hash]
             [kmi.lang.protocol-base :as p]
             [kmi.lang.parser :as parser]
             [kmi.lang.reader :as reader]
             [kmi.lang.type-syntax :as syn]
             [kmi.lang.type-list :as list]
             [kmi.lang.type-vector :as vec]
             [kmi.lang.type-hashmap :as hm]
             [kmi.lang.type-hashset :as hs]
             [kmi.lang.runtime.env :as env]
             [kmi.lang.runtime.primitive :as prim]
             [xt.lang.spec-base :as xt]]})

;;
;; RESULT HELPERS
;;

(defn.xt result
  "creates a successful result"
  {:added "4.1"}
  [runtime value]
  (return {"runtime" runtime
           "value" value}))

(defn.xt error
  "creates an error result"
  {:added "4.1"}
  [runtime message]
  (return {"runtime" runtime
           "error" message}))

(defn.xt error?
  "checks if a result is an error"
  {:added "4.1"}
  [out]
  (return (xt/x:has-key? out "error")))

(defn.xt value
  "extracts the value from a result"
  {:added "4.1"}
  [out]
  (return (xt/x:get-key out "value")))

(defn.xt runtime
  "extracts the runtime from a result"
  {:added "4.1"}
  [out]
  (return (xt/x:get-key out "runtime")))

;;
;; TYPE PREDICATES
;;

(defn.xt class-of
  "returns the kmi/native class tag"
  {:added "4.1"}
  [x]
  (return (common-hash/native-class x)))

(defn.xt symbol? [x] (return (== "symbol" (-/class-of x))))
(defn.xt keyword? [x] (return (== "keyword" (-/class-of x))))
(defn.xt list? [x] (return (== "list" (-/class-of x))))
(defn.xt vector? [x] (return (== "vector" (-/class-of x))))
(defn.xt hashmap? [x] (return (== "hashmap" (-/class-of x))))
(defn.xt hashset? [x] (return (== "hashset" (-/class-of x))))
(defn.xt syntax? [x] (return (== "syntax" (-/class-of x))))

(defn.xt self-evaluating?
  "checks if a value evaluates to itself"
  {:added "4.1"}
  [x]
  (return (or (xt/x:nil? x)
              (xt/x:is-string? x)
              (xt/x:is-number? x)
              (xt/x:is-boolean? x)
              (-/keyword? x)
              (-/hashset? x))))

;;
;; READ
;;

(defn.xt read-many
  "reads all forms from a string"
  {:added "4.1"}
  [source]
  (var rdr (reader/create source))
  (var out [])
  (while true
    (var form (parser/read rdr))
    (when (xt/x:nil? form)
      (return out))
    (xt/x:arr-push out form)))

;;
;; HELPERS (receive eval-fn to avoid forward references)
;;

(defn.xt eval-symbol
  "resolves a symbol to its bound value"
  {:added "4.1"}
  [runtime env sym]
  (var val (env/var-lookup runtime env sym))
  (when (xt/x:not-nil? val)
    (return (-/result runtime val)))
  (return (-/error runtime
                   (xt/x:cat "Unable to resolve symbol: "
                             (env/sym-name sym)))))

(defn.xt eval-do-array
  "evaluates a sequence of forms and returns the last value"
  {:added "4.1"}
  [eval-fn runtime env body]
  (var result nil)
  (xt/for:array [expr body]
    (var out (eval-fn runtime env expr))
    (when (-/error? out)
      (return out))
    (:= runtime (-/runtime out))
    (:= result (-/value out)))
  (return (-/result runtime result)))

(defn.xt eval-if
  "evaluates an if form"
  {:added "4.1"}
  [eval-fn runtime env parts]
  (var cond-form (xt/x:get-idx parts 1))
  (var then-form (xt/x:get-idx parts 2))
  (var else-form (xt/x:get-idx parts 3 nil))
  (var out (eval-fn runtime env cond-form))
  (when (-/error? out)
    (return out))
  (:= runtime (-/runtime out))
  (if (-/value out)
    (return (eval-fn runtime env then-form))
    (return (eval-fn runtime env (or else-form nil)))))

(defn.xt eval-let
  "evaluates a let form"
  {:added "4.1"}
  [eval-fn runtime env parts]
  (var bindings-form (xt/x:get-idx parts 1))
  (var body (xt/x:arr-slice parts 2 (xt/x:len parts)))
  (var pairs (p/to-array bindings-form))
  (var let-env (env/env-create env))
  (var i 0)
  (while (< i (xt/x:len pairs))
    (var sym (xt/x:get-idx pairs i))
    (var val-form (xt/x:get-idx pairs (+ i 1)))
    (var out (eval-fn runtime env val-form))
    (when (-/error? out)
      (return out))
    (:= runtime (-/runtime out))
    (xt/x:set-key (xt/x:get-key let-env "bindings")
                  (env/sym-name sym)
                  (-/value out))
    (:= i (+ i 2)))
  (return (-/eval-do-array eval-fn runtime let-env body)))

(defn.xt make-closure
  "creates a closure from a fn form"
  {:added "4.1"}
  [env parts]
  (var params-form (xt/x:get-idx parts 1))
  (var params (p/to-array params-form))
  (var body (xt/x:arr-slice parts 2 (xt/x:len parts)))
  (return {"type" "kmi.fn"
           "params" params
           "body" body
           "env" env
           "macro" false}))

(defn.xt eval-def
  "evaluates a def form"
  {:added "4.1"}
  [eval-fn runtime env parts]
  (var sym (xt/x:get-idx parts 1))
  (var val-form (xt/x:get-idx parts 2))
  (var out (eval-fn runtime env val-form))
  (when (-/error? out)
    (return out))
  (:= runtime (env/ns-assoc (-/runtime out) sym (-/value out)))
  (return (-/result runtime (-/value out))))

(defn.xt apply-fn
  "applies a function to evaluated arguments"
  {:added "4.1"}
  [eval-fn runtime f args]
  (cond (xt/x:is-function? f)
        (return (-/result runtime (xt/x:apply f args)))

        (and (xt/x:is-object? f)
             (== "kmi.fn" (xt/x:get-key f "type")))
        (do (var params (xt/x:get-key f "params"))
            (when (not= (xt/x:len params) (xt/x:len args))
              (return (-/error runtime
                               (xt/x:cat "arity mismatch: expected "
                                         (xt/x:to-string (xt/x:len params))
                                         ", got "
                                         (xt/x:to-string (xt/x:len args))))))
            (var call-env (env/env-create (xt/x:get-key f "env")))
            (xt/for:index [i [0 (xt/x:len params)]]
              (xt/x:set-key (xt/x:get-key call-env "bindings")
                            (env/sym-name (xt/x:get-idx params i))
                            (xt/x:get-idx args i)))
            (return (-/eval-do-array eval-fn runtime call-env (xt/x:get-key f "body"))))

        true
        (return (-/error runtime
                         (xt/x:cat "not a function: "
                                   (util/show f))))))

(defn.xt eval-call
  "evaluates a function call"
  {:added "4.1"}
  [eval-fn runtime env parts]
  (var op-form (xt/x:first parts))
  (var op-out (eval-fn runtime env op-form))
  (when (-/error? op-out)
    (return op-out))
  (:= runtime (-/runtime op-out))
  (var f (-/value op-out))
  (var arg-forms (xt/x:arr-slice parts 1 (xt/x:len parts)))
  (var args [])
  (xt/for:array [af arg-forms]
    (var out (eval-fn runtime env af))
    (when (-/error? out)
      (return out))
    (:= runtime (-/runtime out))
    (xt/x:arr-push args (-/value out)))
  (return (-/apply-fn eval-fn runtime f args)))

(defn.xt eval-list
  "evaluates a list form"
  {:added "4.1"}
  [eval-fn runtime env form]
  (var parts (p/to-array form))
  (var op (xt/x:first parts))
  (when (-/symbol? op)
    (var op-name (env/sym-name op))
    (cond (== op-name "quote") (return (-/result runtime (xt/x:get-idx parts 1)))
          (== op-name "if")   (return (-/eval-if eval-fn runtime env parts))
          (== op-name "do")   (return (-/eval-do-array eval-fn runtime env (xt/x:arr-slice parts 1 (xt/x:len parts))))
          (== op-name "let")  (return (-/eval-let eval-fn runtime env parts))
          (== op-name "fn")   (return (-/result runtime (-/make-closure env parts)))
          (== op-name "def")  (return (-/eval-def eval-fn runtime env parts))
          true               (return (-/eval-call eval-fn runtime env parts))))
  (return (-/eval-call eval-fn runtime env parts)))

(defn.xt eval-vector
  "evaluates a vector literal"
  {:added "4.1"}
  [eval-fn runtime env form]
  (var out [])
  (xt/for:array [item (p/to-array form)]
    (var res (eval-fn runtime env item))
    (when (-/error? res)
      (return res))
    (:= runtime (-/runtime res))
    (xt/x:arr-push out (-/value res)))
  (return (-/result runtime (vec/vector (xt/x:unpack out)))))

(defn.xt eval-hashmap
  "evaluates a hash-map literal"
  {:added "4.1"}
  [eval-fn runtime env form]
  (var items [])
  (xt/for:iter [entry (p/to-iter form)]
    (var k (p/key entry))
    (var v (p/val entry))
    (var k-out (eval-fn runtime env k))
    (when (-/error? k-out)
      (return k-out))
    (:= runtime (-/runtime k-out))
    (var v-out (eval-fn runtime env v))
    (when (-/error? v-out)
      (return v-out))
    (:= runtime (-/runtime v-out))
    (xt/x:arr-push items (-/value k-out))
    (xt/x:arr-push items (-/value v-out)))
  (return (-/result runtime (hm/hashmap (xt/x:unpack items)))))

(defn.xt eval-set
  "evaluates a set literal"
  {:added "4.1"}
  [eval-fn runtime env form]
  (var out [])
  (xt/for:array [item (p/to-array form)]
    (var res (eval-fn runtime env item))
    (when (-/error? res)
      (return res))
    (:= runtime (-/runtime res))
    (xt/x:arr-push out (-/value res)))
  (return (-/result runtime (hs/hashset (xt/x:unpack out)))))

;;
;; MAIN EVALUATOR
;;

(defn.xt eval-form
  "evaluates a single form in an environment"
  {:added "4.1"}
  [runtime env form]
  (cond (-/self-evaluating? form)
        (return (-/result runtime form))

        (-/symbol? form)
        (return (-/eval-symbol runtime env form))

        (-/list? form)
        (return (-/eval-list -/eval-form runtime env form))

        (-/vector? form)
        (return (-/eval-vector -/eval-form runtime env form))

        (-/hashmap? form)
        (return (-/eval-hashmap -/eval-form runtime env form))

        (-/syntax? form)
        (return (-/eval-form runtime env (syn/syntax form nil)))

        true
        (return (-/result runtime form))))

(defn.xt eval-forms
  "evaluates an array of forms sequentially"
  {:added "4.1"}
  [runtime env forms]
  (return (-/eval-do-array -/eval-form runtime env forms)))

(defn.xt eval-string
  "evaluates a single string expression"
  {:added "4.1"}
  [runtime source]
  (var forms (-/read-many source))
  (return (-/eval-forms runtime (env/empty-env) forms)))

(defn.xt eval-string-many
  "evaluates all forms in a string"
  {:added "4.1"}
  [runtime source]
  (return (-/eval-string runtime source)))
