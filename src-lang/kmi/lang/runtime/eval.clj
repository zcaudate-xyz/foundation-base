(ns kmi.lang.runtime.eval
  (:require [hara.lang :as l])
  (:refer-clojure :exclude [eval apply read symbol? keyword? list? vector?
                          macroexpand macroexpand-1]))

(l/script :xtalk
  {:require [[kmi.lang.common-util :as util]
             [kmi.lang.common-hash :as common-hash]
             [kmi.lang.protocol-base :as p]
             [kmi.lang.parser :as parser]
             [kmi.lang.reader :as reader]
             [kmi.lang.type-syntax :as syn]
             [kmi.lang.type-symbol :as sym]
             [kmi.lang.type-keyword :as kw]
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
;; RECUR MARKER
;;

(defn.xt recur-marker
  "creates a recur control value"
  {:added "4.1"}
  [values]
  (return {"::" "recur"
           "values" values}))

(defn.xt recur?
  "checks if a value is a recur marker"
  {:added "4.1"}
  [x]
  (return (and (xt/x:is-object? x)
               (== "recur" (xt/x:get-key x "::")))))

(defn.xt recur-values
  "returns the values carried by a recur marker"
  {:added "4.1"}
  [x]
  (return (xt/x:get-key x "values")))

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

(defn.xt tagged-list?
  "checks if a value is a list whose first element is a symbol with the given name"
  {:added "4.1"}
  [x name]
  (when (-/list? x)
    (var arr (p/to-array x))
    (var first (xt/x:get-idx arr 0))
    (return (and (-/symbol? first)
                 (== name (env/sym-name first)))))
  (return false))

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
    (when (-/recur? (-/value out))
      (return out))
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
  (var branch-out nil)
  (if (-/value out)
    (:= branch-out (eval-fn runtime env then-form))
    (:= branch-out (eval-fn runtime env (or else-form nil))))
  (return branch-out))

(defn.xt bind-pattern
  "binds a destructuring pattern to a value in an environment"
  {:added "4.1"}
  [eval-fn runtime env pattern value]
  (cond (-/symbol? pattern)
        (do (xt/x:set-key (xt/x:get-key env "bindings")
                          (env/sym-name pattern)
                          value)
            (return (-/result runtime env)))

        (or (-/vector? pattern) (-/list? pattern))
        (do (var arr (p/to-array pattern))
            (var val-arr (p/to-array value))
            (var i 0)
            (while (< i (xt/x:len arr))
              (var p (xt/x:get-idx arr i))
              (if (and (-/symbol? p)
                       (== "&" (env/sym-name p))
                       (< (+ i 1) (xt/x:len arr)))
                (do (var rest-pattern (xt/x:get-idx arr (+ i 1)))
                    (var rest-val (list/list (xt/x:unpack (xt/x:arr-slice val-arr i (xt/x:len val-arr)))))
                    (var out (-/bind-pattern eval-fn runtime env rest-pattern rest-val))
                    (when (-/error? out)
                      (return out))
                    (:= runtime (-/runtime out))
                    (:= env (-/value out))
                    (:= i (xt/x:len arr)))
                (do (var out (-/bind-pattern eval-fn runtime env p (xt/x:get-idx val-arr i)))
                    (when (-/error? out)
                      (return out))
                    (:= runtime (-/runtime out))
                    (:= env (-/value out))
                    (:= i (+ i 1))))))
            (return (-/result runtime env)))

        (-/hashmap? pattern)
        (do (var keys-vec nil)
            (var or-map nil)
            (var as-sym nil)
            (var entries (p/to-array pattern))
            (var i 0)
            (while (< i (xt/x:len entries))
              (var entry (xt/x:get-idx entries i))
              (var k (xt/x:get-idx entry 0))
              (var v (xt/x:get-idx entry 1))
              (when (-/keyword? k)
                (cond (== k (kw/keyword nil "keys")) (:= keys-vec v)
                      (== k (kw/keyword nil "or"))  (:= or-map v)
                      (== k (kw/keyword nil "as"))  (:= as-sym v)
                      true nil))
              (:= i (+ i 1)))
            (when (xt/x:not-nil? as-sym)
              (var out (-/bind-pattern eval-fn runtime env as-sym value))
              (when (-/error? out)
                (return out))
              (:= runtime (-/runtime out))
              (:= env (-/value out)))
            (when (xt/x:not-nil? keys-vec)
              (xt/for:array [k-sym (p/to-array keys-vec)]
                (var key-name (env/sym-name k-sym))
                (var key-kw (kw/keyword nil key-name))
                (var val (hm/hashmap-lookup-key value key-kw))
                (when (and (xt/x:nil? val) (xt/x:not-nil? or-map))
                  (var default-form (hm/hashmap-lookup-key or-map k-sym))
                  (when (xt/x:not-nil? default-form)
                    (var default-out (eval-fn runtime env default-form))
                    (when (-/error? default-out)
                      (return default-out))
                    (:= runtime (-/runtime default-out))
                    (:= val (-/value default-out))))
                (var out (-/bind-pattern eval-fn runtime env k-sym val))
                (when (-/error? out)
                  (return out))
                (:= runtime (-/runtime out))
                (:= env (-/value out))))
            (return (-/result runtime env)))

        true
        (return (-/error runtime
                         (xt/x:cat "unsupported binding pattern: "
                                   (util/show pattern)))))

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
    (var pattern (xt/x:get-idx pairs i))
    (var val-form (xt/x:get-idx pairs (+ i 1)))
    (var out (eval-fn runtime env val-form))
    (when (-/error? out)
      (return out))
    (:= runtime (-/runtime out))
    (var bind-out (-/bind-pattern eval-fn runtime let-env pattern (-/value out)))
    (when (-/error? bind-out)
      (return bind-out))
    (:= runtime (-/runtime bind-out))
    (:= let-env (-/value bind-out))
    (:= i (+ i 2)))
  (return (-/eval-do-array eval-fn runtime let-env body)))

