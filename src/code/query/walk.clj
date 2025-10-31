(ns code.query.walk
  (:require [code.edit :as nav]
            [std.lib.zip :as zip]))

(defn wrap-meta
  "allows matchwalk to handle meta tags"
  {:added "3.0"}
  ([walk-fn]
   (fn [nav matchers f recur-fn opts]
     (if (= :meta (nav/tag nav))
       (let [nloc (nav/up (walk-fn (-> nav nav/down nav/right) matchers f recur-fn opts))]
         (if (nav/right nloc)
           (walk-fn (nav/right nloc) matchers f recur-fn opts)
           nloc))
       (walk-fn nav matchers f recur-fn opts)))))

(defn wrap-suppress
  "allows matchwalk to handle exceptions"
  {:added "3.0"}
  ([walk-fn]
   (fn [nav matchers f recur-fn opts]
     (try (walk-fn nav matchers f recur-fn opts)
          (catch Exception t
            nav)))))

(defn matchwalk
  "match every entry within a form
 
   (-> (matchwalk (nav/parse-string \"(+ (+ (+ 8 9)))\")
                  [(match/compile-matcher '+)]
                  (fn [nav]
                    (-> nav nav/down (nav/replace '-) nav/up)))
       nav/value)
   => '(- (- (- 8 9)))"
  {:added "3.0"}
  ([nav matchers f]
   (matchwalk nav matchers f {}))
  ([nav matchers f {:keys [suppress] :as opts}]
   (let [matchwalk-fn (cond-> zip/matchwalk
                        suppress wrap-suppress
                        :then wrap-meta)]
     (matchwalk-fn nav matchers f matchwalk-fn (assoc opts
                                                      :move-right nav/right
                                                      :can-move-right? nav/right)))))

(defn levelwalk
  "only match the form at the top level
   (-> (levelwalk (nav/parse-string \"(+ (+ (+ 8 9)))\")
                  [(match/compile-matcher '+)
                   (match/compile-matcher '+)]
                  (fn [nav]
                    (-> nav nav/down (nav/replace '-) nav/up)))
       nav/value)
   => '(- (+ (+ 8 9)))"
  {:added "3.0"}
  ([nav [matcher] f]
   (levelwalk nav [matcher] f {}))
  ([nav [matcher] f {:keys [debug] :as opts}]
   (let [levelwalk-fn (cond-> zip/levelwalk
                        (not debug) wrap-suppress
                        :then wrap-meta)]
     (levelwalk-fn nav [matcher] f levelwalk-fn (assoc opts
                                                       :move-right nav/right
                                                       :can-move-right? nav/right)))))
