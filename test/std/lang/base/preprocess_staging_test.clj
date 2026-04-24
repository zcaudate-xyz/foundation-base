(ns std.lang.base.preprocess-staging-test
  (:use code.test)
  (:require [std.lang.base.emit-helper :as helper]
            [std.lang.base.emit-prep-lua-test :as prep]
            [std.lang.base.grammar :as grammar]
            [std.lang.base.preprocess-staging :refer :all]
            [std.lang.model.spec-js :as js]))

(def +reserved+
  (-> (grammar/build)
      (grammar/to-reserved)))

(def +grammar+
  (grammar/grammar :test +reserved+ helper/+default+))

^{:refer std.lang.base.preprocess-staging/to-staging-form :added "4.1"}
(fact "different staging forms"
  (to-staging-form '(!:template (+ 1 2 3))
                   nil
                   (:modules prep/+book-min+)
                   '{:module {:link {u L.core}}}
                   nil
                   identity)
  => 6

  @(to-staging-form '(!:eval (+ 1 2 3))
                    nil
                    (:modules prep/+book-min+)
                    '{:module {:link {u L.core}}}
                    nil
                    identity)
  => '(!:eval (+ 1 2 3))

  (to-staging-form '(hello 1 2 3)
                   {:reserved {'hello {:type :template
                                       :macro (fn [[_ & args]]
                                                (cons '+ (concat args args)))}}}
                   (:modules prep/+book-min+)
                   '{:module {:link {u L.core}}}
                   nil
                   identity)
  => '(+ 1 2 3 1 2 3)

  (to-staging-form '(hello 1 2 3)
                   {:reserved {'hello {:emit :hard-link
                                       :raw 'world}}}
                   (:modules prep/+book-min+)
                   '{:module {:link {u L.core}}}
                   nil
                   identity)
  => '(world 1 2 3))

(fact "staging failures include macro context"
  (try
    (to-staging-form (with-meta '(hello 1 2 3) {:line 21})
                     {:reserved {'hello {:type :template
                                         :macro (fn [_]
                                                  (throw (ex-info "boom" {:probe true})))}}}
                     (:modules prep/+book-min+)
                     '{:lang :lua
                       :module {:id L.core
                                :link {u L.core}}}
                     nil
                     identity)
    nil
    (catch Throwable t
      (select-keys (ex-data t)
                   [:probe
                    :std.lang/phase
                    :std.lang/subsystem
                    :std.lang/lang
                    :std.lang/line
                    :std.lang/module
                    :std.lang/symbol
                    :std.lang/form])))
  => '{:probe true
        :std.lang/phase :staging/reserved-template
        :std.lang/subsystem :std.lang.base.preprocess/reserved-template
        :std.lang/lang :lua
        :std.lang/line 21
        :std.lang/module L.core
        :std.lang/symbol hello
        :std.lang/form (hello 1 2 3)})

^{:refer std.lang.base.preprocess-staging/to-staging :added "4.1"}
(fact "converts the stage"
  (to-staging '(u/add (u/identity-fn 1) 2)
              nil
              (:modules prep/+book-min+)
              '{:module {:link {u L.core}}})
  => '[(+ (L.core/identity-fn 1) 2) #{L.core/identity-fn} #{L.core/add} {}]

  (to-staging '(u/sub (u/add (u/identity-fn 1) 2)
                      (-/hello))
              nil
              (:modules prep/+book-min+)
              '{:entry {:id hello}
                :module {:id L.util
                         :link {u L.core}}})
  => '[(- (+ (L.core/identity-fn 1) 2) (L.util/hello))
       #{L.core/identity-fn}
       #{L.core/add L.core/sub}
       {}]

  ((juxt identity
         (comp meta last first))
   (to-staging '(var a := (u/identity-fn 1) :inline)
               {:reserved {'var {:emit :def-assign}}}
               (:modules prep/+book-min+)
               '{:module {:link {u L.core}}}))
  => '[[(var a := (L.core/identity-fn 1)) #{} #{} {}] #:assign{:inline true}]

  (first
   (to-staging '(var a := (x:type-native obj))
               js/+grammar+
               {}
               '{:module {:id JS.core
                          :link {- JS.core}}}))
  => '(do
        (var* :let a := nil)
        (do
          (when (== obj nil)
            (return nil))
          (var t := (typeof obj))
          (if (== t "object")
            (cond
              (Array.isArray obj)
              (:= a "array")
              :else
              (do
                (var tn := (. obj ["constructor"] ["name"]))
                (if (== tn "Object")
                  (:= a "object")
                  (:= a tn))))
            (:= a t))))

  (first
   (to-staging '(:= a (x:type-native obj))
               js/+grammar+
               {}
               '{:module {:id JS.core
                          :link {- JS.core}}}))
  => '(do
        (when (== obj nil)
          (return nil))
        (var t := (typeof obj))
        (if (== t "object")
          (cond
            (Array.isArray obj)
            (:= a "array")
            :else
            (do
              (var tn := (. obj ["constructor"] ["name"]))
              (if (== tn "Object")
                (:= a "object")
                (:= a tn))))
          (:= a t)))

  (to-staging 'x:add
              +grammar+
              {}
              '{:module {:id L.core
                         :link {}}})
  => '[(fn [x y] (return (+ x y))) #{} #{} {}])

(fact "language macro form heads do not recurse during staging"
  (first
   (to-staging '(do (for:object [[k v] obj]
                        (return false)))
               js/+grammar+
               {}
               '{:module {:id JS.core
                          :link {- JS.core}}}))
  => '(do (for:object [[k v] obj]
          (return false))))

(fact "core macros remain deferred during staging"
  (first
   (to-staging '(if check
                  (return a)
                  (return b))
               js/+grammar+
               {}
               '{:module {:id JS.core
                          :link {- JS.core}}}))
  => '(if check
        (return a)
        (return b)))

(fact "xtalk operator heads remain in-place inside forms"
  (first
   (to-staging '(do (x:set-key obj
                               (x:arr-first e)
                               (x:arr-second e))
                    (return obj))
               +grammar+
               {}
               '{:module {:id xt.lang.common-lib
                          :link {- xt.lang.common-lib}}}))
  => '(do (x:set-key obj
                     (x:arr-first e)
                     (x:arr-second e))
          (return obj)))

^{:refer std.lang.base.preprocess-staging/to-resolve :added "4.1"}
(fact "resolves only the code symbols (no macroexpansion)"
  (to-resolve '(u/add (u/identity-fn 1) 2)
              nil
              (:modules prep/+book-min+)
              '{:module {:link {u L.core}}})
  => '(L.core/add (L.core/identity-fn 1) 2))
