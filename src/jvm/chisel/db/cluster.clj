(ns jvm.chisel.db.cluster
  "Multi-query admission control for the chip: the control plane that turns the
   fixed pool of reusable operator blocks into a shared, multi-tenant resource.

   Where `jvm.chisel.db.schedule` answers \"can *this* plan run on *this*
   inventory, and where?\", this namespace answers the scheduler's real
   question — \"which of the *currently free* blocks does this query get, and
   what does everyone else keep?\".

   The model is the essay's \"allocate CPU cores, not synthesise hardware\"
   loop:

     query arrives ─▶ admit   (check free units, pin placement, record cost)
     query finishes ─▶ release (units return to the free pool)
     next query     ─▶ admit   (reuses the same physical blocks, no recompile)

   State is a plain persistent map — `cluster`, `admit`, `release` are pure
   functions, so a rejected admission allocates nothing (atomicity is
   structural) and an admissions queue could be threaded through an agent or
   atom without changes here.

   Placement within a query is first-fit: each node draws the lowest-indexed
   free unit of its kind. Preemption, defragmentation and runtime rebalancing
   are deliberately deferred — the seams (`:cost-fn` on `admit`, the
   `:allocations` map being plain data) are where they plug in later."
  (:require [clojure.set :as set]
            [jvm.chisel.db.schedule :as sched]))

(defn cluster
  "An empty chip control-plane state over a unit `inventory`
   (e.g. {:scan 2 :hash 1 :aggregate 1}). No units allocated."
  [inventory]
  {:inventory inventory :allocations {}})

(defn used-units
  "Set of physical unit ids currently pinned by admitted queries."
  [cl]
  (apply set/union #{} (map :units (vals (:allocations cl)))))

(defn free-counts
  "Remaining units per kind: `inventory` minus what admitted queries hold.
   Kinds with no inventory are absent; kinds fully free keep their total."
  [cl]
  (let [held (apply merge-with +
                    {}
                    (map (fn [alloc]
                           (frequencies (map :kind (:placement alloc))))
                         (vals (:allocations cl))))]
    (merge-with - (:inventory cl) held)))

(defn utilization
  "Per-kind fraction of units in use, as a double in [0,1]. Kinds with zero
   inventory report 0.0."
  [cl]
  (let [free (free-counts cl)]
    (into {}
          (map (fn [[kind total]]
                 [kind (if (pos? total)
                         (double (/ (- total (get free kind 0)) total))
                         0.0)])
               (:inventory cl)))))

(defn admit
  "Try to admit `plan` as `query-id` onto the cluster. Pure: returns
   {:ok? true  :cluster cl' :admission {...}} with the updated state and the
   admission record ({:query :placement :units :cost}), or
   {:ok? false :reason ...} with the cluster untouched. Refuses when:
     * `query-id` is already admitted,
     * the plan uses an operator the chip has no blocks for,
     * free units of any kind cannot cover the plan's demand.
   Unit choice is first-fit (lowest free index per kind), excluding units
   pinned by queries already admitted. `opts` may carry `:cost-fn`, the same
   learned-cost-model seam as `schedule/schedule`."
  ([cl query-id plan] (admit cl query-id plan {}))
  ([cl query-id plan {:keys [cost-fn]}]
   (let [nodes (sched/plan->nodes plan)]
     (cond
       (contains? (:allocations cl) query-id)
       {:ok? false :reason "query-id already admitted"}

       (not (every? #(sched/supported-ops (:op %)) nodes))
       {:ok? false :reason "unsupported operator in plan"}

       :else
       (let [need (sched/demands plan)
             free (free-counts cl)]
         (if-not (every? (fn [[kind n]] (<= n (get free kind 0))) need)
           {:ok? false :reason (str "insufficient free resources: need "
                                    need " free " free)}
           (let [[placement _]
                 (reduce
                  (fn [[acc held] [i node]]
                    (let [kind  (sched/op->kind (:op node))
                          pick  (first (remove #(contains? held (keyword (str (name kind) "-" %)))
                                               (range)))
                          unit  (keyword (str (name kind) "-" pick))]
                      [(conj acc {:stage i :op (:op node) :kind kind :unit unit})
                       (conj held unit)]))
                  [[] (used-units cl)]
                  (map-indexed vector nodes))
                 cost ((or cost-fn sched/estimate-cost) plan)
                 admission {:query     query-id
                            :placement placement
                            :units     (set (map :unit placement))
                            :cost      cost}]
             {:ok?       true
              :cluster   (assoc-in cl [:allocations query-id] admission)
              :admission admission})))))))

(defn release
  "Return `query-id`'s units to the free pool. No-op for an unknown id."
  [cl query-id]
  (update cl :allocations dissoc query-id))
