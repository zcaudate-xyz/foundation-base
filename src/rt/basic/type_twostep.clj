(ns rt.basic.type-twostep
  (:require [clojure.string]
            [rt.basic.type-common :as common]
            [std.fs :as fs]
            [std.json :as json]
            [std.lang.base.pointer :as ptr]
            [std.lang.base.runtime :as default]
            [std.lib.collection :as collection]
            [std.lib.foundation :as f]
            [std.lib.impl :as impl]
            [std.lib.os :as os]
            [std.protocol.context :as protocol.context]))

(defn sh-exec
  "basic function for executing the compile and run process"
  {:added "4.0"}
  [input-args input-body {:keys [pipe
                                 trim
                                 stderr
                                 raw
                                 root
                                 extension
                                 output-flag]
                          :as opts
                          :or {trim clojure.string/trim-newline}}]
  (let [tmp-exec (java.io.File/createTempFile "tmp" "")
        tmp-file (str tmp-exec
                      "."
                      (or extension
                          (f/error "Requires File Extension"
                                   opts)))
        _   (spit tmp-file input-body)
        _   (os/sh {:args (if output-flag
                           (vec (concat input-args [output-flag (str tmp-exec) (str tmp-file)]))
                           (conj input-args (str tmp-file)))
                   :root (str (fs/parent tmp-file))})]
    (str (os/sh {:args [(str "./" (fs/file-name tmp-exec))]
                :root (str (fs/parent tmp-file))
                }))))

(defn raw-eval-twostep
  "evaluates the twostep evaluation"
  {:added "4.0"}
  ([{:keys [exec
            process] :as rt} body]
   ((or (:exec-fn process) sh-exec) exec body process)))

(defn invoke-ptr-twostep
  "invokes twostep pointer"
  {:added "4.0"}
  ([{:keys [process lang layout] :as rt
     :or {layout :full}} ptr args]
   (default/default-invoke-script (assoc rt :layout layout)
                                  ptr args raw-eval-twostep process)))

(defn- rt-twostep-string [{:keys [lang runtime program]}]
  (str "#rt.twostep" [lang runtime program]))

(impl/defimpl RuntimeTwostep [id]
  :string rt-twostep-string
  :protocols [protocol.context/IContext
              :prefix "default/default-"
              :method {-raw-eval    raw-eval-twostep
                       -invoke-ptr  invoke-ptr-twostep}])

(defn rt-twostep-setup
  "setup params for the twostep runtime"
  {:added "4.0"}
  ([lang program process exec]
   (rt-twostep-setup lang program process exec :twostep))
  ([lang program process exec context]
   (let [program (common/get-program-default lang context program)
         process (collection/merge-nested (common/get-options lang context program)
                                 process)
         exec    (or exec
                     (common/get-program-exec lang context program))]
     [program process exec])))

(defn rt-twostep:create
  "creates a twostep runtime"
  {:added "4.0"}
  [{:keys [id
           lang
           runtime
           exec
           program
           process] :as m
    :or {runtime :twostep}}]
  (let [[program process exec] (rt-twostep-setup lang program process exec :twostep)
        flags   (common/get-program-flags lang program)
        _   (cond (not (:twostep flags))
                  (f/error "Twostep not available" {:flags flags
                                                    :program program}))]
    (map->RuntimeTwostep (assoc m
                                :id (or id (f/sid))
                                :runtime runtime
                                :program program
                                :exec exec
                                :process process))))

(defn rt-twostep
  "creates an active twostep runtime"
  {:added "4.0"}
  [{:keys [id
           lang
           runtime
           program
           process] :as m}]
  (rt-twostep:create m))



(comment
  
  #_
  (try (let [args (if pipe
                    input-args
                    (conj input-args input-body))
             proc (os/sh {:wait false
                         :args args
                         :root root})
             _    (cond-> proc
                    pipe  (doto (os/sh-write input-body) (os/sh-close))
                    :then (os/sh-wait))
             {:keys [err out exit] :as ret} (os/sh-output proc)]
         (cond raw
               [exit (or (not-empty (clojure.string/split-lines (trim out)))
                         (clojure.string/split-lines (trim err)))]

               :else
               (trim out)))
       (catch Throwable t
         (if stderr
           (trim (.getMessage t))
           (throw t))))
  

  )
