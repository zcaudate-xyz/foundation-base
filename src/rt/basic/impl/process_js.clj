(ns rt.basic.impl.process-js
  (:require [clojure.string]
            [rt.basic.type-basic :as basic]
            [rt.basic.type-common :as common]
            [rt.basic.type-oneshot :as oneshot]
            [rt.basic.type-websocket :as websocket]
            [std.lang.base.impl :as impl]
            [std.lang.base.runtime :as rt]
            [std.lang.interface.type-notify :as notify]
            [xt.lang.common-lib :as lib]))

(defn node-path
  "Builds a NODE_PATH that keeps temp-file Node executions resolving project-local modules."
  {:added "4.1"}
  []
  (let [root     (or (System/getenv "PWD")
                     (System/getProperty "user.dir"))
        local    (str root "/node_modules")
        existing (System/getenv "NODE_PATH")]
    (->> [local existing]
         (remove clojure.string/blank?)
         distinct
         (clojure.string/join java.io.File/pathSeparator))))

(def +node-shell-env+
  (let [path (node-path)]
    (cond-> {}
      (not (clojure.string/blank? path))
      (assoc "NODE_PATH" path))))

(def +program-init+
  (common/put-program-options
   :js  {:default  {:oneshot    :nodejs
                    :basic      :nodejs
                    :websocket  :nodejs}
         :env      {:nodejs    {:exec   "node"
                                :env    +node-shell-env+
                                :bench  {:shell {:env +node-shell-env+}}
                                 :flags  {:oneshot   ["-e"]
                                          :basic     ["-e"]
                                          :websocket ["-e"]
                                          :interactive ["-i"]
                                         :json ["JSON" :builtin]
                                         :bench {:websocket ["ws" :install]}}}
                    :qjs       {:exec   "qjs"
                                :flags  {:oneshot ["-e"]
                                         :interactive ["-i"]
                                         :json ["JSON" :builtin]
                                         :ws-client false}}
                    :osx       {:exec   "osascript"
                                :stderr true
                                :flags  {:oneshot ["-l" "JavaScript" "-e"]
                                         :interactive ["-l" "JavaScript" "-i"]
                                         :json ["JSON" :builtin]
                                         :ws-client false}}
                    :jsc       {:exec   "jsc"
                                :flags  {:oneshot ["-e"]
                                         :interactive ["-i"]
                                         :json ["JSON" :builtin]
                                         :ws-client false}}}}))

;;
;; ONESHOT
;;

(def ^{:arglists '([body])}
  default-oneshot-wrap
  (let [bootstrap  (impl/emit-entry-deps
                    lib/return-eval
                    {:lang :js
                     :layout :flat})]
    (fn [body]
      (str bootstrap
           "\n\n"
           (impl/emit-as
            :js [(list 'console.log (list 'return-eval body))])))))

(def +js-oneshot-config+
  (common/set-context-options
   [:js :oneshot :default]
   {:main  {:in    #'default-oneshot-wrap}
    :emit  {:body  {:transform #'rt/return-transform}}
    :json :full}))


(def +js-oneshot+
  [(rt/install-type!
    :js :oneshot
    {:type :hara/rt.oneshot
     :instance {:create oneshot/rt-oneshot:create}})])

;;
;; BASIC
;; 

(def +client-basic+
  '[(:- :import net :from "'net'")
    (:- :import rl :from "'readline'")
    (:- :import #{createRequire} :from "'module'")
    (var require (createRequire (+ (or (. process env ["PWD"])
                                       (. process (cwd)))
                                   "/package.json")))
    (defn client-basic
      [host port opts]
      (let [conn (new net.Socket)
            _      (conn.connect port host)
            stream (rl.createInterface conn conn)]
        (stream.on "line" (fn [line]
                            (conn.write (+ (return-eval (JSON.parse line))
                                           "\n"))))))])

(def make-bootstrap
  (fn []
    (clojure.string/join "\n\n"
              [(impl/emit-entry-deps
                lib/return-eval
                {:lang :js
                 :layout :flat})
               (impl/emit-as
                :js +client-basic+)])))

(def ^{:arglists '([port & [{:keys [host]}]])}
  default-basic-client
  (let [bootstrap (make-bootstrap)]
    (fn [port & [{:keys [host]}]]
      (str bootstrap
           "\n\n"
           (impl/emit-as
            :js [(list 'client-basic
                       (or host "127.0.0.1")
                       port
                       {})])))))

(def +default-basic-config+
  {:bootstrap #'default-basic-client
    :main   {}
   :emit   {:body  {:transform #'rt/return-transform}
            :lang/format :global}
   :json   :full
   :encode :json ;; default
   :timeout 2000})

(def +js-basic-config+
  (common/set-context-options
   [:js :basic :default]
   +default-basic-config+))

(def +js-basic+
  [(rt/install-type!
    :js :basic
    {:type :hara/rt.basic
     :instance {:create #'basic/rt-basic:create}
     :config {:layout :full}})])


;;
;; WEBSOCKET
;; 

(def +client-ws+
  '[(:- :import #{createRequire} :from "'module'")
    (var require (createRequire (+ (or (. process env ["PWD"])
                                       (. process (cwd)))
                                   "/package.json")))
    (defn client-ws
      [host port opts]
      (let [WS     (or (. globalThis ["WebSocket"])
                       (require "ws"))
            conn   (new WS (+ "ws://" host ":" port "/"))
            _      (. (require "process") (on "unhandledRejection" (fn:>)))]
        (:=  (. conn onmessage)
             (fn [msg]
               (let [#{id body} (JSON.parse msg.data)
                     out (return-eval body)]
                 (. conn (send (JSON.stringify {:id id
                                                :status "ok"
                                                :body out}))))))))])

(def ^{:arglists '([port & [{:keys [host]}]])}
  default-websocket-client
  (let [bootstrap (->> [(impl/emit-entry-deps
                         lib/return-eval
                         {:lang :js
                          :layout :flat})
                        (impl/emit-as
                         :js +client-ws+)]
                       (clojure.string/join "\n\n"))]
    (fn [port & [{:keys [host]}]]
      (str bootstrap
           "\n\n"
           (impl/emit-as
            :js [(list 'client-ws
                       (or host "127.0.0.1")
                       port
                       {})])))))

(def +js-websocket-config+
  (common/set-context-options
   [:js :websocket :default]
   {:bootstrap #'default-websocket-client
    :main  {}
    :emit  {:native {:suppress true}
            :body  {:transform #'rt/return-transform}
            :lang/jsx false
            :lang/format :global}
    :json :full
    :encode :json
    :timeout 2000}))

(def +js-websocket+
  [(rt/install-type!
    :js :websocket
    {:type :hara/rt.websocket
     :instance {:create #'websocket/rt-websocket:create}
     :config {:layout :full}})])
