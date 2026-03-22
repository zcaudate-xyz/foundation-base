(ns rt.basic.type-oneshot
  (:require [rt.basic.type-common :as common]
            [std.json :as json]
            [std.lang.base.pointer :as ptr]
            [std.lang.base.runtime :as default]
            [std.lib.collection]
            [std.lib.foundation]
            [std.lib.impl :refer [defimpl]]
            [std.lib.os]
            [std.protocol.context :as protocol.context]
            [std.string.common]))

(defn sh-exec
  "basic function for executing a shell process"
  {:added "4.0"}
  [input-args input-body {:keys [pipe
                                 trim
                                 stderr
                                 raw
                                 root]
                          :or {trim std.string.common/trim-newlines}}]
  (try (let [args (if pipe
                    input-args
                    (conj input-args input-body))
             proc (std.lib.os/sh {:wait false
                         :args args
                         :root root})
             _    (cond-> proc
                    pipe  (doto (std.lib.os/sh-write input-body) (std.lib.os/sh-close))
                    :then (std.lib.os/sh-wait))
             {:keys [err out exit] :as ret} (std.lib.os/sh-output proc)]
         (cond raw
               [exit (or (not-empty (std.string.common/split-lines (trim out)))
                         (std.string.common/split-lines (trim err)))]

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

(defimpl RuntimeOneshot [id]
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
         process (std.lib.collection/merge-nested (common/get-options lang context program)
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
                  (std.lib.foundation/error "Oneshot not available" {:flags flags
                                                    :program program}))]
    (map->RuntimeOneshot (assoc m
                                :id (or id (std.lib.foundation/sid))
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

