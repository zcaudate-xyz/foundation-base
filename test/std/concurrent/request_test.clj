(ns std.concurrent.request-test
  (:use code.test)
  (:require [std.protocol.request :as protocol.request]
            [std.concurrent.request :refer :all]
            [std.concurrent :as cc]
            [std.lib :as h :refer [defimpl]]))

(def eval-fn (comp eval read-string pr-str))

(defn eval-request-single
  ([{:keys [state]} {:keys [type form] :as input} opts]
   (case type
     :eval           (let [form (h/swap-return! state
                                                (fn [{:keys [transact] :as m}]
                                                  (if transact
                                                    [:transact/queued (update m :inputs conj input)]
                                                    [form m])))]
                       (eval-fn form))
     :transact/start (h/swap-return! state
                                     (fn [{:keys [inputs transact] :as m}]
                                       [:transact/start
                                        (if transact
                                          (throw (ex-info "Already in Transaction" {:inputs inputs}))
                                          (assoc m :transact true))]))
     :transact/end   (let [inputs (h/swap-return! state
                                                  (fn [{:keys [inputs]}]
                                                    [inputs {:inputs [] :transact false}]))]
                       (mapv (comp eval-fn :form) inputs)))))

(defn eval-request-bulk
  ([client inputs _]
   (mapv #(eval-request-single client % {}) inputs)))

(def eval-process-single (fn ([_ data _] data)))

(def eval-process-bulk   (fn ([_ _ data _] data)))

(defn eval-transact-combine
  ([client inputs]
   {:type :eval
    :form (mapv :form inputs)}))

(defimpl EvalClient [state]
  :prefix "eval-"
  :protocols [std.protocol.request/IRequest
              protocol.request/IRequestTransact
              :body {-transact-start   {:type :transact/start}
                     -transact-end     {:type :transact/end}}])

(defn eval-client []
  (EvalClient. (atom {:inputs [] :transact false})))


^{:refer std.concurrent.request/req.more :added "3.0" :adopt true
  :setup [(def |client| (eval-client))]}
(fact "execute command on single, bulk and transact calls"

  (req |client| {:type :eval :form 1}
       {:post [inc inc]})
  => 3

  (bulk |client|
        (fn []
          (req |client| {:type :eval :form 1}
               {:post [inc inc]})))
  => [3]

  (bulk |client|
        (fn []
          (req |client| {:type :eval :form 1}
               {:pre [#(update % :form inc)]
                :post [inc]})))
  => [3]

  (bulk |client|
        (fn []
          (req |client| {:type :eval :form 1}
               {:pre [#(update % :form inc)]
                :post  [inc]
                :chain [inc]})))
  => [4]

  (->> (transact |client|
                 (fn []
                   (req |client| {:type :eval :form 1}
                        {:pre   [#(update % :form inc)]
                         :post  [inc]
                         :chain [inc]})))
       (map deref))
  => [4])

^{:refer std.concurrent.request/req.time :added "3.0" :adopt true
  :setup [(def |client| (eval-client))]}
(fact "execute command on single, bulk and transact calls"

  ;;
  ;; Manual
  ;;
  (let [state (atom {})
        _ (req |client| {:type :eval :form 1}
               {:pre  [(fn [x]
                         (swap! state assoc :start (h/time-ns))
                         x)]
                :post [(fn [x]
                         (swap! state assoc :end (h/time-ns))
                         x)]})
        {:keys [start end]} @state]
    (h/format-ns (- end start)))
  => string?

  ;;
  ;; With measure key
  ;;
  (def -p- (promise))
  (req |client| {:type :eval :form 1}
       {:measure #(deliver -p- %)})

  @-p-
  ;; {:output 1, :start 1600748490664158898, :end 1600748490664239782}
  => (contains {:start number?
                :end number?
                :output 1}))

^{:refer std.concurrent.request/req.fn :added "3.0" :adopt true}
(fact "functions"

  (req + [1 2 3 4])
  => 10

  @(req + [1 2 3 4] {:async true})
  => 10)

^{:refer std.concurrent.request/req:bulk.debug :added "3.0" :adopt true
  :style/indent 1}
(fact "creates a bulk request"
  ^:hidden

  (-> (req:bulk [+ {:debug true}]
                (req + [1 2 3] {:debug true})
                (req + [1 2 3] {:debug true})
                (req - [4 5 6] {:debug true}))
      (h/with-out-str))
  => string?)

^{:refer std.concurrent.request/bulk-context :added "3.0"}
(fact "creates a bulk context" ^:hidden

  (bulk-context)
  => map?)

^{:refer std.concurrent.request/transact-context :added "3.0"}
(fact "creates a transact context" ^:hidden

  (transact-context)
  => h/atom?)

^{:refer std.concurrent.request/req:opts-clean :added "3.0"}
(fact "clean opts for processing inputs" ^:hidden

  (req:opts-clean {})
  => {})

^{:refer std.concurrent.request/req:opts :added "3.0"}
(fact "clean opts for processing inputs" ^:hidden

  (req:opts {} {})
  => {})

^{:refer std.concurrent.request/opts:timer :added "3.0"}
(fact "creates the timer"

  (opts:timer prn)
  => h/future?)

^{:refer std.concurrent.request/opts:wrap-measure :added "3.0"}
(fact "creates the measure opts"

  (opts:wrap-measure {} prn)
  => (contains-in {:pre [fn?], :chain [fn?]}))

^{:refer std.concurrent.request/measure:debug :added "3.0"}
(fact "creates a debug measure"

  (measure:debug {})
  => (contains {:start fn? :stop fn?}))

^{:refer std.concurrent.request/req:opts-init :added "3.0"}
(fact "initialise request opts")

^{:refer std.concurrent.request/req:return :added "3.0"}
(fact "returns the output"

  (req:return 1 false) ^:hidden
  => 1

  (req:return (h/completed 1)
              false)
  => 1

  (req:return (h/completed 1) true)
  => h/future?)

^{:refer std.concurrent.request/req:single-prep :added "3.0"}
(fact "prepares `req:single` options"

  (req:single-prep {:async true}) ^:hidden
  => map?

  (req:single-prep {:async true :transacted (h/incomplete)})
  => map?)

^{:refer std.concurrent.request/req:single-complete :added "3.0"}
(fact "completes the `req:single` call" ^:hidden

  (req:single-complete (req:single-prep {})
                       1)
  => 1

  @(req:single-complete (req:single-prep {:async true})
                        1)
  => 1)

^{:refer std.concurrent.request/req:single :added "3.0"
  :setup [(def |client| (eval-client))]}
(fact "creates a single request call" ^:hidden

  (req:single |client| {:type :eval :form '(+ 1 2 3 4)})
  => 10

  @(req:single |client| {:type :eval :form '(+ 1 2 3 4)}
               {:async true})
  => 10

  (req:single |client| {:type :eval :form '(+ 1 2 3 4)}
              {:chain [inc inc]})
  => 12

  @(req:single |client| {:type :eval :form '(+ 1 2 3 4)}
               {:async true
                :chain [inc inc]})
  => 12)

^{:refer std.concurrent.request/req:unit :added "3.0"
  :setup [(def |client| (eval-client))]}
(fact "creates a single request call with `*bulk*` context" ^:hidden

  (binding [*bulk*    {|client| (bulk-context)}
            *current* {|client| (atom [])}]
    (req:unit |client| {:type :eval :form '(+ 1 2 3)})
    (-> (get *bulk* |client|)
        :cache deref :inputs))
  => {:type :eval :form '(+ 1 2 3)})

^{:refer std.concurrent.request/bulk:inputs :added "3.0"
  :setup [(def |client| (eval-client))] :style/indent 1}
(fact "capture all inputs on the client" ^:hidden

  (bulk:inputs |client|
               (fn []
                 (req:unit |client| {:type :eval :form '(+ 1 2 3)})
                 (req:unit |client| {:type :eval :form '(+ 4 5 6)})))
  => [{:type :eval, :form '(+ 1 2 3)}
      {:type :eval, :form '(+ 4 5 6)}])

^{:refer std.concurrent.request/bulk-collect :added "3.0"
  :setup [(def |client| (eval-client))] :style/indent 1}
(fact "collects bulk inputs" ^:hidden

  (-> (bulk-collect |client|
                    (fn []
                      (req:unit |client| {:type :eval :form '(+ 1 2 3)})
                      (req:unit |client| {:type :eval :form '(+ 4 5 6)}))
                    [])
      first :inputs)
  => [{:type :eval, :form '(+ 1 2 3)}
      {:type :eval, :form '(+ 4 5 6)}])

^{:refer std.concurrent.request/bulk-process :added "3.0"
  :setup [(def |client| (eval-client))]}
(fact "processes the client given bulk inputs"
  ^:hidden

  @(bulk-process |client|
                 {:inputs [{:type :eval, :form '(+ 1 2 3)}
                           {:type :eval, :form '(+ 4 5 6)}]
                  :received (h/incomplete)}
                 {})
  => [6 15])

^{:refer std.concurrent.request/bulk :added "3.0"
  :setup [(def |client| (eval-client))] :style/indent 1}
(fact "allows query inputs to be combined"
  ^:hidden

  (bulk |client|
        (fn []
          (req |client| {:type :eval, :form '(+ 1 2 3)})
          (req |client| {:type :eval, :form '(+ 4 5 6)})))
  => [6 15]

  (bulk |client|
        (fn []
          (req |client| {:type :eval, :form '(+ 1 2 3)})
          (req |client| {:type :eval, :form '(+ 4 5 6)}))
        {:chain [#(apply + %)]})
  => 21

  (bulk |client|
        (fn []
          (req |client| {:type :eval, :form 1} {:chain [inc]})
          (req |client| {:type :eval, :form 2} {:chain [inc]})))
  => [2 3]

  (bulk |client|
        (fn []
          (bulk |client|
                (fn []
                  (req |client| {:type :eval, :form 1} {:chain [inc]})
                  (req |client| {:type :eval, :form 2} {:chain [inc]})))
          (req |client| {:type :eval, :form 3} {:chain [inc]})
          (req |client| {:type :eval, :form 4} {:chain [inc]})))
  => [[2 3] 4 5]

  (bulk |client|
        (fn []
          (bulk |client|
                (fn []
                  (req |client| {:type :eval, :form 1} {:chain [inc]})
                  (req |client| {:type :eval, :form 2} {:chain [inc]}))
                {:chain [#(apply + %)]})
          (req |client| {:type :eval, :form 3} {:chain [inc]})
          (req |client| {:type :eval, :form 4} {:chain [inc]}))
        {:chain [#(apply + %)]})
  => 14)

^{:refer std.concurrent.request/transact :added "3.0"
  :setup [(def |client| (eval-client))] :style/indent 1}
(fact "enable transactions on client"

  (->> (transact |client|
                 (fn []
                   (req |client| {:type :eval :form 1}
                        {:chain [#(* 8 %)]})))
       (map deref)) ^:hidden
  => '(8)

  ;;
  ;; bulk in transact
  ;;

  (->> (transact |client|
                 (fn []
                   (bulk |client|
                         (fn []
                           (transact |client|
                                     (fn []
                                       (req |client|
                                            {:type :eval :form 1}
                                            {})))
                           (req |client|
                                {:type :eval :form 2}
                                {})))
                   (bulk |client|
                         (fn []
                           (req |client|
                                {:type :eval :form 3}
                                {})
                           (req |client|
                                {:type :eval :form 4}
                                {})))))
       (map deref))
  => '(1 2 3 4)

  ;;
  ;; Nesting bulk and transact
  ;;

  (bulk |client|
        (fn []
          (req |client| {:type :eval :form 1}
               {:chain [#(* 8 %)]})
          (transact |client|
                    (fn []
                      (req |client| {:type :eval :form 1}
                           {:chain [#(* 2 %)]})
                      (req |client| {:type :eval :form 1}
                           {:chain [#(* 2 %)]})))
          (transact |client|
                    (fn []
                      (bulk |client|
                            (fn []
                              (req |client| {:type :eval :form 2}
                                   {:chain [#(* 2 %)]})))))
          (transact |client|
                    (fn []
                      (req |client| {:type :eval :form 3}
                           {:chain [#(* 2 %)]})))))
  => [8
      :transact/start 2 2 [1 1]
      :transact/start [4] [2]
      :transact/start 6 [3]])

^{:refer std.concurrent.request/transact-prep :added "3.0"
  :setup [(def |client| (eval-client))]}
(fact "prepare req:opts given transaction context"

  (transact-prep |client| (transact-context)
                 {:type :eval, :form 1}
                 {:chain [inc]}) ^:hidden
  => map?)

^{:refer std.concurrent.request/req-fn :added "3.0"}
(fact "function for req")

^{:refer std.concurrent.request/bulk:transact :added "3.0"
  :style/indent 1
  :setup [(def |client| (eval-client))]}
(fact "creates a transaction within a bulk context"
  ^:hidden

  (bulk:transact |client|
                 (fn []
                   (req |client| {:type :eval :form 1}
                        {:chain [inc]})
                   (req |client| {:type :eval :form 1}
                        {:chain [#(* 2 %)]})))
  => [2 2])

^{:refer std.concurrent.request/req :added "3.0" :adopt true
  :setup [(def |client| (eval-client))]}
(fact "execute command on single, bulk and transact calls"

  (req |client| {:type :eval :form 1}) ^:hidden
  => 1

  (req |client| {:type :transact/start})
  => :transact/start

  (req |client| {:type :eval :form 1})
  => :transact/queued

  (req |client| {:type :eval :form 1})
  => :transact/queued

  (req |client| {:type :transact/end})
  => [1 1])

^{:refer std.concurrent.request/req:in :added "3.0"}
(fact "captures the command input"
  ^:hidden

  (req:in
   (req nil [1])
   (req nil [2])
   (req nil [3]))
  => [[1] [2] [3]])

^{:refer std.concurrent.request/req:bulk :added "3.0"
  :style/indent 1}
(fact "creates a bulk request"
  ^:hidden

  (req:bulk [+]
            (req + [1 2 3])
            (req + [1 2 3])
            (req - [4 5 6]))
  => [6 6])

^{:refer std.concurrent.request/req:transact :added "3.0"
  :setup [(def |client| (eval-client))] :style/indent 1}
(fact "creates a bulk transaction request"
  ^:hidden

  (-> (req:transact [|client| {:debug true}]
                    (req |client| {:type :eval :form 1} {:debug true})
                    (req |client| {:type :eval :form 2} {:debug true}))
      (h/with-out-str))
  => string?)

^{:refer std.concurrent.request/bulk:map :added "3.0"
  :setup [(def |client| (eval-client))] :style/indent 1}
(fact "map function across a client"
  ^:hidden

  (bulk:map |client|
            req-fn [{:type :eval :form 1}
                    {:type :eval :form 2}])
  => [1 2])

^{:refer std.concurrent.request/transact:map :added "3.0"
  :setup [(def |client| (eval-client))] :style/indent 1}
(fact "performs a transaction across the client"

  (transact:map |client|
                req-fn [{:type :eval :form 1}
                        {:type :eval :form 2}])

  => [1 2])
