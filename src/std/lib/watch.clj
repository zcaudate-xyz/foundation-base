(ns std.lib.watch
  (:require [std.lib.collection :as c]
            [std.lib.foundation :as h]
            [std.lib.function :as fn]
            [std.protocol.watch :as protocol.watch])
  (:refer-clojure :exclude [list remove set]))

(defn wrap-select
  "enables operating on a given key
 
   ((watch/wrap-select (fn [_ _ p n]
                         (+ p n))
                       :a)
    nil nil {:a 2} {:a 1})
   => 3"
  {:added "3.0"}
  ([f sel]
   (fn [& args]
     (let [[n p & more] (reverse args)
           pv    (get-in p (c/seqify sel))
           nv    (get-in n (c/seqify sel))]
       (apply f (->> more (cons pv) (cons nv) reverse))))))

(defn wrap-diff
  "only functions when the inputs are different
 
   ((watch/wrap-diff (fn [_ _ p n]
                       (+ p n)))
    nil nil 2 2)
   => 2
 
   ((watch/wrap-diff (fn [_ _ p n]
                       (+ p n)))
    nil nil 2 3)
   => 5"
  {:added "3.0"}
  ([f]
   (fn [& args]
     (let [[nv pv & more] (reverse args)]
       (cond (and (nil? pv) (nil? nv))
             nil

             (or (nil? pv) (nil? nv)
                 (not (= pv nv)))
             (apply f args)

             :else pv)))))

(defn wrap-mode
  "changes how the function is run, :sync (same thread) and :async (new thread)"
  {:added "3.0"}
  ([f mode]
   (fn [& args]
     (case mode
       :sync  (apply f args)
       :async (future (apply f args))))))

(defn wrap-suppress
  "runs the function but if errors, does not throw exception"
  {:added "3.0"}
  ([f]
   (fn [& args]
     (try
       (apply f args)
       (catch Throwable t)))))

(defn process-options
  "helper function for building a watch function
 
   (watch/process-options {:supress true
                           :diff true
                          :mode :async
                           :args 2}
                          (fn [_ _] 1))"
  {:added "3.0"}
  ([opts f]
   (let [f (if (:diff opts)
             (wrap-diff f)
             f)
         f (if-let [sel (:select opts)]
             (wrap-select f sel)
             f)
         f (if (:suppress opts)
             (wrap-suppress f)
             f)
         f (wrap-mode f (or (:mode opts) :sync))]
     f)))

(defn watch:add
  "Adds a watch function through the IWatch protocol"
  {:added "3.0"}
  ([obj f] (watch:add obj nil f nil))
  ([obj k f] (watch:add obj k f nil))
  ([obj k f opts]
   (protocol.watch/-add-watch obj k f opts)))

(defn watch:list
  "Lists watch functions through the IWatch protocol"
  {:added "3.0"}
  ([obj] (watch:list obj nil))
  ([obj opts] (protocol.watch/-list-watch obj opts)))

(defn watch:remove
  "Removes watch function through the IWatch protocol"
  {:added "3.0"}
  ([obj]   (watch:remove obj nil nil))
  ([obj k] (watch:remove obj k nil))
  ([obj k opts] (protocol.watch/-remove-watch obj k opts)))

(defn watch:clear
  "Clears all watches form the object"
  {:added "3.0"}
  ([obj] (watch:clear obj nil))
  ([obj opts]
   (let [watches (watch:list obj opts)]
     (doseq [k (keys watches)]
       (watch:remove obj k opts)))))

(defn watch:set
  "Sets a watch in the form of a map"
  {:added "3.0"}
  ([obj watches] (watch:set obj watches nil))
  ([obj watches opts]
   (doseq [[k f] watches]
     (watch:add obj k f opts))
   (watch:list obj opts)))

(defn watch:copy
  "Copies watches from one object to another"
  {:added "3.0"}
  ([to from] (watch:copy to from nil))
  ([to from opts]
   (let [watches (watch:list from opts)]
     (watch:set to watches opts))))

(extend-protocol protocol.watch/IWatch
  clojure.lang.IRef
  (-add-watch [obj k f opts]
    (add-watch obj k (process-options opts f))
    (.getWatches obj))

  (-list-watch [obj _]
    (.getWatches obj))

  (-remove-watch [obj k _]
    (remove-watch obj k)))
