(ns std.lang.base.emit
  (:require [std.lang.base.emit-block :as block]
	    [std.lang.base.emit-common :as common]
  	    [std.lang.base.emit-fn :as fn]
  	    [std.lang.base.emit-helper :as helper]
  	    [std.lang.base.preprocess :as preprocess]
            [std.lang.base.emit-rewrite :as rewrite]
  	    [std.lang.base.emit-top-level :as top]
  	    [std.lang.base.grammar :as grammar]
  	    [std.lang.base.util :as ut]
  	    [std.lib.collection :as collection]
  	    [std.lib.env :as env]))

(defn default-grammar
  "returns the default grammar
 
   (emit/default-grammar)
   => map?"
  {:added "4.0"}
  [& [m]]
  (collection/merge-nested helper/+default+ m))

(def +option-keys+
  [:lang
   :entry
   :module
   :book
   :snapshot
   :layout
   :emit
   :std.lang/provenance])

(defn emit-main-loop
  "creates the raw emit
 
   (emit/emit-main-loop '(not (+ 1 2 3))
                       +grammar+
                       {})
   => \"!((+ 1 2 3))\""
  {:added "4.0"}
  ([form grammar mopts]
   (common/emit-common-loop form
                            grammar
                            mopts
                            top/+emit-lookup+
                            top/emit-form)))

(defn emit-main
  "creates the raw emit with loop
 
   (emit/emit-main '(not (+ 1 2 3))
                   +grammar+
                   {})
   => \"!(1 + 2 + 3)\""
  {:added "4.0"}
  ([form grammar mopts]
   (binding [common/*emit-fn* emit-main-loop]
     (emit-main-loop form grammar mopts))))

(defn emit
  "emits form to output string"
  {:added "4.0"}
  ([form grammar namespace mopts]
   (let [mopts (select-keys mopts +option-keys+)]
     (binding [*ns* (or (if namespace
                          (the-ns namespace))
                        *ns*)]
       (cond (:emit grammar)
             ((:emit grammar) form mopts)
             
             :else
             (emit-main form grammar mopts))))))

(defmacro with:emit
  "binds the top-level emit function to common/*emit-fn*
 
   (emit/with:emit
    (common/*emit-fn* '(not (+ 1 2 3))
                      +grammar+
                      {}))
   => \"!(1 + 2 + 3)\""
  {:added "4.0"}
  [& body]
  `(binding [common/*emit-fn* emit-main-loop]
     ~@body))

;;
;;
;;
;;

(def +test-grammar+
  (delay (grammar/grammar :test
           (grammar/to-reserved (grammar/build))
           helper/+default+)))

(defn prep-options
  "prepares the options for processing"
  {:added "4.0"}
  [meta]
  (let [{:keys [lang grammar book namespace snapshot]
         step :-} meta
        step (or step
                 (if (or (and lang snapshot)
                         book)
                   :staging
                   :input))
        namespace (or namespace
                      (env/ns-sym))
        book     (or (if (symbol? book)
                       @(resolve book)
                       book)
                     (get-in snapshot [lang :book]))
        grammar  (or (if (symbol? grammar)
                       @(resolve grammar)
                       grammar)
                     (if book (:grammar book))
                     @+test-grammar+)
        mopts (select-keys meta +option-keys+)]
    [step grammar book namespace mopts]))

(def +steps+
  [[:raw]
   [:input]
   [:staging]])

(defn prep-form
  "prepares the form"
  {:added "4.0"}
  [step form grammar book mopts]
  (let [input (preprocess/to-input form)]
    (case step
      :raw     [form]
      :input   [input]
      :staging (let [[staged deps deps-fragment deps-native]
                     (preprocess/to-staging input
                                            grammar
                                            (:modules book)
                                            mopts)
                     rewritten
                     (rewrite/rewrite-stage :staging
                                            staged
                                            grammar
                                            (assoc mopts :book book))]
                 [rewritten deps deps-fragment deps-native]))))
