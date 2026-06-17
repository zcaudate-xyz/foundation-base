(ns hara.runtime.basic.impl-annex.process-circom
  (:require [hara.runtime.basic.type-common :as common]
             [hara.runtime.basic.type-twostep :as twostep]
             [std.fs :as fs]
             [hara.lang.book :as book]
             [hara.lang.impl :as impl]
            [hara.lang.pointer :as ptr]
            [hara.lang.runtime :as rt]
            [hara.common.util :as ut]
            [std.lib.os :as os]))

(defn sh-exec-circom
  "compiles a circom circuit and returns compiler output.

   By default only compiles the circuit (producing .r1cs/.sym output). This
   avoids the heavy C++ witness-generator toolchain (nasm, nlohmann-json,
   libgmp) while still exercising the full compile phase through the runtime."
  {:added "4.0"}
  [input-args input-body {:keys [extension
                                 root
                                 compile-only]
                          :as opts
                          :or {compile-only true}}]
  (let [tmp-exec (java.io.File/createTempFile "tmp" "")
        tmp-file (str tmp-exec "." extension)
        _        (spit tmp-file input-body)
        parent   (str (fs/parent tmp-file))
        basename (fs/file-name tmp-exec)
        cpp-dir  (str parent "/" basename "_cpp")

        ;; Compile: circom <file> --output <parent> (and optionally --c)
        compile-args (vec (concat input-args
                                  [(str tmp-file)
                                   "--output" parent]
                                  (when-not compile-only ["--c"])))
        compile-ret (os/sh {:args compile-args :root parent})]
    (if compile-only
      (str compile-ret)
      (let [_ (os/sh {:args ["make" "-C" cpp-dir] :root parent})
            exec-path (str cpp-dir "/" basename)]
        (str (os/sh {:args [exec-path] :root parent}))))))

(def +program-init+
  (common/put-program-options
   :circom {:default  {:twostep     :circom
                       :interactive false
                       :ws-client   false}
            :env      {:circom   {:exec "circom"
                                  :extension   "circom"
                                  :exec-fn sh-exec-circom
                                  :stderr true
                                  :flags  {:twostep []
                                           :interactive false
                                           :json false
                                           :ws-client false}}}}))

(defn transform-form
  "wraps runtime body forms into a complete circom program"
  {:added "4.1"}
  [forms opts]
  (let [forms (if (symbol? (first forms)) [forms] forms)]
    (if (some #(and (seq? %) (#{'main 'pragma} (first %))) forms)
      (apply list 'do forms)
      `(:- "\ncomponent main = " ~(last forms) ";"))))

(def +circom-twostep-config+
  (common/set-context-options
   [:circom :twostep :default]
   {:exec-fn #'sh-exec-circom
    :emit  {:body {:transform #'transform-form}}}))

(def +circom-twostep+
  [(rt/install-type!
    :circom :twostep
    {:type :hara/rt.twostep
     :instance {:create twostep/rt-twostep:create}})])