(defn.xt parse-params
  "splits a parameter vector into required and rest symbols"
  {:added "4.1"}
  [params]
  (var req [])
  (var rparam nil)
  (var i 0)
  (while (< i (xt/x:len params))
    (var p (xt/x:get-idx params i))
    (if (== "&" (env/sym-name p))
      (do (:= rparam (xt/x:get-idx params (+ i 1)))
          (:= i (+ i 2)))
      (do (xt/x:arr-push req p)
          (:= i (+ i 1)))))
  (return {"req" req
           "rest" rparam}))

(defn.xt make-closure
  "creates a closure from a fn form"
  {:added "4.1"}
  [env parts]
  (var params-form (xt/x:get-idx parts 1))
  (var params (p/to-array params-form))
  (var parsed (-/parse-params params))
  (var body (xt/x:arr-slice parts 2 (xt/x:len parts)))
  (return {"type" "kmi.fn"
           "params" params
           "req" (xt/x:get-key parsed "req")
           "rest" (xt/x:get-key parsed "rest")
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

(defn.xt eval-syntax-quote
  "evaluates a syntax-quote form, returning the constructed form"
  {:added "4.1"}
  [eval-fn runtime env form]
  (cond (-/tagged-list? form "unquote")
        (return (eval-fn runtime env (xt/x:get-idx (p/to-array form) 1)))

        (-/list? form)
        (do (var out [])
            (xt/for:array [item (p/to-array form)]
              (cond (-/tagged-list? item "unquote-splicing")
                    (do (var splice-form (xt/x:get-idx (p/to-array item) 1))
                        (var splice-out (eval-fn runtime env splice-form))
                        (when (-/error? splice-out)
                          (return splice-out))
                        (:= runtime (-/runtime splice-out))
                        (xt/for:array [v (p/to-array (-/value splice-out))]
                          (xt/x:arr-push out v)))

                    true
                    (do (var item-out (-/eval-syntax-quote eval-fn runtime env item))
                        (when (-/error? item-out)
                          (return item-out))
                        (:= runtime (-/runtime item-out))
                        (xt/x:arr-push out (-/value item-out)))))
            (return (-/result runtime (list/list (xt/x:unpack out)))))

        (-/vector? form)
        (do (var out [])
            (xt/for:array [item (p/to-array form)]
              (cond (-/tagged-list? item "unquote-splicing")
                    (do (var splice-form (xt/x:get-idx (p/to-array item) 1))
                        (var splice-out (eval-fn runtime env splice-form))
                        (when (-/error? splice-out)
                          (return splice-out))
                        (:= runtime (-/runtime splice-out))
                        (xt/for:array [v (p/to-array (-/value splice-out))]
                          (xt/x:arr-push out v)))

                    true
                    (do (var item-out (-/eval-syntax-quote eval-fn runtime env item))
                        (when (-/error? item-out)
                          (return item-out))
                        (:= runtime (-/runtime item-out))
                        (xt/x:arr-push out (-/value item-out)))))
            (return (-/result runtime (vec/vector (xt/x:unpack out)))))

        (-/hashmap? form)
        (do (var out [])
            (xt/for:array [entry (p/to-array form)]
              (var k (xt/x:get-idx entry 0))
              (var v (xt/x:get-idx entry 1))
              (var k-out (-/eval-syntax-quote eval-fn runtime env k))
              (when (-/error? k-out)
                (return k-out))
              (:= runtime (-/runtime k-out))
              (var v-out (-/eval-syntax-quote eval-fn runtime env v))
              (when (-/error? v-out)
                (return v-out))
              (:= runtime (-/runtime v-out))
              (xt/x:arr-push out (-/value k-out))
              (xt/x:arr-push out (-/value v-out)))
            (return (-/result runtime (hm/hashmap (xt/x:unpack out)))))

        (-/hashset? form)
        (do (var out [])
            (xt/for:array [item (p/to-array form)]
              (var item-out (-/eval-syntax-quote eval-fn runtime env item))
              (when (-/error? item-out)
                (return item-out))
              (:= runtime (-/runtime item-out))
              (xt/x:arr-push out (-/value item-out)))
            (return (-/result runtime (hs/hashset (xt/x:unpack out)))))

        true
        (return (-/result runtime form))))

(defn.xt eval-defmacro
  "evaluates a defmacro form, binding a macro transformer"
  {:added "4.1"}
  [eval-fn runtime env parts]
  (var sym (xt/x:get-idx parts 1))
  (var params-form (xt/x:get-idx parts 2))
  (var params (p/to-array params-form))
  (var parsed (-/parse-params params))
  (var body (xt/x:arr-slice parts 3 (xt/x:len parts)))
  (var closure {"type" "kmi.fn"
                "params" params
                "req" (xt/x:get-key parsed "req")
                "rest" (xt/x:get-key parsed "rest")
                "body" body
                "env" env
                "macro" true})
  (return (-/result (env/ns-assoc-macro runtime sym closure) closure)))

(defn.xt eval-deref
  "evaluates a deref form on a var object"
  {:added "4.1"}
  [eval-fn runtime env parts]
  (var form (xt/x:get-idx parts 1))
  (var out (eval-fn runtime env form))
  (when (-/error? out)
    (return out))
  (var val (-/value out))
  (when (and (xt/x:is-object? val)
             (== "var" (xt/x:get-key val "::")))
    (return (-/eval-symbol (-/runtime out) env
                           (sym/symbol (xt/x:get-key val "ns")
                                       (xt/x:get-key val "name")))))
  (return out))

(defn.xt eval-var
  "evaluates a var-quote form, returning a var descriptor"
  {:added "4.1"}
  [runtime env parts]
  (var sym (xt/x:get-idx parts 1))
  (var ns-name (env/sym-ns sym))
  (when (xt/x:nil? ns-name)
    (:= ns-name (env/current-ns-name runtime)))
  (return (-/result runtime {"::" "var"
                            "ns" ns-name
                            "name" (env/sym-name sym)})))

(defn.xt eval-in-ns
  "evaluates an in-ns form, switching the current namespace"
  {:added "4.1"}
  [eval-fn runtime env parts]
  (var form (xt/x:get-idx parts 1))
  (var out (eval-fn runtime env form))
  (when (-/error? out)
    (return out))
  (var sym (-/value out))
  (var ns-name (env/sym-name sym))
  (var rt (env/ns-ensure (-/runtime out) ns-name))
  (return (-/result (env/runtime-set-ns rt ns-name) nil)))

(defn.xt spec-ns-name
  "extracts a namespace name from a require/use spec"
  {:added "4.1"}
  [spec]
  (cond (-/symbol? spec)
        (return (env/sym-name spec))

        (or (-/vector? spec) (-/list? spec))
        (return (env/sym-name (xt/x:get-idx (p/to-array spec) 0)))

        true
        (return nil)))

(defn.xt eval-require-spec
  "processes a single require spec"
  {:added "4.1"}
  [eval-fn runtime env spec]
  (when (-/symbol? spec)
    (return (-/result (env/ns-ensure runtime (env/sym-name spec)) nil)))
  (var spec-arr (p/to-array spec))
  (var ns-sym (xt/x:get-idx spec-arr 0))
  (var ns-name (env/sym-name ns-sym))
  (var rt (env/ns-ensure runtime ns-name))
  (var i 1)
  (while (< i (xt/x:len spec-arr))
    (var k (xt/x:get-idx spec-arr i))
    (var v (xt/x:get-idx spec-arr (+ i 1)))
    (when (-/keyword? k)
      (var k-name (env/sym-name k))
      (cond (== k-name "as")
            (:= rt (env/ns-set-alias rt (env/sym-name v) ns-name))

            (== k-name "refer")
            (cond (-/keyword? v)
                  (when (== "all" (env/sym-name v))
                    (:= rt (env/ns-refer-all rt ns-name)))

                  true
                  (xt/for:array [s (p/to-array v)]
                    (:= rt (env/ns-refer rt ns-name s))))))
    (:= i (+ i 2)))
  (return (-/result rt nil)))

(defn.xt eval-require
  "evaluates a require form"
  {:added "4.1"}
  [eval-fn runtime env parts]
  (var specs-form (xt/x:get-idx parts 1))
  (var form-out (eval-fn runtime env specs-form))
  (when (-/error? form-out)
    (return form-out))
  (:= runtime (-/runtime form-out))
  (var specs-data (-/value form-out))
  (var specs nil)
  (cond (-/symbol? specs-data)
        (:= specs (vec/vector specs-data))

        (-/vector? specs-data)
        (do (var arr (p/to-array specs-data))
            (var first (xt/x:get-idx arr 0))
            (if (-/symbol? first)
              (:= specs (vec/vector specs-data))
              (:= specs specs-data)))

        (-/list? specs-data)
        (:= specs (vec/vector (xt/x:unpack (p/to-array specs-data))))

        true
        (return (-/error runtime "invalid require form")))
  (var rt runtime)
  (xt/for:array [spec (p/to-array specs)]
    (var out (-/eval-require-spec eval-fn rt env spec))
    (when (-/error? out)
      (return out))
    (:= rt (-/runtime out)))
  (return (-/result rt nil)))

(defn.xt eval-use
  "evaluates a use form, referring all vars from a namespace"
  {:added "4.1"}
  [eval-fn runtime env parts]
  (var form (xt/x:get-idx parts 1))
  (var out (eval-fn runtime env form))
  (when (-/error? out)
    (return out))
  (var sym (-/value out))
  (var ns-name (env/sym-name sym))
  (var rt (env/ns-ensure (-/runtime out) ns-name))
  (return (-/result (env/ns-refer-all rt ns-name) nil)))

(defn.xt eval-host-interop
  "evaluates a host interop form: (. target member args*)"
  {:added "4.1"}
  [eval-fn runtime env parts]
  (var target-form (xt/x:get-idx parts 1))
  (var target-out (eval-fn runtime env target-form))
  (when (-/error? target-out)
    (return target-out))
  (:= runtime (-/runtime target-out))
  (var target (-/value target-out))
  (var member-form (xt/x:get-idx parts 2))
  (var member-name nil)
  (cond (-/symbol? member-form)
        (:= member-name (env/sym-name member-form))

        (xt/x:is-string? member-form)
        (:= member-name member-form)

        true
        (return (-/error runtime "interop member must be a symbol or string")))
  (var arg-forms (xt/x:arr-slice parts 3 (xt/x:len parts)))
  (when (== 0 (xt/x:len arg-forms))
    (return (-/result runtime (xt/x:get-key target member-name))))
  (var args [])
  (xt/for:array [af arg-forms]
    (var out (eval-fn runtime env af))
    (when (-/error? out)
      (return out))
    (:= runtime (-/runtime out))
    (xt/x:arr-push args (-/value out)))
  (var f (xt/x:get-key target member-name))
  (return (-/result runtime (xt/x:apply f args))))

(defn.xt eval-throw
  "evaluates a throw form, returning an error result"
  {:added "4.1"}
  [eval-fn runtime env parts]
  (var form (xt/x:get-idx parts 1))
  (var out (eval-fn runtime env form))
  (when (-/error? out)
    (return out))
  (return (-/error runtime (-/value out))))

(defn.xt env-loop-find
  "finds the nearest loop environment in the lexical chain"
  {:added "4.1"}
  [env]
  (while (xt/x:not-nil? env)
    (when (xt/x:has-key? env "loop-bindings")
      (return env))
    (:= env (xt/x:get-key env "parent")))
  (return nil))

(defn.xt eval-loop
  "evaluates a loop form with tail recursion"
  {:added "4.1"}
  [eval-fn runtime env parts]
  (var bindings-form (xt/x:get-idx parts 1))
  (var body (xt/x:arr-slice parts 2 (xt/x:len parts)))
  (var pairs (p/to-array bindings-form))
  (var loop-env (env/env-create env))
  (var loop-bindings [])
  (var i 0)
  (while (< i (xt/x:len pairs))
    (var pattern (xt/x:get-idx pairs i))
    (var val-form (xt/x:get-idx pairs (+ i 1)))
    (var out (eval-fn runtime env val-form))
    (when (-/error? out)
      (return out))
    (:= runtime (-/runtime out))
    (var bind-out (-/bind-pattern eval-fn runtime loop-env pattern (-/value out)))
    (when (-/error? bind-out)
      (return bind-out))
    (:= runtime (-/runtime bind-out))
    (:= loop-env (-/value bind-out))
    (xt/x:arr-push loop-bindings pattern)
    (:= i (+ i 2)))
  (xt/x:set-key loop-env "loop-bindings" loop-bindings)
  (xt/x:set-key loop-env "loop-body" body)
  (while true
    (var out (-/eval-do-array eval-fn runtime loop-env body))
    (when (-/error? out)
      (return out))
    (var val (-/value out))
    (if (-/recur? val)
      (do (var values (-/recur-values val))
          (when (not= (xt/x:len values) (xt/x:len loop-bindings))
            (return (-/error runtime
                             (xt/x:cat "recur arity mismatch: expected "
                                       (xt/x:to-string (xt/x:len loop-bindings))
                                       ", got "
                                       (xt/x:to-string (xt/x:len values))))))
          (xt/for:index [idx [0 (xt/x:len loop-bindings)]]
            (var bind-out (-/bind-pattern eval-fn runtime loop-env
                                          (xt/x:get-idx loop-bindings idx)
                                          (xt/x:get-idx values idx)))
            (when (-/error? bind-out)
              (return bind-out))
            (:= runtime (-/runtime bind-out))
            (:= loop-env (-/value bind-out))))
      (return out))))

(defn.xt eval-recur
  "evaluates a recur form, returning a recur marker"
  {:added "4.1"}
  [eval-fn runtime env parts]
  (var loop-env (-/env-loop-find env))
  (when (xt/x:nil? loop-env)
    (return (-/error runtime "recur outside of loop")))
  (var arg-forms (xt/x:arr-slice parts 1 (xt/x:len parts)))
  (var values [])
  (xt/for:array [af arg-forms]
    (var out (eval-fn runtime env af))
    (when (-/error? out)
      (return out))
    (:= runtime (-/runtime out))
    (xt/x:arr-push values (-/value out)))
  (return (-/result runtime (-/recur-marker values))))

(defn.xt apply-fn
  "applies a function to evaluated arguments"
  {:added "4.1"}
  [eval-fn runtime f args]
  (cond (xt/x:is-function? f)
        (return (-/result runtime (xt/x:apply f args)))

        (and (xt/x:is-object? f)
             (== "kmi.fn" (xt/x:get-key f "type")))
        (do (var req (xt/x:get-key f "req"))
            (var rparam (xt/x:get-key f "rest"))
            (var req-count (xt/x:len req))
            (var arg-count (xt/x:len args))
            (cond (xt/x:not-nil? rparam)
                  (when (< arg-count req-count)
                    (return (-/error runtime
                                     (xt/x:cat "arity mismatch: expected at least "
                                               (xt/x:to-string req-count)
                                               ", got "
                                               (xt/x:to-string arg-count)))))

                  (not= arg-count req-count)
                  (return (-/error runtime
                                   (xt/x:cat "arity mismatch: expected "
                                             (xt/x:to-string req-count)
                                             ", got "
                                             (xt/x:to-string arg-count)))))
            (var call-env (env/env-create (xt/x:get-key f "env")))
            (xt/for:index [i [0 req-count]]
              (var pattern (xt/x:get-idx req i))
              (var bind-out (-/bind-pattern eval-fn runtime call-env pattern (xt/x:get-idx args i)))
              (when (-/error? bind-out)
                (return bind-out))
              (:= runtime (-/runtime bind-out))
              (:= call-env (-/value bind-out)))
            (when (xt/x:not-nil? rparam)
              (var tail (xt/x:arr-slice args req-count arg-count))
              (var bind-out (-/bind-pattern eval-fn runtime call-env rparam (list/list (xt/x:unpack tail))))
              (when (-/error? bind-out)
                (return bind-out))
              (:= runtime (-/runtime bind-out))
              (:= call-env (-/value bind-out)))
            (var out (-/eval-do-array eval-fn runtime call-env (xt/x:get-key f "body")))
            (return out))

        true
        (return (-/error runtime
                         (xt/x:cat "not a function: "
                                   (util/show f))))))

(defn.xt eval-apply
  "evaluates an apply form"
  {:added "4.1"}
  [eval-fn runtime env parts]
  (var arg-forms (xt/x:arr-slice parts 1 (xt/x:len parts)))
  (when (< (xt/x:len arg-forms) 2)
    (return (-/error runtime "apply requires a function and argument collection")))
  (var f-out (eval-fn runtime env (xt/x:get-idx arg-forms 0)))
  (when (-/error? f-out)
    (return f-out))
  (:= runtime (-/runtime f-out))
  (var f (-/value f-out))
  (var args [])
  (var i 1)
  (while (< i (- (xt/x:len arg-forms) 1))
    (var out (eval-fn runtime env (xt/x:get-idx arg-forms i)))
    (when (-/error? out)
      (return out))
    (:= runtime (-/runtime out))
    (xt/x:arr-push args (-/value out))
    (:= i (+ i 1)))
  (var last-form (xt/x:get-idx arg-forms (- (xt/x:len arg-forms) 1)))
  (var last-out (eval-fn runtime env last-form))
  (when (-/error? last-out)
    (return last-out))
  (:= runtime (-/runtime last-out))
  (var last-val (-/value last-out))
  (xt/for:array [v (p/to-array last-val)]
    (xt/x:arr-push args v))
  (return (-/apply-fn eval-fn runtime f args)))

;;
;; MACRO EXPANSION
;;

(defn.xt macroexpand-one
  "expands a form once if its head is a macro"
  {:added "4.1"}
  [eval-fn runtime env form]
  (when (not (-/list? form))
    (return form))
  (var parts (p/to-array form))
  (var op (xt/x:get-idx parts 0))
  (when (not (-/symbol? op))
    (return form))
  (var macro-fn (env/macro-lookup runtime op))
  (when (xt/x:nil? macro-fn)
    (return form))
  (var args (xt/x:arr-slice parts 1 (xt/x:len parts)))
  (var out (-/apply-fn eval-fn runtime macro-fn args))
  (return (-/value out)))

(defn.xt macroexpand-all
  "repeatedly expands macros until stable"
  {:added "4.1"}
  [eval-fn runtime env form]
  (var current form)
  (while true
    (var next (-/macroexpand-one eval-fn runtime env current))
    (when (util/eq current next)
      (return current))
    (:= current next))
  (return current))

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
  (var expanded (-/macroexpand-one eval-fn runtime env form))
  (when (not= form expanded)
    (return (eval-fn runtime env expanded)))
  (var parts (p/to-array form))
  (var op (xt/x:first parts))
  (when (-/symbol? op)
    (var op-name (env/sym-name op))
    (cond (== op-name "quote")   (return (-/result runtime (xt/x:get-idx parts 1)))
          (== op-name "if")     (return (-/eval-if eval-fn runtime env parts))
          (== op-name "do")     (return (-/eval-do-array eval-fn runtime env (xt/x:arr-slice parts 1 (xt/x:len parts))))
          (== op-name "let")    (return (-/eval-let eval-fn runtime env parts))
          (== op-name "loop")   (return (-/eval-loop eval-fn runtime env parts))
          (== op-name "recur")  (return (-/eval-recur eval-fn runtime env parts))
          (== op-name "fn")     (return (-/result runtime (-/make-closure env parts)))
          (== op-name "def")    (return (-/eval-def eval-fn runtime env parts))
          (== op-name "defmacro") (return (-/eval-defmacro eval-fn runtime env parts))
          (== op-name "syntax-quote") (return (-/eval-syntax-quote eval-fn runtime env (xt/x:get-idx parts 1)))
          (== op-name "unquote") (return (-/error runtime "unquote outside syntax-quote"))
          (== op-name "unquote-splicing") (return (-/error runtime "unquote-splicing outside syntax-quote"))
          (== op-name "deref")  (return (-/eval-deref eval-fn runtime env parts))
          (== op-name "var")    (return (-/eval-var runtime env parts))
          (== op-name "apply")  (return (-/eval-apply eval-fn runtime env parts))
          (== op-name "in-ns")  (return (-/eval-in-ns eval-fn runtime env parts))
          (== op-name "require") (return (-/eval-require eval-fn runtime env parts))
          (== op-name "use")    (return (-/eval-use eval-fn runtime env parts))
          (== op-name "host")   (return (-/eval-host-interop eval-fn runtime env parts))
          (== op-name "throw")  (return (-/eval-throw eval-fn runtime env parts))
          true                  (return (-/eval-call eval-fn runtime env parts))))
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
  (var entries (p/to-array form))
  (var i 0)
  (while (< i (xt/x:len entries))
    (var entry (xt/x:get-idx entries i))
    (var k (xt/x:get-idx entry 0))
    (var v (xt/x:get-idx entry 1))
    (var k-out (eval-fn runtime env k))
    (when (-/error? k-out)
      (return k-out))
    (:= runtime (-/runtime k-out))
    (var v-out (eval-fn runtime env v))
    (when (-/error? v-out)
      (return v-out))
    (:= runtime (-/runtime v-out))
    (xt/x:arr-push items (-/value k-out))
    (xt/x:arr-push items (-/value v-out))
    (:= i (+ i 1)))
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
