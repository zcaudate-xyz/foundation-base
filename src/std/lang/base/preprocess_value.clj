(ns std.lang.base.preprocess-value
  (:require [std.lib.collection :as collection]))

(defn value-template-args
  "derives callable value args from op or template arglists"
  {:added "4.1"}
  ([template]
   (value-template-args nil template))
  ([arglists template]
   (if arglists
     (let [argv (-> arglists first)
           argv (if (vector? (first argv))
                  (first argv)
                  argv)]
       (vec argv))
     (let [arglists (-> template meta :arglists)
           argv     (-> arglists first)
           argv     (if (vector? (first argv))
                      (first argv)
                      argv)]
       (->> argv
            rest
            vec)))))

(defn value-standalone
  "returns the standalone expansion for a value-liftable reserved symbol"
  {:added "4.1"}
  [sym grammar]
  (let [{:keys [emit macro]
         template :value/template
         standalone :value/standalone
         op-spec :op-spec} (get-in grammar [:reserved sym])
        template (or template
                     (when (= :macro emit)
                       macro))
        self-return? (= :xt/self
                        (get-in op-spec [:type 2]))]
    (cond (or (collection/form? standalone)
              (symbol? standalone))
          standalone

          (and (= true standalone)
               template)
          (let [args (value-template-args (:arglists op-spec)
                                          template)]
            (if self-return?
              (let [self-arg (first args)]
                (list 'fn args
                      (template (apply list nil args))
                      (list 'return self-arg)))
              (list 'fn args
                    (list 'return
                          (template (apply list nil args))))))

          :else
          nil)))
