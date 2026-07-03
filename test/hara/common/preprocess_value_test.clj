(ns hara.common.preprocess-value-test
  (:use code.test)
  (:require [hara.common.emit-helper :as helper]
            [hara.common.grammar :as grammar]
            [hara.common.preprocess-value :refer :all]
            [hara.model.spec-js :as js]
            [hara.model.spec-lua :as lua]))

(def +reserved+
  (-> (grammar/build)
      (grammar/to-reserved)))

(def +grammar+
  (grammar/grammar :test +reserved+ helper/+default+))

(fact "namespaced standalone macros with block-valued targets are lowered in return position"
  (let [grammar {:reserved {'x:notify-http {:emit :macro
                                            :macro (fn [[_ host port value id key opts]]
                                                     (list 'try
                                                           (list 'fetch host port value id key opts)
                                                           (list 'return ["async"])
                                                           (list 'catch 'e
                                                                 (list 'return ["unable to connect"]))))
                                            :op-spec {:allow-blocks true}}}}
        template-fn (fn [host port value id key opts]
                      (list 'x:notify-http host port value id key opts))
        modules {'U.notify {:fragment {'notify-http {:template template-fn
                                                     :standalone true
                                                     :form '(fn [host port value id key opts])}}}}
        mopts '{:module {:id JS.core
                         :link {u U.notify}}}]
    (process-value-form
     '(return (u/notify-http "127.0.0.1" 18130 "hello" "oneshot/abc" nil {}))
     grammar
     modules
     mopts))
  => '(try
        (fetch "127.0.0.1" 18130 "hello" "oneshot/abc" nil {})
        (return ["async"])
        (catch e
          (return ["unable to connect"]))))

^{:refer hara.common.preprocess-value/value-block-entry :added "4.1"}
(fact "returns the reserved entry for block-valued macro calls"
  (let [macro (fn [_] nil)
        grammar {:reserved {'x:block {:emit :macro
                                      :macro macro
                                      :op-spec {:allow-blocks true}}}}]
    (value-block-entry '(x:block 1 2) grammar)
    => {:emit :macro :macro macro :op-spec {:allow-blocks true}}

    (value-block-entry '(x:block 1 2)
                       {:reserved {'x:block {:emit :macro
                                             :macro macro
                                             :op-spec {}}}})
    => nil

    (value-block-entry 'x:block grammar)
    => nil))

^{:refer hara.common.preprocess-value/expand-value-block :added "4.1"}
(fact "expands block-valued macro calls"
  (let [macro (fn [[_ & body]] (apply list '+ body))
        grammar {:reserved {'x:block {:emit :macro
                                      :macro macro
                                      :op-spec {:allow-blocks true}}}}]
    (expand-value-block '(x:block 1 2 3) grammar nil {})
    => '(+ 1 2 3)

    (expand-value-block '(foo 1 2) {} nil {})
    => nil))

^{:refer hara.common.preprocess-value/resolve-block-form :added "4.1"}
(fact "resolves block-valued forms and namespaced fragment wrappers"
  (let [macro (fn [_] nil)
        grammar {:reserved {'x:block {:emit :macro
                                      :macro macro
                                      :op-spec {:allow-blocks true}}}}
        modules {'U.test {:fragment {'my-op {:template (fn [a b]
                                                          (list 'x:block a b))}}}}
        mopts '{:module {:id JS.core
                         :link {u U.test}}}]
    (resolve-block-form '(x:block 1) grammar nil {})
    => '(x:block 1)

    (resolve-block-form '(foo 1) grammar nil {})
    => nil

    (resolve-block-form '(u/my-op 1 2) grammar modules mopts)
    => '(x:block 1 2)))

^{:refer hara.common.preprocess-value/value-template-args :added "4.1"}
(fact "derives template value args from arglists metadata"
  (value-template-args
   (with-meta
     (fn [_ sym value] [sym value])
     {:arglists '([ctx sym value])}))
  => '[sym value]

  (value-template-args
   (with-meta
     (fn [_ [sym value]] [sym value])
     {:arglists '(( [ctx sym value] ))}))
  => '[sym value]

  (value-template-args
   '([x y])
   (with-meta
     (fn [_ a b & more] [a b more])
     {:arglists '([ctx a b & more])}))
  => '[x y])

^{:refer hara.common.preprocess-value/value-standalone :added "4.1"}
(fact "callable xtalk intrinsics use shared value-standalone compilation"
  (value-standalone 'x:add +grammar+)
  => '(fn [x y] (return (+ x y)))

  (value-standalone 'x:arr-push js/+grammar+)
  => '(fn [arr value]
        (. arr (push value))
        (return arr))

  (value-standalone 'proto:create lua/+grammar+)
  => '(fn [m]
        (do
          (var mt m)
          (:= (. mt __index) mt)
          (return mt)))

  (value-standalone 'hello
                    {:reserved {'hello {:emit :macro
                                        :macro (with-meta
                                                 (fn [[_ a b]]
                                                   (list '+ a b))
                                                  {:arglists '([_ a b])})
                                        :value/standalone true}}})
  => '(fn [a b] (return (+ a b))))

^{:refer hara.common.preprocess-value/process-value-form :added "4.1"}
(fact "block-valued xtalk macros are lowered in value positions"
  (value-standalone 'x:type-native js/+grammar+)
  => '(fn [value]
        (do
          (when (== value nil)
            (return nil))
          (var t := (typeof value))
          (if (== t "object")
            (cond
              (Array.isArray value)
              (return "array")
              :else
              (do
                (var tn := (. value ["constructor"] ["name"]))
                (if (== tn "Object")
                  (return "object")
                  (return tn))))
            (return t))))

  (process-value-form '(return (x:type-native obj))
                      js/+grammar+
                      '{:module {:id JS.core
                                 :link {- JS.core}}})
  => '(do
        (when (== obj nil)
          (return nil))
        (var t := (typeof obj))
        (if (== t "object")
          (cond
            (Array.isArray obj)
            (return "array")
            :else
            (do
              (var tn := (. obj ["constructor"] ["name"]))
              (if (== tn "Object")
                (return "object")
                (return tn))))
          (return t)))

  (process-value-form '(g (x:type-native obj))
                      js/+grammar+
                      '{:module {:id JS.core
                                 :link {- JS.core}}})
  => '(g ((fn [value]
            (do
              (when (== value nil)
                (return nil))
              (var t := (typeof value))
              (if (== t "object")
                (cond
                  (Array.isArray value)
                  (return "array")
                  :else
                  (do
                    (var tn := (. value ["constructor"] ["name"]))
                    (if (== tn "Object")
                      (return "object")
                      (return tn))))
                (return t))))
          obj)))