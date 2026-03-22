(ns lib.redis.impl.reference
  (:require [std.json :as json]
            [std.lib.collection]
            [std.lib.env]
            [std.lib.invoke :refer [definvoke]]
            [std.string.case]
            [std.string.common]))

(def +main-path+
  "assets/lib.redis/commands.json")

(def +supplement-path+
  "assets/lib.redis/supplement.json")

(defn command-doc
  "converts an entry to the redis doc format
 
   (command-doc (:set (parse-commands)))
   => \"SET key value [EX seconds|PX milliseconds] [NX|XX] [KEEPTTL]\""
  {:added "3.0"}
  ([{:keys [id prefix arguments]}]
   (let [fmt (fn [a]
               (let [s (cond (:command a) (str (:command a) " "
                                               (if (= (:type a) "enum")
                                                 (apply str (interpose "|" (:enum a)))
                                                 (if (string? (:name a))
                                                   (:name a)
                                                   (apply str (interpose " " (:name a))))))
                             (= (:type a) "enum") (apply str (interpose "|" (:enum a)))
                             :else (:name a))
                     s (if (:multiple a) (str s " [" s " ..]") s)
                     s (if (:optional a) (str "[" s "]") s)]
                 s))]
     (str (std.string.common/join " " prefix) " "
          (apply str (interpose " " (map fmt arguments)))))))

(definvoke parse-main
  "parses file for main reference"
  {:added "3.0"}
  [:memoize]
  ([content]
   (let [struct (json/read content json/+keyword-case-mapper+)
         prep   (fn [{:keys [id] :as m}]
                  (-> m
                      (update :group (comp keyword std.string.case/spear-case))
                      (assoc  :summary (command-doc m))
                      (assoc  :prefix (->> (std.string.common/split (name id) #"-")
                                           (map std.string.common/upper-case)))))]
     (std.lib.collection/map-entries (fn [[id m]]
                      [id (prep (assoc m :id id))])
                    struct))))

(definvoke parse-supplements
  "parses file for supplement reference"
  {:added "3.0"}
  [:memoize]
  ([content]
   (let [struct (json/read content json/+keyword-case-mapper+)
         parse-fn (fn [[arity flags key-start key-end step xflags]]
                    {:arity (if (pos? arity)
                              [arity false]
                              [(dec (- arity)) true])
                     :key [key-start key-end step]
                     :flags (into (set (map keyword flags))
                                  (map (comp keyword #(subs % 1)) xflags))})]
     (->> struct
          (map (juxt (comp keyword std.string.common/lower-case first)
                     (comp parse-fn rest)))
          (into {})))))

(definvoke parse-commands
  "returns all commands
 
   (parse-commands)
   => map?"
  {:added "3.0"}
  [:memoize]
  ([]
   (parse-commands +main-path+ +supplement-path+))
  ([main-path supplement-path]
   (let [main (parse-main (std.lib.env/sys:resource-content main-path))
         supplement (parse-supplements (std.lib.env/sys:resource-content supplement-path))]
     (std.lib.collection/map-vals (fn [{:keys [id] :as m}]
                   (merge m (get supplement id)))
                 main))))

(defn command-list
  "returns all commands
 
   (command-list)
 
   (command-list :hash)"
  {:added "3.0"}
  ([]
   (sort (keys (parse-commands))))
  ([group]
   (sort (keys (std.lib.collection/filter-vals (comp #{group} :group) (parse-commands))))))

(defn command
  "gets the command info
 
   (command :hset)
   => map?"
  {:added "3.0"}
  ([k]
   (get (parse-commands) k)))

(definvoke command-groups
  "lists all command group types
 
   (command-groups)
   => (list :cluster :connection :generic :geo :hash
            :hyperloglog :list :pubsub :scripting
            :server :set :sorted-set :stream :string :transactions)"
  {:added "3.0"}
  [:memoize]
  ([]
   (sort (set (map :group (vals (parse-commands))))))
  ([group]
   (sort (keep (fn [[k v]]
                 (if (-> v :group (= group))
                   k))
               (parse-commands)))))
