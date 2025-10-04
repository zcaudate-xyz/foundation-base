(ns rt.libpython
  (:require [std.protocol.component :as protocol.component]
            [std.protocol.context :as protocol.context]
            [rt.basic.impl.process-js :as js]
            [rt.basic.impl.process-python :as python]
            [std.lang.base.impl :as impl]
            [std.lang.base.runtime :as default]
            [libpython-clj2.python :as lp]
            [std.concurrent :as cc]
            [std.lang :as l]
            [std.lib :as h :refer [defimpl]]
            [std.string :as str]
            [xt.lang.base-repl :as k]))

(defonce +preamble+
  (delay (lp/initialize!)))

(defn eval-raw
  "performs an exec expression"
  {:added "3.0"}
  ([_ string]
   @+preamble+
   (lp/run-simple-string string)))

(defn eval-libpython
  "evals body in the runtime
   
   (lp/eval-libpython nil
                      \"OUT = 1 + 1\")
   => 2"
  {:added "4.0"}
  ([_ string]
   (get-in (eval-raw nil string)
           [:globals "OUT"])))

(def +options+
  {})

(defn invoke-libpython
  "invokes a pointer in the runtime
 
   (lp/invoke-libpython (l/rt :python)
                        k/sub
                        [1 2])
   => -1"
  {:added "4.0"}
  ([{:keys [state lang layout] :as rt} ptr args]
   (default/default-invoke-script
    rt ptr args eval-libpython
    {:main  {}
     :emit  {:body  {:transform #'python/default-body-transform}}})))

(defn start-libpython
  "starts the libpython runtime"
  {:added "3.0"}
  [rt]
  @+preamble+
  rt)

(defn stop-libpython
  "stops the libpython runtime"
  {:added "3.0"}
  ([rt] rt))

(defn- rt-libpython-string
  ([rt]
   (str "#rt.libpython" (into {} rt))))

(defimpl RuntimeLibpython [lang raw]
  :string rt-libpython-string
  :protocols [std.protocol.component/IComponent
              :exclude [-kill]
              :suffix "-libpython"
              protocol.context/IContext
              :prefix "default/default-"
              :method {-raw-eval eval-libpython
                       -invoke-ptr invoke-libpython}])

(defn rt-libpython:create
  "creates a libpython runtime
 
   (h/-> (lp/rt-libpython:create {:lang :js})
         (h/start)
         (h/stop))
   => lp/rt-libpython?"
  {:added "4.0"}
  [{:keys [id]
    :or {id (h/sid)}
    :as m}]
  (map->RuntimeLibpython (assoc m :id id)))

(defn rt-libpython
  "creates a libpython rt"
  {:added "4.0"}
  ([m]
   (-> (rt-libpython:create m)
       (h/start))))

(defn rt-libpython?
  "checks object is a libpython rt"
  {:added "4.0"}
  [obj]
  (instance? RuntimeLibpython obj))

;;
;; Hooks for lang
;;

(def +init+
  [(default/install-type!
    :python :libpython
    {:type :hara/rt.libpython
     :config {:lang :python :layout :full}
     :instance {:create rt-libpython:create}})])
