(ns repro-issue
  (:require [std.lang.base.script-macro :as macro]
            [std.lib.context.registry :as reg]
            [std.lib.context.space :as space]
            [std.lang.base.util :as ut]
            [std.lib :as h]
            [std.lang.model.spec-jq]))

;; Hack the registry to simulate the issue
(swap! reg/*registry* assoc-in [(ut/lang-context :jq) :scratch] {:some "map" :module :default})

(defn check-jq-rt []
  (let [lang :jq
        ctx (ut/lang-context lang)
        rt (h/p:space-rt-current ctx)]
    (println "Runtime for :jq is:" rt)
    (println "Type of runtime:" (type rt))
    (println "Satisfies IContext?" (satisfies? std.protocol.context/IContext rt))))

(defn -main []
  (check-jq-rt)

  ;; Try to invoke intern-!-fn
  (try
    (println "Attempting to invoke intern-!-fn...")
    (macro/intern-!-fn :jq '() {})
    (println "Successfully invoked intern-!-fn (no exception thrown)")
    (catch Exception e
      (println "Caught exception:" e)
      (.printStackTrace e))))
