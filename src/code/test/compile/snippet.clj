(ns code.test.compile.snippet
  (:require [code.test.base.runtime :as rt]
            [std.lib :as h]))

(defn fact-setup
  "creates a setup hook"
  {:added "3.0"}
  ([m]
   `(fn []
      ~@(h/seqify (:setup m)))))

(defn fact-teardown
  "creates a teardown hook"
  {:added "3.0"}
  ([m]
   `(fn []
      ~@(h/seqify (:teardown m)))))

(defn fact-wrap-ceremony
  "creates the setup/teardown wrapper
 
   (fact-wrap-ceremony '{:setup [(prn 1 2 3)]
                         :teardown (prn \"goodbye\")})"
  {:added "3.0"}
  ([{:keys [setup teardown guard] :as m}]
   `(fn [~'thunk]
      (fn []
        (let [~@(if setup
                  ['_ setup])
              ~'out (~'thunk)
              ~@(if teardown
                  ['_ teardown])]
          ~'out)))))

(defn fact-wrap-check
  "creates a wrapper for before and after arrows"
  {:added "3.0"}
  ([{:keys [check guard] :as m}]
   (let [{:keys [before after]} check]
     `(fn [~'thunk]
        (fn []
          (binding [rt/*results* (atom [])
                    rt/*eval-check* {:guard  ~guard
                                     :before (fn []
                                               ~@(h/seqify before))
                                     :after  (fn []
                                               ~@(h/seqify after))}]
            (~'thunk)))))))

(defn fact-slim
  "creates the slim thunk
 
   (fact-slim '[(+ a b)])"
  {:added "3.0"}
  ([bare]
   (let [slim `(fn [] ~@bare)]
     `(try
        (eval (quote ~slim))
        (catch Throwable t#
          (fn []
            (throw (ex-info (.getMessage t#) {:input (quote ~bare)}))))))))
