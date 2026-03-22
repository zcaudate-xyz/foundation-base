(ns lib.redis.impl.generator
  (:require [lib.redis.impl.common :as common]
            [lib.redis.impl.reference :as ref]
            [net.resp.wire :as wire]
            [std.concurrent :as cc]
            [std.lib.foundation]
            [std.lib.invoke :refer [definvoke]]
            [std.string.case]
            [std.string.common]
            [std.string.plural :as plural]))

(def ^:dynamic *command-id* nil)

;;
;; in:macros
;;

(defn expand-process
  "expands the argument for the `:process` keys"
  {:added "3.0"}
  ([{:keys [process type multiple] :as arg}]
   (cond process process

         (vector? type)
         (->> (map (fn [m]
                     (if (keyword? m)
                       (hash-map :type m)
                       m))
                   type)
              (mapv expand-process))

         (not multiple)
         (case type
           :key  `common/process:key
           :data `common/process:data
           nil)

         multiple
         (case type
           :key  `common/process:key-multi
           :data `common/process:data-multi
           nil))))

(defn expand-prelim
  "expands the initial argument"
  {:added "3.0"}
  ([{:keys [type display sym name command enum multiple optional process] :as arg}]
   (let [type    (or type :custom)
         name    (if (string? name)
                   (-> (std.string.common/escape name {\/ "-" \: "-"})
                       (std.string.common/lower-case))
                   name)
         sym     (or sym
                     (cond-> (or name (clojure.core/name type))
                       multiple (plural/plural)
                       :then symbol))
         multiple   (or (boolean multiple)
                        (vector? type))
         process (expand-process arg)]
     {:type type
      :command command
      :name name
      :sym  sym
      :display display
      :optional optional
      :multiple multiple
      :process process
      :enum enum})))

(defn expand-enum
  "expands the enum structure"
  {:added "3.0"}
  ([{:keys [enum name command]}]
   (cond  command
          (let [cstr (std.string.case/spear-case command)]
            {:type :command
             :command command
             :sym (symbol cstr)
             :name cstr
             :values (set (map (comp keyword std.string.case/spear-case) enum))})

          (= 1 (count enum))
          (let [estr (std.string.case/spear-case (first enum))]
            {:type :flag :sym (symbol estr) :name estr})

          :else
          {:type :enum
           :sym (symbol name)
           :name name
           :values (set (map (comp keyword std.string.case/spear-case) enum))})))

(defn expand-argument
  "expands an argument"
  {:added "3.0"}
  ([arg]
   (let [{:keys [type name] :as m} (expand-prelim arg)]
     (case type
       :enum (merge m (expand-enum m))
       m))))

