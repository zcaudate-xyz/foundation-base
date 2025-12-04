(ns rt.basic.type-twostep
  (:require [std.protocol.context :as protocol.context]
            [std.lang.base.pointer :as ptr]
            [std.lang.base.runtime :as default]
            [std.lib :as h :refer [defimpl]]
            [std.json :as json]
            [std.string :as str]
            [std.fs :as fs]
            [rt.basic.type-common :as common]))

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
                          :or {trim str/trim-newlines}}]
  (let [tmp-exec (java.io.File/createTempFile "tmp" "")
        tmp-file (str tmp-exec
                      "."
                      (or extension
                          (h/error "Requires File Extension"
                                   opts)))
        _   (spit tmp-file input-body)
        _   (h/sh {:args (if output-flag
                           (vec (concat input-args [output-flag (str tmp-exec) (str tmp-file)]))
                           (conj input-args (str tmp-file)))
                   :root (str (fs/parent tmp-file))})]
    (str (h/sh {:args [(str "./" (fs/file-name tmp-exec))]
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

(defimpl RuntimeTwostep [id]
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
         process (h/merge-nested (common/get-options lang context program)
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
                  (h/error "Twostep not available" {:flags flags
                                                    :program program}))]
    (map->RuntimeTwostep (assoc m
                                :id (or id (h/sid))
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
             proc (h/sh {:wait false
                         :args args
                         :root root})
             _    (cond-> proc
                    pipe  (doto (h/sh-write input-body) (h/sh-close))
                    :then (h/sh-wait))
             {:keys [err out exit] :as ret} (h/sh-output proc)]
         (cond raw
               [exit (or (not-empty (str/split-lines (trim out)))
                         (str/split-lines (trim err)))]

               :else
               (trim out)))
       (catch Throwable t
         (if stderr
           (trim (.getMessage t))
           (throw t))))
  

  )
