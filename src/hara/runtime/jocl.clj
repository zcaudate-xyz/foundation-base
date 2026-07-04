(ns hara.runtime.jocl
  (:refer-clojure :exclude [meta to-array])
  (:require [hara.runtime.jocl.env :as jocl-env]
            [hara.lang.registry :as reg]
            [hara.lang.runtime :as default]
            [std.lib.foundation :as h]))

(def ^{:added "4.1"}
  +available?+
  "True when the OpenCL native library can be loaded and has at least one
   platform.  The JOCL sub-namespaces are only loaded when this is true so
   that tests can be skipped gracefully on machines without OpenCL."
  (boolean (jocl-env/opencl-available?)))

(defn- import-jocl-ns
  "Loads `ns-sym` and interns all of its public vars into the current
   namespace.  This makes the functions/macros re-referrable by test
   namespaces without importing `org.jocl.CL` directly."
  {:added "4.1"}
  [ns-sym]
  (require ns-sym)
  (let [ns-obj (find-ns ns-sym)]
    (doseq [[sym ^clojure.lang.Var var] (ns-publics ns-obj)]
      (when-not (= sym '+available?+)
        (h/intern-var *ns* sym var)))))

(when +available?+
  (import-jocl-ns 'hara.runtime.jocl.common)
  (import-jocl-ns 'hara.runtime.jocl.meta)
  (import-jocl-ns 'hara.runtime.jocl.exec)
  (import-jocl-ns 'hara.runtime.jocl.type)
  (import-jocl-ns 'hara.runtime.jocl.runtime))

(when-not +available?+
  ;; Install a no-op :c/:jocl runtime type so that `(l/script- :c
  ;; {:runtime :jocl :test-mode true})` in test namespaces can still
  ;; compile and set up script macros even when OpenCL is missing.
  (default/install-type!
   :c :jocl
   {:type :hara/lib.jocl.dummy
    :config {:bootstrap false}
    :instance {:create (fn [config] config)}}))
