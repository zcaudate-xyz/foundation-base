(ns std.make.bulk
  (:require [clojure.set]
            [clojure.string]
            [std.fs :as fs]
            [std.lib.collection :as collection]
            [std.lib.env :as env]
            [std.lib.foundation :as f]
            [std.lib.sort :as sort]
            [std.lib.time :as time]
            [std.make.common :as common]
            [std.make.compile :as compile]
            [std.make.github :as github]
            [std.make.project :as project]
            [std.print.format.common :as format]))

(defn make-bulk-get-keys
  "bulk get keys"
  {:added "4.0"}
  [changed actions]
  (let [changed-keys (set (keys (collection/filter-vals identity changed)))
        bulk-order  (sort/topological-sort
                     (collection/map-vals (comp set :deps) actions))
        bulk-keys   (reduce (fn [acc k]
                              (if (empty?
                                   (clojure.set/intersection
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
        built    (collection/map-entries
                  (fn [[key plan]]
                    (let [_       (env/local :print
                                           (format/pad:right
                                            (str (f/strn key)  " (" (:tag @(:instance plan)) ")")
                                            30))
                          [ms result]  (env/meter-out (project/build-all plan))
                          changed (project/changed-files result)]
                      (env/local :print
                               (if (not-empty changed)
                                 "UPDATE REQUIRED"
                                 "NO CHANGE")
                               " (" (time/format-ms ms) ")\n")
                      (when changed
                        (doseq [file changed]
                          (env/p file)))
                      [key [ms changed]]))
                  (if (vector? only)
                    (select-keys configs only)
                    configs))
        changed  (collection/map-vals (comp not empty? second) built)
        changed  (cond (= refresh :all)
                       (collection/map-vals f/T actions)

                       (vector? refresh)
                       (merge changed
                              (collection/map-juxt [identity f/T] refresh))
                       
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
        _      (do (env/p)
                   (env/p "--------------------------------------------------------------------")
                   (env/p "BUILD STARTED --" (clojure.string/upper-case name))
                   (env/p "--------------------------------------------------------------------"))
        [built changed]    (make-bulk-build m)
        [bulking
         bulk-order] (make-bulk-get-keys changed actions)
        _        (do (env/p)
                     (if (empty? bulking)
                       (env/p "ALL UPDATED")
                       (env/p "BUILDING" (clojure.string/join ", " (map f/strn bulking)))))
        [ms-total bulked]
        (env/meter-out
         (collection/map-juxt
          [identity
           (fn [key]
             (Thread/sleep 50)
             (first
              (common/make-run-internal
               (or (get configs key)
                   (f/error "CONFIGS NOT FOUND"
                            {:key key
                             :options (keys configs)}))
               (or (get-in actions [key :action])
                   (f/error "ACTIONS NOT FOUND"
                            {:key key
                             :options (keys actions)})))))]
          bulking))
        _         (Thread/sleep 50)
        _         (do (env/p "--------------------------------------------------------------------")
                      (env/p "BUILD COMPLETE --" (clojure.string/upper-case name))
                      (env/p "--------------------------------------------------------------------")
                      (doseq [k bulk-order]
                        (let [[t-b] (get built k)
                              [t-d] (get bulked k)]
                          (when (or t-b t-d)
                            (env/p (format/pad:right (f/strn k) 15)
                                 (format/pad:right (str "BUILD  " (time/format-ms (or t-b 0)))
                                                   20)
                                 (if t-d
                                   (format/pad:right (str "BULK " (time/format-ms (or t-d 0)))
                                                     20)
                                   "-")))))
                      (env/p)
                      (env/p "TOTAL" (time/format-ms ms-total))
                      (env/p))]
    {:built built
     :bulked bulked
     :total ms-total}))

(defn make-bulk-container-filter
  "TODO"
  {:added "4.0"}
  [configs containers]
  (let [containers (set containers)]
    (collection/filter-vals (fn [{:keys [instance]}]
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
  (collection/map-vals (fn [mcfg]
                (env/p "--------------------------------------------------------------------")
                (env/p "GITHUB INIT --" (:tag @(:instance mcfg)))
                (env/p "--------------------------------------------------------------------")
                (project/build-all mcfg)
                (github/gh-dwim-init mcfg message))
              (sort-by first configs)))

(defn make-bulk-gh-push
  "make bulk push github"
  {:added "4.0"}
  [configs & [message]]
  (collection/map-vals (fn [mcfg]
                (env/p "--------------------------------------------------------------------")
                (env/p "GITHUB PUSH --" (:tag @(:instance mcfg)))
                (env/p "--------------------------------------------------------------------")
                (project/build-all mcfg)
                (github/gh-dwim-push mcfg message))
              (sort-by first configs)))
