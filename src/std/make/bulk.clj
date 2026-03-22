(ns std.make.bulk
  (:require [clojure.set]
            [std.fs :as fs]
            [std.lib.collection]
            [std.lib.env]
            [std.lib.foundation]
            [std.lib.sort]
            [std.lib.time]
            [std.make.common :as common]
            [std.make.compile :as compile]
            [std.make.github :as github]
            [std.make.project :as project]
            [std.print.format.common :as format]
            [std.string.common]))

(defn make-bulk-get-keys
  "bulk get keys"
  {:added "4.0"}
  [changed actions]
  (let [changed-keys (set (keys (std.lib.collection/filter-vals identity changed)))
        bulk-order  (std.lib.sort/topological-sort
                     (std.lib.collection/map-vals (comp set :deps) actions))
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
        built    (std.lib.collection/map-entries
                  (fn [[key plan]]
                    (let [_       (std.lib.env/local :print
                                           (format/pad:right
                                            (str (std.lib.foundation/strn key)  " (" (:tag @(:instance plan)) ")")
                                            30))
                          [ms result]  (std.lib.env/meter-out (project/build-all plan))
                          changed (project/changed-files result)]
                      (std.lib.env/local :print
                               (if (not-empty changed)
                                 "UPDATE REQUIRED"
                                 "NO CHANGE")
                               " (" (std.lib.time/format-ms ms) ")\n")
                      (when changed
                        (doseq [file changed]
                          (std.lib.env/p file)))
                      [key [ms changed]]))
                  (if (vector? only)
                    (select-keys configs only)
                    configs))
        changed  (std.lib.collection/map-vals (comp not empty? second) built)
        changed  (cond (= refresh :all)
                       (std.lib.collection/map-vals std.lib.foundation/T actions)

                       (vector? refresh)
                       (merge changed
                              (std.lib.collection/map-juxt [identity std.lib.foundation/T] refresh))
                       
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
        _      (do (std.lib.env/p)
                   (std.lib.env/p "--------------------------------------------------------------------")
                   (std.lib.env/p "BUILD STARTED --" (std.string.common/upper-case name))
                   (std.lib.env/p "--------------------------------------------------------------------"))
        [built changed]    (make-bulk-build m)
        [bulking
         bulk-order] (make-bulk-get-keys changed actions)
        _        (do (std.lib.env/p)
                     (if (empty? bulking)
                       (std.lib.env/p "ALL UPDATED")
                       (std.lib.env/p "BUILDING" (std.string.common/join ", " (map std.lib.foundation/strn bulking)))))
        [ms-total bulked]
        (std.lib.env/meter-out
         (std.lib.collection/map-juxt
          [identity
           (fn [key]
             (Thread/sleep 50)
             (first
              (common/make-run-internal
               (or (get configs key)
                   (std.lib.foundation/error "CONFIGS NOT FOUND"
                            {:key key
                             :options (keys configs)}))
               (or (get-in actions [key :action])
                   (std.lib.foundation/error "ACTIONS NOT FOUND"
                            {:key key
                             :options (keys actions)})))))]
          bulking))
        _         (Thread/sleep 50)
        _         (do (std.lib.env/p "--------------------------------------------------------------------")
                      (std.lib.env/p "BUILD COMPLETE --" (std.string.common/upper-case name))
                      (std.lib.env/p "--------------------------------------------------------------------")
                      (doseq [k bulk-order]
                        (let [[t-b] (get built k)
                              [t-d] (get bulked k)]
                          (when (or t-b t-d)
                            (std.lib.env/p (format/pad:right (std.lib.foundation/strn k) 15)
                                 (format/pad:right (str "BUILD  " (std.lib.time/format-ms (or t-b 0)))
                                                   20)
                                 (if t-d
                                   (format/pad:right (str "BULK " (std.lib.time/format-ms (or t-d 0)))
                                                     20)
                                   "-")))))
                      (std.lib.env/p)
                      (std.lib.env/p "TOTAL" (std.lib.time/format-ms ms-total))
                      (std.lib.env/p))]
    {:built built
     :bulked bulked
     :total ms-total}))

(defn make-bulk-container-filter
  "TODO"
  {:added "4.0"}
  [configs containers]
  (let [containers (set containers)]
    (std.lib.collection/filter-vals (fn [{:keys [instance]}]
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
  (std.lib.collection/map-vals (fn [mcfg]
                (std.lib.env/p "--------------------------------------------------------------------")
                (std.lib.env/p "GITHUB INIT --" (:tag @(:instance mcfg)))
                (std.lib.env/p "--------------------------------------------------------------------")
                (project/build-all mcfg)
                (github/gh-dwim-init mcfg message))
              (sort-by first configs)))

(defn make-bulk-gh-push
  "make bulk push github"
  {:added "4.0"}
  [configs & [message]]
  (std.lib.collection/map-vals (fn [mcfg]
                (std.lib.env/p "--------------------------------------------------------------------")
                (std.lib.env/p "GITHUB PUSH --" (:tag @(:instance mcfg)))
                (std.lib.env/p "--------------------------------------------------------------------")
                (project/build-all mcfg)
                (github/gh-dwim-push mcfg message))
              (sort-by first configs)))
