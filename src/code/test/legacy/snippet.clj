(ns code.test.legacy.snippet
  (:require [code.test.base.runtime :as rt]
            [std.lib :as h]))

(defn vecify
  "puts the item in a vector if not already
 
   (vecify 1)
   => [1]"
  {:added "3.0"}
  ([obj]
   (if (vector? obj)
     obj
     [obj])))

(defn replace-bindings
  "replaces values of current bindings
 
   (replace-bindings '[a 1 b 2 c 3]
                     '[b 4])
   => '[a 1 b 4 c 3]"
  {:added "3.0"}
  ([old new]
   (let [old (partition 2 old)
         new (apply hash-map new)]
     (mapcat (fn [[sym val]]
               (if (contains? new sym)
                 [sym (get new sym)]
                 [sym val]))
             old))))

(defn wrap-bindings
  "wraps the form in bindings
 
   (wrap-bindings '(+ a b)
                  '[a 1 b (+ a 1)])
   => '(clojure.core/binding [a 1]
         (clojure.core/binding [b (+ a 1)]
           (+ a b)))"
  {:added "3.0"}
  ([form bindings]
   (reduce (fn [form pair]
             `(binding [~@pair]
                ~form))
           form
           (reverse (partition 2 bindings)))))

(defn fact-use
  "setup form for use (with component)"
  {:added "3.0"}
  ([vsym ks]
   (let [comp (rt/get-global (h/ns-sym) :component vsym)]
     (->> (vecify (get-in comp ks))
          (map (fn [f]
                 `(~(or f `identity) ~vsym)))))))

(defn fact-use-setup-eval
  "setup form form eval
 
   (fact-use-setup-eval '*serv*)
   => '(list :database)"
  {:added "3.0"}
  ([vsym]
   (let [{:keys [create]}  (rt/get-global (h/ns-sym) :component vsym)]
     create)))

(defn fact-use-setup
  "setup form for 'use'
 
   (fact-use-setup '*serv*)
   => '(start (list :database))"
  {:added "3.0"}
  ([vsym]
   (let [{:keys [create setup]} (rt/get-global (h/ns-sym) :component vsym)]
     `(~(if setup setup `identity) ~create))))

(defn fact-use-teardown
  "teardown form for 'use'
 
   (fact-use-teardown '*serv*)
   => '(clojure.core/identity *serv*)"
  {:added "3.0"}
  ([vsym]
   (let [{:keys [teardown]} (rt/get-global (h/ns-sym) :component vsym)]
     `(~(if teardown teardown `identity) ~vsym))))

(defn fact-let-defs
  "create def forms for symbols
 
   (fact-let-defs '{:let [a 1 b 2]})
   => '[(def a 1) (def b 2)]"
  {:added "3.0"}
  ([m]
   (map (fn [[id value]]
          (list 'def (with-meta id {:dynamic true})
                value))
        (concat (map (juxt identity fact-use-setup-eval) (:use m))
                (partition 2 (:let m))))))

(defn fact-let-declare
  "creates let declarations
 
   (fact-let-declare '{:let [a 1 b 2 c 3]
                       :use [*serv*]})
   => '[(def a nil) (def b nil) (def c nil) (def *serv* nil)]"
  {:added "3.0"}
  ([m]
   (->> (concat (take-nth 2 (:let m))
                (:use m))
        (map #(vary-meta % assoc :dynamic true))
        (mapv #(list 'def % nil)))))

(defn fact-declare
  "creates a declare hook"
  {:added "3.0"}
  ([m]
   `(fn []
      ~@(fact-let-declare m))))

(defn fact-setup
  "creates a setup hook"
  {:added "3.0"}
  ([m]
   `(fn []
      ~@(fact-let-defs m)
      ~@(vecify (:setup m)))))

(defn fact-teardown
  "creates a teardown hook"
  {:added "3.0"}
  ([m]
   `(fn []
      ~@(vecify (:teardown m))
      ~@(map fact-use-teardown (reverse (:use m))))))

(defn fact-wrap-replace
  "creates a replace wrapper
 
   (fact-wrap-replace '{:use [*serv*]
                        :let [a 1 b 2]
                        :replace {+ -}})"
  {:added "3.0"}
  ([m]
   (let [replace (->> (concat (:let m))
                      (apply hash-map)
                      (merge (:replace m)))]
     `(fn [~'thunk]
        (fn []
          (binding [rt/*eval-replace* (quote ~replace)]
            (~'thunk)))))))

(defn fact-wrap-ceremony
  "creates the setup/teardown wrapper
 
   (fact-wrap-ceremony '{:setup [(prn 1 2 3)]
                         :teardown (prn \"goodbye\")})"
  {:added "3.0"}
  ([{:keys [setup teardown guard] :as m}]
   `(fn [~'thunk]
      (fn []
        (let [~'_ ~(if setup setup)
              ~'out (~'thunk)
              ~'_ ~(if teardown teardown)
              ~'_ (do ~@(map fact-use-teardown (reverse (:use m))))]
          ~'out)))))

(defn fact-wrap-bindings
  "creates a wrapper for bindings
 
   (fact-wrap-bindings '{:let [a 1 b (+ a 1)]})"
  {:added "3.0"}
  ([m]
   `(fn [~'thunk]
      (fn []
        ~(wrap-bindings `(~'thunk)
                        (concat (mapcat (juxt identity fact-use-setup) (:use m))
                                (:let m)))))))

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
                                               ~@(mapcat #(fact-use % [:check :before]) (:use m))
                                               ~@(vecify before))
                                     :after  (fn []
                                               ~@(vecify after)
                                               ~@(mapcat #(fact-use % [:check :after]) (:use m)))}]
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
