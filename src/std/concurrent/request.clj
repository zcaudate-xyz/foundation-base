(ns std.concurrent.request
  (:require [std.lib.atom :as at]
            [std.lib.env :as env]
            [std.lib.foundation :as h]
            [std.lib.future :as f]
            [std.lib.impl :as impl :refer [defimpl]]
            [std.lib.time :as t]
            [std.protocol.request :as protocol.request]))

(impl/build-impl {}
                 protocol.request/IRequest
                 :with-opts :all
                 protocol.request/IRequestTransact)

(def ^:dynamic *debug*    nil)
(def ^:dynamic *inputs*   nil)
(def ^:dynamic *bulk*     {})
(def ^:dynamic *current*  {})
(def ^:dynamic *transact* {})

(declare req-fn)

;;
;; context
;;

(defn bulk-context
  "creates a bulk context"
  {:added "3.0"}
  ([]
   {:received (f/incomplete)
    :cache (atom {:inputs  []
                  :outputs []})}))

(defn transact-context
  "creates a transact context"
  {:added "3.0"}
  ([]
   (atom {:inputs {:final (f/incomplete)
                   :count 0}
          :outputs []})))

(defn req:opts-clean
  "clean opts for processing inputs"
  {:added "3.0"}
  ([opts]
   (dissoc opts :measure :time :debug :pre :post
           :chain :async :final :received :transact)))

