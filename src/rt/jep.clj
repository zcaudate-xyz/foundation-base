(ns rt.jep
  (:require [std.protocol.component :as protocol.component]
            [std.protocol.context :as protocol.context]
            [rt.jep.bootstrap :as bootstrap]
            [rt.basic.impl.process-python :as python]
            [std.lang.base.runtime :as default]
            [std.lang.base.pointer :as ptr]
            [std.lang.base.impl :as impl]
            [std.concurrent :as cc]
            [std.lang :as l]
            [std.lib :as h :refer [defimpl]]
            [std.string :as str]
            [xt.lang.base-repl :as k])
  (:import (jep SharedInterpreter
                Interpreter)
           (jep.python PyObject
                       PyCallable)))

(defonce ^:dynamic *interpreters* (atom #{}))

(defonce ^:dynamic *bus* nil)

(def +init+
  (h/res:variant-add
   :hara/concurrent.bus
   {:id    :rt.jep/bus
    :alias :hara/jep.bus
    :mode {:allow #{:global} :default :global}
    :instance {:setup    (fn [bus] (h/set! *bus* bus) (h/start bus))
               :teardown (fn [bus] (h/set! *bus* nil) (h/stop bus))}}))

(def +startup+
  (impl/emit-entry-deps
   k/return-eval
   {:lang :python
    :layout :flat}))

(defn jep-bus
  "gets or creates a runtime bus for thread isolation"
  {:added "3.0"}
  []
  (or *bus*
      (h/res :hara/jep.bus)))

;;
;; Interpreter
;;

(defn ^Interpreter make-interpreter
  "makes a shared interpreter
 
   (jep/make-interpreter)
   => jep.SharedInterpreter"
  {:added "3.0"}
  ([]
   @bootstrap/+init+
   (let [itp (SharedInterpreter.)
         _ (swap! *interpreters* conj itp)]
     itp)))

(defn close-interpreter
  "closes the shared interpreter"
  {:added "3.0"}
  ([^Interpreter itp]
   (if itp (.close itp))
   (swap! *interpreters* disj itp)
   itp))

(defn eval-exec-interpreter
  "executes script on the interpreter"
  {:added "3.0"}
  ([itp string]
   (.exec ^Interpreter itp string)))

(defn eval-get-interpreter
  "gets a value from the interpreter"
  {:added "3.0"}
  ([itp body]
   (let [val (.getValue ^Interpreter itp body)]
       (cond (instance? PyCallable val)
             {:callable (str val)}

             :else val))))

(defmacro jep:temp-interpreter
  "gets a value from the interpreter"
  {:added "3.0"}
  ([itp & body]
   `(let [~itp (make-interpreter)]
      (try
        ~@body
        (finally (close-interpreter ~itp))))))

;;
;; Runtime
;;


(defn jep-handler
  "creates a loop handler from interpreter
 
   (jep:temp-interpreter itp
                         (let [handler (jep-handler (atom itp))]
                           (handler {:op :exec :body \"a = 1\"})
                           (handler {:op :get :body \"a\"})))
   => 1"
  {:added "3.0"}
  ([interpreter]
   (fn [{:keys [op body] :as command}]
     (let [result (try (case op
                         :get  (eval-get-interpreter @interpreter body)
                         :exec (eval-exec-interpreter @interpreter body))
                       (catch Throwable t t))]
       result))))

(defn eval-command-jep
  "inputs command input jep context
 
   @(eval-command-jep +jep+ {:op :exec :body \"a = 1\"})
   => nil
 
   @(eval-command-jep +jep+ {:op :get :body \"a\"})
   => 1"
  {:added "3.0"}
  ([{:keys [id bus]} command]
   (-> (cc/bus:send bus id command)
       (h/on:success identity))))

(defn eval-command-fn
  "helper function to input command"
  {:added "3.0"}
  ([op]
   (eval-command-fn op nil))
  ([op key]
   (if (not key)
     (fn [jep]
       (eval-command-jep jep {:op op}))
     (fn [jep input]
       (eval-command-jep jep {:op op key input})))))

(def ^{:arglists '([jep body])} eval-exec-jep  (eval-command-fn :exec :body))
(def ^{:arglists '([jep body])} eval-get-jep   (eval-command-fn :get :body))

(defn start-jep
  "starts up the jep runtime"
  {:added "3.0"}
  ([{:keys [id bus state interpreter] :as jep}]
   (if (empty? @state)
     (let [p (->> {:id id
                   :on-start (fn []
                               (let [itp (make-interpreter)]
                                 (.exec itp +startup+)
                                 (reset! interpreter itp)))
                   :on-stop  (fn [] (h/swap-return! interpreter
                                                    (fn [itp]
                                                      [(close-interpreter itp) nil])))}
                  (cc/bus:open bus (jep-handler interpreter)))]
       (reset! state @p)))
   jep))

(defn stop-jep
  "stops the jep runtime"
  {:added "3.0"}
  ([{:keys [id bus state interpreter stop-fn] :as jep}]
   (if (not-empty @state)
     (let [stopped (h/swap-return! state
                                   (fn [{:keys [stopped]}]
                                     [stopped nil]))
           _ ((or stop-fn cc/bus:close) bus id)]
       @stopped))))

(defn kill-jep
  "kills the jep runtime"
  {:added "3.0"}
  ([jep]
   (stop-jep (assoc jep :stop-fn cc/bus:kill))))

(defn invoke-ptr-jep
  "invokes a pointer in the runtime"
  {:added "4.0"}
  ([{:keys [lang] :as rt} ptr args]
   (default/default-invoke-script
    rt ptr args eval-get-jep
    {:main {:in   (fn [body]
                    (impl/emit-as
                     :python [(list 'return-eval body)]))
            :out  (fn [p]
                    (deref p 2000 [:timed-out]))}
     :emit {:body  {:transform #'python/default-body-transform}}
     :json :full})))

(defn- rt-jep-string
  ([jep]
   (str "#rt.jep" (into {} jep))))

(defimpl RuntimeJep [lang raw]
  :string rt-jep-string
  :protocols [std.protocol.component/IComponent
              :exclude [-kill]
              :suffix "-jep"
              protocol.context/IContext
              :prefix "default/default-"
              :method {-raw-eval eval-get-jep
                       -invoke-ptr invoke-ptr-jep}])

(defn rt-jep:create
  "creates a componentizable runtime"
  {:added "3.0"}
  ([{:keys [id bus] :as m
     :or {bus (jep-bus)
          id  (h/sid)}}]
   (map->RuntimeJep (assoc m
                           :id id
                           :bus bus
                           :layout :full
                           :state (atom nil)
                           :interpreter (atom nil)))))

(defn rt-jep
  "creates and starts the runtime"
  {:added "3.0"}
  ([]
   (rt-jep {}))
  ([m]
   (-> (rt-jep:create m)
       (h/start))))

(defn rt-jep?
  "checks that object is a jep runtime"
  {:added "3.0"}
  [obj]
  (instance? RuntimeJep obj))

(default/install-type!
 :python :jep
 {:type :hara/rt.jep
  :config {:lang :python :layout :full}
  :instance {:create rt-jep:create}})

