^{:no-test true}
(ns rt.shell.interface-basic
  (:require [rt.basic.impl.process-bash :as process]
            [std.concurrent :as cc]
            [std.lang.base.runtime :as default]
            [std.lib.collection]
            [std.lib.component]
            [std.lib.impl :refer [defimpl]]
            [std.protocol.component :as protocol.component]
            [std.protocol.context :as protocol.context]
            [std.string.common]))

(def ^:dynamic *single-line* false)

(defmacro with:single-line
  "allows reading of a single line (faster)"
  {:added "4.0"}
  [& body]
  `(binding [*single-line* true]
     ~@body))

(defn raw-eval-basic
  "basic evaluation for the bash runtime"
  {:added "4.0"}
  ([{:keys [relay] :as shell} body]
   (std.string.common/trim-right (:output @(cc/send relay
                                      (if (nil? body)
                                        {:op (if *single-line*
                                               :line
                                               :string)}
                                        body))))))

(defn invoke-ptr-basic
  "basic invoke for a pointer and arguments"
  {:added "4.0"}
  ([shell ptr args]
   (default/default-invoke-script
    shell
    ptr
    args
    raw-eval-basic
    {:emit  {:body  {:transform #'process/default-body-transform}}
     :json identity})))

(defn- shell-basic-string
  [{:keys [id lang]}]
  (str "#shell.basic" [id]))

(defimpl ShellBasic [relay error]
  :string shell-basic-string
  :protocols [protocol.component/IComponent
              :body   {-start  (do (std.lib.component/start relay)
                                   component)
                       -stop   (std.lib.component/stop relay)
                       -kill   (std.lib.component/kill relay)}
              protocol.context/IContext
              :prefix "default/default-"
              :method {-raw-eval    raw-eval-basic
                       -invoke-ptr  invoke-ptr-basic}])

(defn shell-basic:create
  "creates a basic shell"
  {:added "4.0"}
  ([]
   (shell-basic:create {}))
  ([{:keys [process] :as m}]
   (let [err   (atom {})
         relay (cc/relay:create (-> {:type :process
                                     :args ["bash"]
                                     :options {:in  {:quiet true}
                                               :err {:return :passive
                                                     :watch  err}}}
                                    (std.lib.collection/merge-nested process)))]
     (map->ShellBasic (merge (dissoc m :process)
                             {:relay relay :error err})))))


(defn shell-basic
  "creates and starts a basic shell"
  {:added "4.0"}
  ([]
   (shell-basic {}))
  ([m]
   (-> (shell-basic:create m)
       (std.lib.component/start))))

(def +bash-basic+
  [(default/install-type!
    :bash :basic
    {:type :hara/rt.shell.basic
     :instance {:create #'shell-basic:create}
     :config {:layout :full}})])

(comment
  (./import)
  (shell-basic)
  (./create-tests)
  (def )(std.lib.component/start (create-relay))
  (def -sh- (create-shell ["lua" "-i"]))
  
  (std.lib.component/start (:shell -sh-))
  (std.lib.component/stop (:shell -sh-))

  (cmd-exists? "lua")
  (cmd-exists? "lua -i"))
