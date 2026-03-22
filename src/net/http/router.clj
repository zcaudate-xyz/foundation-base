(ns net.http.router
  (:require [clojure.string]
            [std.lib.bin :as bin]
            [std.lib.env :as env]))

(defn compare-masks [as bs]
  (let [a (first as)
        b (first bs)]
    (cond
      (= nil as bs)  0
      (= nil as)    -1
      (= nil bs)     1
      (= "**" a b)  (recur (next as) (next bs))
      (= "**" a)     1
      (= "**" b)    -1
      (= "*" a b)   (recur (next as) (next bs))
      (= "*" a)      1
      (= "*" b)     -1
      :else         (recur (next as) (next bs)))))

(defn split-path
  [s]
  (let [[_ method path] (re-matches #"(?:([a-zA-Z]+)\s+)?(.*)" s)]
    (->> (clojure.string/split path #"/+")
      (cons method)
      (remove clojure.string/blank?)
      (map clojure.string/trim)
      vec)))

(defn make-matcher
  "Given set of routes, builds matcher structure. See `router`"
  [routes]
  (->> routes
    (map (fn [[mask v]] [(split-path mask) v]))
    (sort-by first compare-masks)))

(defn match-path [mask path]
  (loop [mask   mask
         path   path
         params []]
    (let [m (first mask)
          p (first path)]
      (cond
        (= "**" m)        (if path
                            (conj params (clojure.string/join "/" path))
                            params)
        (= nil mask path) params
        (= nil mask)      nil
        (= nil path)      nil
        (= "*" m)         (recur (next mask) (next path) (conj params p))
        (= m p)           (recur (next mask) (next path) params)))))

(defn match-impl [matcher path]
  (reduce
    (fn [_ [mask v]]
      (when-some [params (match-path mask path)]
        (reduced [v params])))
    nil matcher))

(defn match
  [matcher path]
  (match-impl matcher (split-path path)))

(defn router
  [routes]
  (let [matcher (make-matcher routes)]
    (fn [req]
      (let [{:keys [request-method uri]} req
            path (cons
                   (clojure.string/upper-case (name request-method))
                   (remove clojure.string/blank? (clojure.string/split uri #"/+")))
            res  (when-some [[handler params] (match-impl matcher path)]
                   (handler (assoc req :path-params params)))]
        (cond (string? res)
              {:status 200
               :headers {"Content-Type" "application/json"}
               :body res}

              :else res)))))

;;
;; resource
;;

(defn serve-resource [uri public-path]
  (let [res (env/sys:resource
             (str public-path uri))]
    (if res
      (let [content-type (cond
                           (clojure.string/ends-with? uri ".html") "text/html"
                           (clojure.string/ends-with? uri ".css") "text/css"
                           (clojure.string/ends-with? uri ".js") "application/javascript"
                           (clojure.string/ends-with? uri ".json") "application/json"
                           :else "application/octet-stream")]
        {:status 200
         :headers {"Content-Type" content-type}
         :body (bin/input-stream res)})
      nil)))
