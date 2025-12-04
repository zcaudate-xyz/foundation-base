(ns code.test.base.runtime
  (:require [std.lib :as h]
            [std.string :as str]
            [code.test.base.context :as context]))

;; When a namespace is run:
;; - *eval* is set to false
;; - the namespace is cleared
;; - the file is reloaded (forms are compiled)
;; - all the loaded tests are then run

(defn purge-all
  "purges all facts from namespace"
  {:added "3.0"}
  ([]
   (purge-all (h/ns-sym)))
  ([ns]
   (swap! context/*registry* dissoc ns)
   ns))

(defn get-global
  "gets the global settings for namespace
 
   (get-global)"
  {:added "3.0"}
  ([]
   (get-global (h/ns-sym)))
  ([ns]
   (or context/*eval-global*
       (get-in @context/*registry* [ns :global])))
  ([ns k & more]
   (-> (get-global ns)
       (get-in (vec (cons k more))))))

(defn set-global
  "sets the global settings for namespace
 
   (set-global {:check {:setup '[(prn \"hello\")]}})
   => '{:check {:setup [(prn \"hello\")]}}"
  {:added "3.0"}
  ([m]
   (set-global (h/ns-sym) m))
  ([ns m]
   (-> (swap! context/*registry* assoc-in [ns :global] m)
       (get-in [ns :global]))))

(defn update-global
  "updates global data"
  {:added "3.0"}
  ([f]
   (update-global (h/ns-sym) f))
  ([ns f & args]
   (-> (apply swap! context/*registry* update-in [ns :global] f args)
       (get-in [ns :global]))))

(defn list-links
  "lists ns links"
  {:added "3.0"}
  ([] (list-links (h/ns-sym)))
  ([ns]
   (get-in @context/*registry* [ns :links])))

(defn clear-links
  "clear ns links"
  {:added "3.0"}
  ([] (clear-links (h/ns-sym)))
  ([ns]
   (-> (swap! context/*registry* assoc-in [ns :links] nil)
       (get-in [ns :links]))))

(defn add-link
  "add ns link"
  {:added "3.0"}
  ([link] (add-link (h/ns-sym) link))
  ([ns link]
   (-> (swap! context/*registry* update-in [ns :links] (fnil #(conj % link) #{}))
       (get-in [ns :links]))))

(defn remove-link
  "remove ms link"
  {:added "3.0"}
  ([link] (remove-link (h/ns-sym) link))
  ([ns link]
   (-> (swap! context/*registry* update-in [ns :links] disj link)
       (get-in [ns :links]))))

(defn all-facts
  "retrieves a list of all the facts in a namespace
 
   (keys (all-facts))"
  {:added "3.0"}
  ([]
   (all-facts (h/ns-sym)))
  ([ns]
   (get-in @context/*registry* [ns :facts])))

(defn list-facts
  "lists all facts in current namespace
 
   (first (list-facts))
   => 'test-code_test_base_runtime__purge_all"
  {:added "3.0"}
  ([]
   (list-facts (h/ns-sym)))
  ([ns]
   (->> (all-facts ns)
        (vals)
        (sort-by :line)
        (map :id))))

(defn purge-facts
  "purges all facts in the namespace (for reload)
 
   (purge-facts)
   (list-facts)
   => []"
  {:added "3.0"}
  ([]
   (purge-facts (h/ns-sym)))
  ([ns]
   (swap! context/*registry* update ns dissoc :facts :flags)
   ns))

(defn parse-args
  "helper function for variable args"
  {:added "3.0"}
  ([ns id arg more]
   (if (or (symbol? id)
           (nil? id))
     [ns id (cons arg more)]
     [(h/ns-sym) ns (cons id
                          (cons arg (rest more)))])))

(defn get-fact
  "gets a fact
 
   (get-fact (fsym) :refer)
   => 'code.test.base.runtime/purge-all"
  {:added "3.0"}
  ([id]
   (get-fact (h/ns-sym) id))
  ([ns id]
   (if (symbol? id)
     (get-in @context/*registry* [ns :facts id])
     (get-fact (h/ns-sym) ns id)))
  ([ns id k & more]
   (let [[ns id ks] (parse-args ns id k more)]
     (-> (get-fact ns id)
         (get-in ks)))))

(defn set-fact
  "sets the entire data on a fact"
  {:added "3.0"}
  ([id data]
   (set-fact (h/ns-sym) id data))
  ([ns id data]
   (swap! context/*registry* assoc-in [ns :facts id] data)
   [ns id]))

(defn set-in-fact
  "sets the property on a fact
 
   (set-in-fact (fsym) [:function :other] (fn []))"
  {:added "3.0"}
  ([id ks data]
   (set-in-fact (h/ns-sym) id ks data))
  ([ns id ks data]
   (swap! context/*registry* assoc-in (concat [ns :facts id] ks) data)
   [ns id]))

(defn get-flag
  "checks if the setup flag has been set
 
   (get-flag (fsym) :setup)"
  {:added "3.0"}
  ([id flag]
   (get-flag (h/ns-sym) id flag))
  ([ns id flag]
   (boolean (get-in @context/*registry* [ns :flags id flag]))))

(defn set-flag
  "sets the setup flag
 
   (set-flag (fsym) :setup true)"
  {:added "3.0"}
  ([id flag val]
   (set-flag (h/ns-sym) id flag val))
  ([ns id flag ^Boolean val]
   (swap! context/*registry* assoc-in [ns :flags id flag] val)
   [ns id]))

(defn update-fact
  "updates a fact given a function"
  {:added "3.0"}
  ([ns id f & args]
   (let [[ns id [f & args]] (parse-args ns id f args)]
     (apply swap! context/*registry* update-in [ns :facts id] f args)
     [ns id])))

(defn remove-fact
  "removes a fact from namespace"
  {:added "3.0"}
  ([id]
   (remove-fact (h/ns-sym) id))
  ([ns id]
   (swap! context/*registry* (fn [m]
                       (-> m
                           (update-in [ns :facts] dissoc id)
                           (update-in [ns :flags] dissoc id))))
   [ns id]))

(defn teardown-fact
  "runs the teardown hook
 
   (teardown-fact (:id (find-fact {:line (h/code-line)})))
   => 6"
  {:added "3.0"}
  ([id]
   (teardown-fact (h/ns-sym) id))
  ([ns id]
   (let [teardown-fn  (get-fact ns id :function :teardown)
         out    (h/explode (teardown-fn))
         _  (set-flag ns id :setup false)]
     out)))

(defn setup-fact
  "runs the setup hook
 
   (setup-fact (:id (find-fact {:line (h/code-line)})))
   => 6"
  {:added "3.0"}
  ([id]
   (setup-fact (h/ns-sym) id))
  ([ns id]
   (let [_         (if (get-flag ns id :setup)
                     (teardown-fact ns id))
         setup-fn  (get-fact ns id :function :setup)
         [out status]  (if setup-fn
                         [(setup-fn) true]
                         [nil false])]
     (if status (set-flag ns id :setup status))
     out)))

(defn exec-thunk
  "executes the fact thunk (only the check"
  {:added "3.0"}
  ([fpkg]
   ((-> fpkg :function :thunk))))

(defn exec-slim
  "executes the fact slim (only the body"
  {:added "3.0"}
  ([fpkg]
   ((-> fpkg :function :slim))))

(defn no-dots
  "removes dots and slash from the string"
  {:added "3.0"}
  ([^String s]
   (str/escape s {\. "_"
                  \/ "__"})))

(defn fact-id
  "returns an id from fact data
 
   (fact-id {:refer 'code.test.base.runtime/fact-id})
   => 'test-code_test_base_runtime__fact_id"
  {:added "3.0"}
  ([{:keys [id refer]}]
   (let [id    (or id
                   (if refer (symbol (str "test-" (munge (no-dots (str refer)))))))]
     id)))

(defn find-fact
  "the fact that is associated with a given line
 
   (:id (find-fact {:line (h/code-line)}))
   => 'test-code_test_base_runtime__find_fact"
  {:added "3.0"}
  ([meta]
   (find-fact (h/ns-sym) meta))
  ([ns {:keys [line source]}]
   (let [id (if source
              (cond (map? source)
                    (fact-id source)

                    :else source))]
     (if id
       (get-fact ns id)
       (let [sel (comp :line second)]
         (->> (all-facts ns)
              (vals)
              (sort-by :line)
              (filter #(-> % :type (= :core)))
              (reverse)
              (drop-while #(< line (:line %)))
              (first)))))))

(defn run-op
  "common runtime functions for easy access
 
   (run-op {:line (h/code-line)}
           :self)"
  {:added "3.0"}
  ([{:keys [line source] :as meta} op & args]
   (let [ns (h/ns-sym)
         {:keys [id] :as fpkg} (find-fact ns meta)]
     (case op
       :self     fpkg
       :id       id
       :setup    (setup-fact ns id)
       :setup?   (get-flag ns id :setup)
       :teardown (teardown-fact ns id)
       :remove   (remove-fact ns id)
       :get      (apply get-fact ns id args)
       :set      (set-fact ns id (first args))
       :set-in   (set-in-fact ns id (first args) (second args))
       :update   (apply update-fact ns id args)))))
