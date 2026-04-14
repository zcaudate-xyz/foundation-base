(ns rt.basic.type-oneshot
  (:require [clojure.string]
            [rt.basic.type-common :as common]
            [std.json :as json]
            [std.lang.base.pointer :as ptr]
            [std.lang.base.runtime :as default]
            [std.lib.collection :as collection]
            [std.lib.foundation :as f]
            [std.lib.impl :as impl]
            [std.lib.os :as os]
            [std.protocol.context :as protocol.context]))

(defn sh-exec
  "basic function for executing a shell process"
  {:added "4.0"}
  [input-args input-body {:keys [pipe
                                 trim
                                 stderr
                                 raw
                                 root
                                 shell]
                           :or {trim clojure.string/trim-newline}}]
  (try (let [args (if pipe
                    input-args
                    (conj input-args input-body))
             proc (os/sh (merge shell
                                {:wait false
                                 :args args
                                 :root root}))
              _    (cond-> proc
                     pipe  (doto (os/sh-write input-body) (os/sh-close))
                     :then (os/sh-wait))
              {:keys [err out exit] :as ret} (os/sh-output proc)]
         (cond raw
               (let [out-lines (->> (clojure.string/split-lines (trim out))
                                    (remove empty?)
                                    seq)
                     err-lines (->> (clojure.string/split-lines (trim err))
                                    (remove empty?)
                                    seq)]
                 [exit (or out-lines err-lines [])])

               :else
               (trim out)))
       (catch Throwable t
         (if stderr
           (trim (.getMessage t))
           (throw t)))))

(defn raw-eval-oneshot
  "evaluates a raw statement with oneshot"
  {:added "4.0"}
  ([{:keys [exec
            process] :as rt} body]
   (sh-exec exec body process)))

(defn invoke-ptr-oneshot
  "gets the oneshow invoke working"
  {:added "4.0"}
  ([{:keys [process lang layout] :as rt
     :or {layout :full}} ptr args]
   (default/default-invoke-script (assoc rt :layout layout)
                                  ptr args raw-eval-oneshot process)))

(defn- rt-oneshot-string [{:keys [lang runtime program]}]
  (str "#rt.oneshot" [lang runtime program]))

(impl/defimpl RuntimeOneshot [id]
  :string rt-oneshot-string
  :protocols [protocol.context/IContext
              :prefix "default/default-"
              :method {-raw-eval    raw-eval-oneshot
                       -invoke-ptr  invoke-ptr-oneshot}])

(defn rt-oneshot-setup
  "helper function for preparing oneshot params"
  {:added "4.0"}
  ([lang program process exec]
   (rt-oneshot-setup lang program process exec :oneshot))
  ([lang program process exec context]
   (let [program (common/get-program-default lang context program)
         process (collection/merge-nested (common/get-options lang context program)
                                 process)
         exec    (or exec
                     (common/get-program-exec lang context program))]
     [program process exec])))

(defn rt-oneshot:create
  "creates a oneshot runtime"
  {:added "4.0"}
  [{:keys [id
           lang
           runtime
           exec
           program
           process] :as m
    :or {runtime :oneshot}}]
  (let [[program process exec] (rt-oneshot-setup lang program process exec :oneshot)
        flags   (common/get-program-flags lang program)
        _   (cond (not (:oneshot flags))
                  (f/error "Program does not support oneshot runtime"
                           {:lang lang
                            :runtime runtime
                            :flags flags
                            :program program}))]
    (map->RuntimeOneshot (assoc m
                                :id (or id (f/sid))
                                :runtime runtime
                                :program program
                                :exec exec
                                :process process))))

(defn rt-oneshot
  "creates a oneshot runtime"
  {:added "4.0"}
  [{:keys [id
           lang
           runtime
           program
           process] :as m}]
  (rt-oneshot:create m))
