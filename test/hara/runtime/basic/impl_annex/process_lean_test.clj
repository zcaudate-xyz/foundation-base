(ns hara.runtime.basic.impl-annex.process-lean-test
  (:use code.test)
  (:require [clojure.string :as str]
            [hara.runtime.basic.impl-annex.process-lean :refer :all]
            [hara.runtime.basic.type-twostep :as twostep]
            [std.fs :as fs]
            [std.lib.os :as os]))

^{:refer hara.runtime.basic.impl-annex.process-lean/sh-exec-lean :added "4.1"}
(fact "executes a lean source file locally"
  (let [calls (atom [])
        result (with-redefs [os/sh (fn [opts] (swap! calls conj opts) :proc)
                             os/sh-wait (fn [_] nil)
                             os/sh-output (fn [_] {:exit 0 :out "2\n" :err ""})]
                 (sh-exec-lean ["lean"] "#eval 1 + 1" {:extension "lean"}))
        opts (first @calls)]
    [result
     (-> opts :args first)
     (boolean (some-> opts :args second (str/ends-with? ".lean")))
     (= (:root opts) (str (fs/parent (-> opts :args second))))])
  => ["2" "lean" true true]

  (let [result (with-redefs [os/sh (fn [_] :proc)
                             os/sh-wait (fn [_] nil)
                             os/sh-output (fn [_] {:exit 0 :out "a\nb\n" :err ""})]
                 (sh-exec-lean ["lean"] "x" {:extension "lean" :raw true}))]
    result)
  => [0 ["a" "b"]]

  (let [result (with-redefs [os/sh (fn [_] :proc)
                             os/sh-wait (fn [_] nil)
                             os/sh-output (fn [_] {:exit 1 :out "stdout\n" :err "stderr\n"})]
                 (sh-exec-lean ["lean"] "x" {:extension "lean" :stderr true}))]
    result)
  => "stderr")

^{:refer hara.runtime.basic.impl-annex.process-lean/sh-exec-lean-docker :added "4.1"}
(fact "executes a lean source file inside a docker container"
  (let [calls (atom [])
        result (with-redefs [os/sh (fn [opts] (swap! calls conj opts) :proc)
                             os/sh-wait (fn [_] nil)
                             os/sh-output (fn [_] {:exit 0 :out "ok\n" :err ""})]
                 (sh-exec-lean-docker ["lean"] "print 1" {:extension "lean"
                                                            :container {:image "lean:latest"
                                                                        :exec ["lean"]}}))
        args (:args (first @calls))
        vol-idx (.indexOf args "-v")
        vol-arg (when (>= vol-idx 0) (nth args (inc vol-idx)))
        work-idx (.indexOf args "-w")]
    [result
     (first args)
     (second args)
     (nth args 2)
     (some? vol-arg)
     (str/ends-with? (str vol-arg) ":/work")
     (when (>= work-idx 0) (= (nth args (inc work-idx)) "/work"))
     (some #(= "lean:latest" %) args)
     (boolean (some #(str/ends-with? % ".lean") args))])
  => ["ok" "docker" "run" "--rm" true true true true true])

^{:refer hara.runtime.basic.impl-annex.process-lean/sh-exec-lean-portable :added "4.1"}
(fact "chooses local or container execution based on availability"
  (with-redefs [twostep/local-exec-available? (fn [_] true)
                sh-exec-lean (fn [_ _ _] "local")
                sh-exec-lean-docker (fn [_ _ _] "docker")]
    (sh-exec-lean-portable ["lean"] "body" {:extension "lean"}))
  => "local"

  (with-redefs [twostep/local-exec-available? (fn [_] false)
                sh-exec-lean (fn [_ _ _] "local")
                sh-exec-lean-docker (fn [_ _ _] "docker")]
    (sh-exec-lean-portable ["lean"] "body" {:extension "lean"
                                               :container {:image "lean:latest"}}))
  => "docker"

  (with-redefs [twostep/local-exec-available? (fn [_] true)
                sh-exec-lean (fn [_ _ _] "local")
                sh-exec-lean-docker (fn [_ _ _] "docker")]
    (sh-exec-lean-portable ["lean"] "body" {:extension "lean"
                                               :container {:image "lean:latest"}
                                               :force-container true}))
  => "docker"

  (with-redefs [twostep/local-exec-available? (fn [_] false)
                sh-exec-lean (fn [_ _ _] "local")
                sh-exec-lean-docker (fn [_ _ _] "docker")]
    (sh-exec-lean-portable ["lean"] "body" {:extension "lean"
                                               :container {:image "lean:latest"}
                                               :container-backup false}))
  => "local")

^{:refer hara.runtime.basic.impl-annex.process-lean/transform-form :added "4.1"}
(fact "wraps forms into a standalone lean script ending in #eval"
  (transform-form '[(+ 1 2) (+ 3 4)] {})
  => '(:lines (+ 1 2) (:% (:raw-str "#eval ") (+ 3 4)))

  (transform-form '[(foo bar)] {})
  => '(:lines (:% (:raw-str "#eval ") (foo bar))))
