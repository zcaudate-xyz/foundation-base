(ns code.test.mock)

(def ^:dynamic *calls* nil)

(defn record-call [var-name args]
  (when *calls*
    (swap! *calls* update var-name (fnil conj []) args)))

(defn return [val]
  (fn [& _] val))

(defn verify-called
  "verifies that a function was called

   (verify-called my-fn)
   => true"
  {:added "3.0"}
  ([var-sym]
   (verify-called var-sym nil))
  ([var-sym args]
   (if-let [calls (get @*calls* var-sym)]
     (if args
       (boolean (some #(= args %) calls))
       (boolean (seq calls)))
     false)))

(defn verify-call-count
  "verifies the number of calls

   (verify-call-count my-fn 2)
   => true"
  {:added "3.0"}
  ([var-sym n]
   (let [calls (get @*calls* var-sym)]
     (= n (count calls)))))

(defmacro with
  "mocks functions and records calls

   (mock/with [my-fn (mock/return 10)]
     (my-fn 1))
   => 10"
  {:added "3.0"}
  ([bindings & body]
   (let [pairs (partition 2 bindings)
         vars (map first pairs)
         vals (map second pairs)]
     `(binding [*calls* (atom {})]
        (with-redefs [~@(interleave vars
                                    (map (fn [v val]
                                           `(let [orig# ~val
                                                  shim# (if (fn? orig#)
                                                          orig#
                                                          (constantly orig#))]
                                              (fn [& args#]
                                                (record-call (quote ~v) args#)
                                                (apply shim# args#))))
                                         vars vals))]
          ~@body)))))
