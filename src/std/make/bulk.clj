(ns std.make.bulk
  (:require [std.lib :as h]
            [std.string :as str]
            [std.print.format.common :as format]
            [std.make.common :as common]
            [std.make.compile :as compile]
            [std.make.project :as project]
            [std.make.github :as github]
            [std.fs :as fs]))

(defn make-bulk-get-keys
  "bulk get keys"
  {:added "4.0"}
  [changed actions]
  (let [changed-keys (set (keys (h/filter-vals identity changed)))
        bulk-order  (h/topological-sort
                     (h/map-vals (comp set :deps) actions))
        bulk-keys   (reduce (fn [acc k]
                              (if (empty?
                                   (h/intersection
                                    acc
                                    (or (get-in actions [k :deps])
                                        #{})))
                                acc
                                (conj acc k)))
                            changed-keys
                            bulk-order)]
    [(filter bulk-keys bulk-order)
     bulk-order]))

(defn make-bulk-build
  "build make bulk datastructure"
  {:added "4.0"}
  [m]
  (let [{:keys [name
                refresh
                configs
                only
                actions]} m
        built    (h/map-entries
                  (fn [[key plan]]
                    (let [_       (h/local :print
                                           (format/pad:right
                                            (str (h/strn key)  " (" (:tag @(:instance plan)) ")")
                                            30))
                          [ms result]  (h/meter-out (project/build-all plan))
                          changed (project/changed-files result)]
                      (h/local :print
                               (if (not-empty changed)
                                 "UPDATE REQUIRED"
                                 "NO CHANGE")
                               " (" (h/format-ms ms) ")\n")
                      (when changed
                        (doseq [file changed]
                          (h/p file)))
                      [key [ms changed]]))
                  (if (vector? only)
                    (select-keys configs only)
                    configs))
        changed  (h/map-vals (comp not empty? second) built)
        changed  (cond (= refresh :all)
                       (h/map-vals h/T actions)

                       (vector? refresh)
                       (merge changed
                              (h/map-juxt [identity h/T] refresh))
                       
                       :else
                       changed)]
    [built changed]))

(defn make-bulk
  "make bulk"
  {:added "4.0"}
  [m]
  (let [{:keys [name
                refresh
                configs
                only
                actions]} m
        _      (do (h/p)
                   (h/p "--------------------------------------------------------------------")
                   (h/p "BUILD STARTED --" (str/upper-case name))
                   (h/p "--------------------------------------------------------------------"))
        [built changed]    (make-bulk-build m)
        [bulking
         bulk-order] (make-bulk-get-keys changed actions)
        _        (do (h/p)
                     (if (empty? bulking)
                       (h/p "ALL UPDATED")
                       (h/p "BUILDING" (str/join ", " (map h/strn bulking)))))
        [ms-total bulked]
        (h/meter-out
         (h/map-juxt
          [identity
           (fn [key]
             (Thread/sleep 50)
             (first
              (common/make-run-internal
               (or (get configs key)
                   (h/error "CONFIGS NOT FOUND"
                            {:key key
                             :options (keys configs)}))
               (or (get-in actions [key :action])
                   (h/error "ACTIONS NOT FOUND"
                            {:key key
                             :options (keys actions)})))))]
          bulking))
        _         (Thread/sleep 50)
        _         (do (h/p "--------------------------------------------------------------------")
                      (h/p "BUILD COMPLETE --" (str/upper-case name))
                      (h/p "--------------------------------------------------------------------")
                      (doseq [k bulk-order]
                        (let [[t-b] (get built k)
                              [t-d] (get bulked k)]
                          (when (or t-b t-d)
                            (h/p (format/pad:right (h/strn k) 15)
                                 (format/pad:right (str "BUILD  " (h/format-ms (or t-b 0)))
                                                   20)
                                 (if t-d
                                   (format/pad:right (str "BULK " (h/format-ms (or t-d 0)))
                                                     20)
                                   "-")))))
                      (h/p)
                      (h/p "TOTAL" (h/format-ms ms-total))
                      (h/p))]
    {:built built
     :bulked bulked
     :total ms-total}))

(defn make-bulk-container-filter
  "TODO"
  {:added "4.0"}
  [configs containers]
  (let [containers (set containers)]
    (h/filter-vals (fn [{:keys [instance]}]
                     (let [container (get @instance :container)]
                       (and (not (nil? container))
                            (get containers container))))
                   configs)))

(defn make-bulk-container-build
  "TODO"
  {:added "4.0"}
  [configs & [containers opts]]
  (let [ks  (vec (keys
                  (if (not-empty containers)
                    (make-bulk-container-filter configs containers)
                    configs)))]
    (make-bulk (merge {:name    "CONTAINER BUILD"
                       :only    ks
                       :configs configs
                       :actions (zipmap ks (repeat {:action :container-build}))}
                      opts))))

(defn make-bulk-gh-init
  "make bulk init github"
  {:added "4.0"}
  [configs & [message]]
  (h/map-vals (fn [mcfg]
                (h/p "--------------------------------------------------------------------")
                (h/p "GITHUB INIT --" (:tag @(:instance mcfg)))
                (h/p "--------------------------------------------------------------------")
                (project/build-all mcfg)
                (github/gh-dwim-init mcfg message))
              (sort-by first configs)))

(defn make-bulk-gh-push
  "make bulk push github"
  {:added "4.0"}
  [configs & [message]]
  (h/map-vals (fn [mcfg]
                (h/p "--------------------------------------------------------------------")
                (h/p "GITHUB PUSH --" (:tag @(:instance mcfg)))
                (h/p "--------------------------------------------------------------------")
                (project/build-all mcfg)
                (github/gh-dwim-push mcfg message))
              (sort-by first configs)))