(defn command-redefs
  "creates redefinitions for let bindings"
  {:added "3.0"}
  ([{:keys [sym process]}]
   (cond (vector? process)
         [sym `(map std.lib.foundation/call ~sym ~process (repeat ~'opts))]

         (symbol? process)
         [sym `(~process ~sym ~'opts)]

         :else
         [])))

(defn command-step
  "creates a command step"
  {:added "3.0"}
  [{:keys [type sym name multiple command values]}]
  (case type
    :flag    [sym `(conj ~(std.string.common/upper-case name))]
    :enum    [sym `(conj (if (get ~values ~sym)
                           (std.string.common/upper-case (std.lib.foundation/strn ~sym))
                           (throw (ex-info "Invalid input" {:arg (quote ~sym)
                                                            :options ~values}))))]
    :command [sym `(conj ~command
                         (if (get ~values ~sym)
                           (std.string.common/upper-case (std.lib.foundation/strn ~sym))
                           (throw (ex-info "Invalid input" {:arg (quote ~sym)
                                                            :options ~values}))))]
    :optional [sym `(into ~sym)]
    [sym (cond multiple
               `(into ~sym)

               command
               `(conj ~command ~sym)

               :else
               `(conj ~sym))]))

(defn command-tmpl
  "creates a command function from data"
  {:added "3.0"}
  ([{:keys [id prefix arguments return multiple optionals]}]
   (let [fsym  (symbol (str "in:" (name id)))
         command  (if prefix
                    (vec prefix)
                    [(std.string.case/upper-sep-case (name id))])
         fargs (map :sym arguments)
         dargs (map #(or %1 %2)
                    (map :display arguments)
                    fargs)
         rargs (mapcat command-redefs arguments)
         cargs (map (comp second command-step) arguments)]
     `(defn ~fsym
        {:redis/return    (quote ~return)
         :redis/optionals ~optionals
         :redis/multiple ~multiple}
        ([~@fargs]
         (~fsym ~@fargs {}))
        ([~@dargs ~'opts]
         (let [~@rargs]
           (-> ~command ~@cargs)))))))

(defn command-type
  "normalizes a command type"
  {:added "3.0"}
  [v]
  (if (vector? v)
    (mapv command-type v)
    (keyword v)))

(defn optional-tmpl
  "creates the optional form from data"
  {:added "3.0"}
  [{:keys [id arguments]}]
  (let [fsym (symbol (str "optional:" (name id)))
        {:keys [display arguments]} (last arguments)]
    `(defn ~fsym
       ([~display ~'_]
        (cond-> []
          ~@(mapcat command-step arguments))))))

(defn collect-optional
  "collect all optional variables"
  {:added "3.0"}
  ([arguments optionals key]
   (let [optional? (fn [{:keys [optional multiple]}]
                     (and optional (not multiple)))
         fargs (remove optional? arguments)
         oargs (filter optional? arguments)
         oargs (when (not-empty oargs)
                 (vreset! optionals true)
                 (let [display   `{:keys ~(mapv :sym oargs) :as ~'optional}]
                   [{:parent key
                     :type :optional
                     :display display
                     :sym 'optional
                     :arguments oargs
                     :multiple true
                     :process (symbol (str "optional:" (name key)))}]))]
     (concat fargs oargs))))

(defn command-arguments
  "function for command arguments"
  {:added "3.0"}
  ([args key custom multiple optionals]
   (-> (map-indexed (fn [i arg]
                      (let [arg (-> arg
                                    (update :type command-type)
                                    (merge (get (:arguments custom) i))
                                    (expand-argument))
                            _ (if (:multiple arg)
                                (vreset! multiple true))]
                        arg))
                    args)
       (doall)
       (collect-optional optionals key))))

(defn command-parse
  "parse params for a given skeleton"
  {:added "3.0"}
  ([m]
   (command-parse m {}))
  ([{:keys [id flags] :as m} {:keys [replace] :as custom}]
   (let [optionals (volatile! nil)
         multiple  (volatile! nil)
         return    (if (:write flags)
                     :ack
                     :data)]
     (binding [*command-id* id]
       (std.lib.foundation/-> (select-keys m [:id :arguments :prefix])
             (assoc :return return)
             (cond-> %
               replace  (assoc :arguments (:arguments custom)))
             (update :arguments
                     #(command-arguments %
                                         id
                                         custom
                                         multiple
                                         optionals))
             (merge (dissoc custom :arguments))
             (assoc :optionals (boolean @optionals))
             (assoc :multiple (boolean @multiple)))))))

(defn command-params
  "create command params for form generation
 
   (command-params :ttl)
   => '{:id :ttl,
        :arguments ({:enum nil, :name \"key\",
                     :process lib.redis.impl.common/process:key,
                     :command nil, :type :key, :multiple false, :sym key,
                     :optional nil, :display nil}),
        :prefix (\"TTL\"), :return :data, :optionals false, :multiple false}
 
   (command-params [:set {}])"
  {:added "3.0"}
  ([input]
   (if (vector? input)
     (apply command-params input)
     (command-params input {})))
  ([key custom]
   (command-parse (ref/command key)
                  custom)))

(defn command-form
  "create the command form"
  {:added "3.0"}
  ([k]
   (command-form k {}))
  ([k custom]
   (-> (command-params k custom)
       (command-tmpl))))

(defn select-commands
  "select commands and create a list of params"
  {:added "3.0"}
  ([{:keys [group include exclude custom replace]}]
   (let [ks (cond include include

                  (vector? group)
                  (mapcat ref/command-groups group)

                  :else
                  (ref/command-groups group))]
     (->> ks
          (remove (set (concat exclude (keys replace))))
          (map (juxt identity (or custom {})))
          (concat (map (fn [[k v]]
                         [k (assoc v :replace true)])
                       replace))
          (map command-params)))))
