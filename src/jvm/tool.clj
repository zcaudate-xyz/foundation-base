(ns jvm.tool
  (:require [std.lib :as h]
            [std.lib.link :as l]
            [jvm.require :as require]))

(def hotkey-0 (fn []))
(def hotkey-1 (fn []))
(def hotkey-2 (fn []))
(def hotkey-3 (fn []))
(def hotkey-4 (fn []))
(def hotkey-5 (fn []))
(def hotkey-6 (fn []))
(def hotkey-7 (fn []))
(def hotkey-8 (fn []))
(def hotkey-9 (fn []))

(defn hotkey-set
  "set the hotkey function"
  {:added "4.0"}
  [idx f]
  (case (long idx)
    0 (alter-var-root #'hotkey-0 (fn [_] f))
    1 (alter-var-root #'hotkey-1 (fn [_] f))
    2 (alter-var-root #'hotkey-2 (fn [_] f))
    3 (alter-var-root #'hotkey-3 (fn [_] f))
    4 (alter-var-root #'hotkey-4 (fn [_] f))
    5 (alter-var-root #'hotkey-5 (fn [_] f))
    6 (alter-var-root #'hotkey-6 (fn [_] f))
    7 (alter-var-root #'hotkey-7 (fn [_] f))
    8 (alter-var-root #'hotkey-8 (fn [_] f))
    9 (alter-var-root #'hotkey-9 (fn [_] f))))

(defn- inject-reflectors
  []
  (l/link {:ns clojure.core}
          ;;jvm.tool/forange
          jvm.reflect/.%
          jvm.reflect/.%>
          jvm.reflect/.?
          jvm.reflect/.?*
          jvm.reflect/.?>
          jvm.reflect/.*
          jvm.reflect/.*>
          jvm.reflect/.&))

(defn- inject-tools
  []
  (create-ns 's)
  (l/link {:ns s}
          jvm.tool/hotkey-0
          jvm.tool/hotkey-1
          jvm.tool/hotkey-2
          jvm.tool/hotkey-3
          jvm.tool/hotkey-4
          jvm.tool/hotkey-5
          jvm.tool/hotkey-6
          jvm.tool/hotkey-7
          jvm.tool/hotkey-8
          jvm.tool/hotkey-set
          jvm.require/force-require)
  
  (l/link {:ns s}
          std.log/with-logger-basic
          std.log/spy
          std.log/trace

          std.block/layout)
  
  (l/link {:ns s}
          lib.aether/pull
          lib.aether/push
          
          jvm.artifact.search/class-seq
          
          code.project/project
          code.project/all-files
          code.test/run
          code.test/run:interrupt
          code.test/run:load
          code.test/run:test
          code.test/run-errored
          code.java.compile/javac

          indigo/test:all
          indigo/test:app
          indigo/test:base
          indigo/test:base+framework
          indigo/test:framework
          indigo/test:standard
          indigo/test:infra
          indigo/fix-tests

          code.maven/linkage
          code.maven/package
          code.maven/install
          code.maven/deploy
          code.maven/deploy-lein
          code.manage/vars
          code.manage/locate-code
          code.manage/grep
          code.manage/grep-replace
          code.manage/incomplete
          code.manage/import
          code.manage/scaffold
          code.manage/arrange
          code.manage/create-tests
          code.manage/purge
          code.manage/orphaned
          code.manage/missing
          code.manage/todos
          code.manage/unchecked
          code.manage/commented
          code.manage/in-order?
          code.manage/pedantic
          code.manage/heal-code
          code.manage/transform-code

          code.doc/publish
          code.doc/init-template
          code.doc/deploy-template
          jvm.namespace/reset
          jvm.namespace/reload
          jvm.namespace/reeval
          jvm.namespace/clear
          jvm.namespace/clear-aliases
          jvm.namespace/clear-interns
          jvm.namespace/clear-refers
          jvm.namespace/list-loaded))

(def +init+
  (do 
    (prefer-method print-method clojure.lang.IDeref java.util.Map)
    (prefer-method print-method clojure.lang.IDeref clojure.lang.IPersistentMap)
    (prefer-method clojure.pprint/simple-dispatch clojure.lang.IDeref java.util.Map)
    (prefer-method clojure.pprint/simple-dispatch clojure.lang.IDeref clojure.lang.IPersistentMap)))

(def +inject-vars+ (-> (do (inject-reflectors)
                           (inject-tools)
                           (l/link:resolve-all))
                       
                       (h/on:success
                        (fn [_]
                          ))))