(defn req:opts
  "clean opts for processing inputs"
  {:added "3.0"}
  ([opts]
   (req:opts opts nil))
  ([opts {:keys [pre post chain] :as m}]
   (cond-> opts
     m     (merge (dissoc m :chain :post :pre))
     pre   (update :pre   #(vec (into % pre)))
     post  (update :post  (comp vec (partial concat post)))
     chain (update :chain (comp vec (partial concat chain))))))

(defn opts:timer
  "creates the timer
 
   (opts:timer prn)
   => h/future?"
  {:added "3.0"}
  ([timer]
   (let [timer (cond (h/atom? timer)
                     (partial reset! timer)

                     :else timer)
         timer  (if (fn? timer)
                  (doto (f/incomplete)
                    (f/on:success timer))
                  timer)]
     timer)))

(defn opts:wrap-measure
  "creates the measure opts
 
   (opts:wrap-measure {} prn)
   => (contains-in {:pre [fn?], :chain [fn?]})"
  {:added "3.0"}
  ([opts measure]
   (let [{:keys [start stop timer]
          :or {start h/NIL
               stop  h/NIL}} (cond (map? measure)
                                   measure

                                   :else {:timer measure})
         timer    (opts:timer timer)
         holder   (atom nil)
         start-fn (fn [input]
                    (start input)
                    (reset! holder (t/time-ns))
                    input)
         stop-fn  (fn [output]
                    (if timer
                      (f/future:force timer
                                      {:output output
                                       :start @holder
                                       :end (t/time-ns)}))
                    (stop output)
                    output)]
     (-> opts
         (update :pre   (comp vec (partial cons start-fn)))
         (update :chain (fnil #(conj % stop-fn) []))))))

(defn measure:debug
  "creates a debug measure
 
   (measure:debug {})
   => (contains {:start fn? :stop fn?})"
  {:added "3.0"}
  ([{:keys [context]}]
   {:start (fn [input]
             (env/local :println :START
                        context input))
    :stop  (fn [output]
             (env/local :println :STOP
                        context output))}))

(defn req:opts-init
  "initialise request opts"
  {:added "3.0"}
  ([{:keys [measure debug context] :as opts}]
   (cond-> opts
     debug   (opts:wrap-measure (measure:debug opts))
     measure (opts:wrap-measure measure))))

;;
;; single exec
;;

(defn req:return
  "returns the output
 
   (req:return 1 false)"
  {:added "3.0"}
  ([result async]
   (cond async result

         (f/future? result)
         @result

         :else
         result)))

(defn req:single-prep
  "prepares `req:single` options
 
   (req:single-prep {:async true})"
  {:added "3.0"}
  ([{:keys [async received transacted chain] :as opts}]
   (cond async
         (let [received (or received (f/incomplete))
               final    (if transacted
                          transacted
                          (f/future:chain received chain))]
           (assoc opts :received received :final final))

         :else opts)))

(defn req:single-complete
  "completes the `req:single` call"
  {:added "3.0"}
  ([{:keys [async transacted received final post chain] :as opts} result]
   (cond transacted final

         async
         (do (f/future:force received
                             (reduce h/call result post))
             final)

         :else (h/-> result
                     (reduce h/call % post)
                     (reduce h/call % chain)))))

(defn req:single
  "creates a single request call"
  {:added "3.0"}
  ([client command]
   (req:single client command {}))
  ([client command opts]
   (let [{:keys [pre post final async] :as opts} (req:single-prep opts)
         result (h/-> command
                      (reduce h/call % pre)
                      (request-single client % opts)
                      (process-single client % opts))
         final  (req:single-complete opts result)]
     (req:return final (if (get *transact* client)
                         true
                         async)))))

;;
;; req - unit root
;;

(defn req:unit
  "creates a single request call with `*bulk*` context"
  {:added "3.0"}
  ([client command]
   (req:unit client command {}))
  ([client command {:keys [pre post transacted catch chain] :as opts}]
   (let [{:keys [received cache]} (get *bulk* client)
         output (at/swap-return! cache
                                 (fn [{:keys [inputs outputs] :as m}]
                                   (let [i        (count inputs)
                                         output   (f/on:success received
                                                                (fn [arr]
                                                                  (reduce h/call (nth arr i) post)))
                                         output   (cond-> output
                                                    catch (f/on:exception catch))
                                         final    (if transacted
                                                    (f/future:chain transacted [])
                                                    (f/future:chain output chain))
                                         command  (if (h/iobj? command)
                                                    (vary-meta command merge (req:opts-clean opts))
                                                    command)]
                                     [final  (-> m
                                                 (update :inputs  conj (reduce h/call command pre))
                                                 (update :outputs conj final))])))
         _  (swap! (get *current* client) conj output)]
     output)))

(defn bulk:inputs
  "capture all inputs on the client"
  {:added "3.0" :style/indent 1}
  ([client thunk]
   (let [context (bulk-context)]
     (binding [*bulk*    (assoc *bulk* client context)
               *current* (assoc *current* client (atom []))]
       (thunk)
       (:inputs @(:cache context))))))

(defn bulk-collect
  "collects bulk inputs"
  {:added "3.0" :style/indent 1}
  ([client thunk chain]
   (let [top     (get *bulk* client)
         context (or top (bulk-context))
         current (atom [])]
     (binding [*bulk*    (assoc *bulk* client context)
               *current* (assoc *current* client current)]
       (thunk)
       [(if (nil? top)
          (-> (dissoc context :cache)
              (merge @(:cache context))))
        @current]))))

(defn bulk-process
  "processes the client given bulk inputs"
  {:added "3.0"}
  ([client {:keys [inputs received]} {:keys [pre] :as opts}]
   (let [result  (h/->> (reduce h/call inputs pre)
                        (request-bulk client % opts)
                        (process-bulk client inputs % opts))
         _  (f/future:force received result)]
     received)))

(defn bulk
  "allows query inputs to be combined"
  {:added "3.0" :style/indent 1}
  ([client thunk]
   (bulk client thunk {}))
  ([client thunk opts]
   (let [{:keys [async catch chain post] :as opts} (req:opts-init opts)
         [bulk current] (bulk-collect client thunk chain)
         received (f/on:all current
                            (fn [& arr]
                              (reduce h/call (apply vector arr) post)))
         received (cond-> received
                    catch (f/on:exception catch))
         final    (f/future:chain received chain)
         _ (cond bulk
                 (bulk-process client bulk opts)

                 :else
                 (swap! (get *current* client) conj final))]
     (req:return final (boolean (or (not bulk)
                                    (get *transact* client)
                                    async))))))

(defn transact
  "enable transactions on client
 
   (->> (transact |client|
                  (fn []
                    (req |client| {:type :eval :form 1}
                        {:chain [#(* 8 %)]})))
        (map deref))"
  {:added "3.0" :style/indent 1}
  ([client thunk]
   (let [top       (get *transact* client)
         context   (or top (transact-context))
         start-cmd (transact-start client)
         end-cmd   (transact-end client)
         _         (if (nil? top)
                     (req-fn client start-cmd {:async true}))
         results   (binding [*transact* (assoc *transact* client context)]
                     (thunk)
                     (:outputs @context))
         final     (-> @context :inputs :final)
         output    (if (nil? top)
                     (req-fn client end-cmd {:async true}))
         _   (if output
               (f/on:complete output
                              (fn [val err]
                                (if err
                                  (f/future:force final :exception err)
                                  (f/future:force final :value val)))))]
     results)))

(defn transact-prep
  "prepare req:opts given transaction context
 
   (transact-prep |client| (transact-context)
                  {:type :eval, :form 1}
                  {:chain [inc]})"
  {:added "3.0"}
  ([client context command {:keys [post chain] :as opts}]
   (let [transacted (at/swap-return! context
                                     (fn [{:keys [inputs outputs] :as m}]
                                       (let [{:keys [final count]} inputs
                                             transacted  (-> (f/on:success
                                                              final
                                                              (fn [arr]
                                                                (reduce h/call (nth arr count) post)))
                                                             (f/future:chain chain))]
                                         [transacted (-> (update-in m [:inputs :count] inc)
                                                         (update :outputs conj transacted))])))]
     (assoc opts :transacted transacted))))

(defn req-fn
  "function for req"
  {:added "3.0"}
  ([client command]
   (req-fn client command {}))
  ([client command opts]
   (let [bulk     (get *bulk* client)
         transact (get *transact* client)
         opts     (req:opts-init opts)
         opts     (if transact
                    (transact-prep client transact command opts)
                    opts)]
     (cond *inputs*
           (swap! *inputs* conj command)

           bulk
           (req:unit client command opts)

           :else
           (req:single client command opts)))))

(defn bulk:transact
  "creates a transaction within a bulk context"
  {:added "3.0" :style/indent 1}
  ([client thunk]
   (bulk:transact client thunk {}))
  ([client thunk opts]
   (let [{:keys [async chain pre post] :as opts} (req:opts-init opts)
         state (volatile! nil)
         _     (reduce h/call nil pre)]
     (bulk client
           (fn []
             (let [res (transact client thunk)]
               (vreset! state res)))
           (req:opts-clean opts))
     (-> @state
         (f/on:all (fn [& arr]
                     (reduce h/call (apply vector arr) post)))
         (f/future:chain chain)
         (req:return (boolean (or (get *bulk* client)
                                  (get *transact* client)
                                  async)))))))

(defmacro req
  "execute command on single, bulk and transact calls
 
   (req |client| {:type :eval :form 1})"
  {:added "3.0" :adopt true}
  ([client command]
   (let [context (-> (meta &form)
                     (assoc :ns (env/ns-sym)))]
     `(req-fn ~client ~command {:context (quote ~context)})))
  ([client command opts]
   (let [context (-> (meta &form)
                     (assoc :ns (env/ns-sym)))]
     `(req-fn ~client ~command (assoc ~opts :context (quote ~context))))))

(defmacro req:in
  "captures the command input"
  {:added "3.0"}
  ([& body]
   `(binding [*inputs* (atom [])]
      ((fn [] ~@body))
      @*inputs*)))

(defmacro req:bulk
  "creates a bulk request"
  {:added "3.0" :style/indent 1}
  ([[client opts] & body]
   (let [context (-> (meta &form)
                     (assoc :ns (env/ns-sym)
                            :type :bulk))]
     `(bulk ~client
            (fn []
              ~@body)
            (assoc ~opts :context (quote ~context))))))

(defmacro req:transact
  "creates a bulk transaction request"
  {:added "3.0" :style/indent 1}
  ([[client opts] & body]
   (let [context (-> (meta &form)
                     (assoc :ns (env/ns-sym)
                            :type :transact))]
     `(bulk:transact ~client
                     (fn []
                       ~@body)
                     (assoc ~opts :context (quote ~context))))))

(defn bulk:map
  "map function across a client"
  {:added "3.0" :style/indent 1}
  ([client f inputs]
   (bulk:map client f inputs {}))
  ([client f inputs opts]
   (bulk client
         (fn []
           (mapv (partial f client) inputs))
         opts)))

(defn transact:map
  "performs a transaction across the client
 
   (transact:map |client|
                 req-fn [{:type :eval :form 1}
                         {:type :eval :form 2}])
 
   => [1 2]"
  {:added "3.0" :style/indent 1}
  ([client f inputs]
   (transact:map client f inputs {}))
  ([client f inputs opts]
   (bulk:transact client
                  (fn []
                    (mapv (partial f client) inputs))
                  opts)))

;;
;; extend for functions
;;

(defn- fn-request-single
  ([f args _]
   (apply f args)))

(defn- fn-request-bulk
  ([f arr _]
   (mapv (partial apply f) arr)))

(defn- fn-process-bulk
  ([_ _ data _] data))

(defn- fn-process-single
  ([_ data _] data))

(impl/extend-impl clojure.lang.AFn
                  :prefix "fn-"
                  :protocols [std.protocol.request/IRequest])
