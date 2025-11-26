(ns code.test.legacy.types
  (:require [std.math :as math]
            [code.test.base.runtime :as rt]
            [code.test.legacy.snippet :as snippet]
            [std.lib :as h :refer [defimpl]]
            [std.lib.future :as f]
            [std.lib.time :as time]))

(defonce ^:dynamic *eval-replace* nil)

(def +type+
  #{:core :template :bench :table :check})

(defmulti fact-invoke
  "invokes a fact object"
  {:added "3.0" :guard true}
  :type)

(defn fact-display-info
  "displays a fact"
  {:added "4.0"}
  ([m]
   (dissoc m :path :eval :full :code :wrap
                     :function :setup :let :teardown :use)))

(defn fact-display
  "displays a fact"
  {:added "3.0"}
  ([m]
   (str (->> (fact-display-info m)
             (h/filter-vals identity)))))

(defn- fact-string
  [m]
  (str "#fact " (fact-display m)))

(defimpl Fact [type id ns path refer desc column line source global]
  :type defrecord
  :invoke [fact-invoke 1]
  :string fact-string
  :final true)

(defn fact?
  "checks if object is a fact"
  {:added "3.0"}
  ([obj]
   (instance? Fact obj)))

(defmethod fact-invoke :template
  [_])

(defmethod fact-invoke :core
  ([{:keys [wrap function guard timeout] :as m}]
   (let [{:keys [bindings ceremony check replace]} wrap
         execution-fn (-> function :thunk ceremony check bindings replace)
         result (if timeout
                  (let [fut (f/future:call (bound-fn [] (execution-fn)))]
                    (try
                      (-> fut
                          (f/future:timeout timeout)
                          (f/future:value))
                      (catch java.util.concurrent.ExecutionException e
                        (let [cause (.getCause e)]
                          (if (instance? java.util.concurrent.TimeoutException cause)
                            (do
                              (f/future:cancel fut)
                              (let [ex (ex-info "Test timed out" {:timeout timeout} cause)
                                    meta (select-keys m [:line :column :path :ns :refer :desc])
                                    err-result {:status :exception
                                                :data ex
                                                :form :timeout
                                                :from :evaluate
                                                :meta meta}]
                                (h/signal {:id rt/*run-id*
                                           :test :fact
                                           :meta meta
                                           :results [err-result]})
                                false))
                            (throw cause))))))
                  (execution-fn))]
     (if (and guard (not result))
       (h/error "Guard failed" (fact-display-info m)))
     result)))

(defmethod fact-invoke :derived
  ([{:keys [wrap function] :as m}]
   (let [{:keys [bindings check ceremony replace]} wrap]
     (binding [rt/*eval-meta* (select-keys m [:line :column :path :ns :refer :desc])]
       ((-> function :thunk ceremony check bindings replace))))))

(defmethod fact-invoke :table
  ([{:keys [wrap function header inputs] :as m}]
   (let [{:keys [bindings ceremony]} wrap
         {:keys [thunk]} function
         table-fn (fn []
                    (mapv (fn [input]
                            (let [nlet (snippet/replace-bindings (:let m)
                                                                 (interleave header input))
                                  bind-fn (eval (snippet/fact-wrap-bindings {:let nlet}))]
                              (binding [rt/*eval-meta* (-> (select-keys m [:path :ns :refer :desc])
                                                           (merge (clojure.core/meta input)))
                                        *eval-replace* (apply hash-map nlet)]
                                ((bind-fn thunk)))))
                          inputs))]
     ((-> table-fn ceremony bindings)))))

(def +stats+
  [:mean :max :min :range :stddev :median :skew])

(defn bench-single
  "runs a benchmark on a single function"
  {:added "3.0"}
  ([{:keys [create-fn times batch aggregates unit gc]
     :or {times 1
          unit :ns
          batch 1
          create-fn (fn [function _] function)
          gc false}}
    function]
   (let [thunk-fn (fn [] (dotimes [i batch]
                           (function)))]
     (h/->> (for [i (range times)]
              (binding [time/*no-gc* (not gc)]
                (case unit
                  :ms (h/bench-ms (thunk-fn))
                  :ns (h/bench-ns (thunk-fn)))))
            (map #(double (/ % batch)))
            (cond-> %
              (= 1 times) (h/-> first
                                (vector unit)
                                (hash-map :result % :times 1))
              (not= 1 times) (-> (math/aggregates (or aggregates +stats+))
                                 (merge {:times times :batch batch})))))))

(defmethod fact-invoke :bench
  ([{:keys [wrap function params inputs] :as m}]
   (let [inputs (if (empty? inputs)
                  [[:default []]]
                  inputs)
         {:keys [ceremony]} wrap
         {:keys [slim]} function
         bench-fn (fn []
                    (bench-single params slim))]
     (mapv (fn [[tag bindings]]
             (let [nlet (snippet/replace-bindings (:let m) bindings)
                   wrap-bind (eval (snippet/fact-wrap-bindings {:let nlet}))]
               [tag ((-> bench-fn ceremony wrap-bind))]))
           inputs))))

(defmethod fact-invoke :check
  ([{:keys [wrap function inputs] :as m}]
   (let [{:keys [ceremony check bindings]} wrap
         bsyms  (map (juxt (comp first)
                           (comp symbol #(str "i-" %) first)) (partition 2 inputs))
         inputs (mapcat (fn [[sym values] bsyms] [(second bsyms) values])
                        (partition 2 inputs)
                        bsyms)
         {:keys [thunk]} function
         check-form `(fn [~'thunk]
                       (fn []
                         (for [~@inputs]
                           (binding [~@(flatten bsyms)
                                     *eval-replace* ~(h/map-keys (fn [s]
                                                                      (list 'quote s))
                                                                    (into {} bsyms))]
                             (~'thunk)))))
         check-fn (eval check-form)]
     ((-> thunk check-fn ceremony check bindings)))))

(comment

  {:<>        #{:id :setup :teardown :let}
   :base      {:fn   #{:setup :teardown :thunk :slim}
               :wrap #{:ceremony :bindings}
               :body #{:full :bare}}
   :template  [:base]
   :derived   [:->source
               {:params #{:let :replace}}]
   :table     [:->source
               {:params #{:let :replace}
                :cases  []}]
   :check     [:->source
               {:params #{:let :replace}
                :inputs []}]
   :bench     [:->source
               {;; if id, save history to namespace
                ;; if no id, warn
                :params #{:runs :batch :gc :rerun}
                :inputs [[:map     {:let [x 1 y 2] :replace []}]
                         [:pmap    [x 1 y 2]]
                         [:bulk-5  [x 1 y 2]]
                         [:bulk-10 [x 1 y 2]]]}]})
