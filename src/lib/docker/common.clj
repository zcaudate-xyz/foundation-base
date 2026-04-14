(ns lib.docker.common
  (:require [clojure.string]
            [std.concurrent :as cc]
            [std.json :as json]
            [std.lib.foundation :as f]
            [std.lib.os :as os]))

(defonce ^:dynamic *host* (System/getenv "DOCKER_HOST"))

(defonce ^:dynamic *timeout* 10000)

(defn raw-exec
  "executes a shell command"
  {:added "4.0" :guard true}
  [args opts]
  (let [lines (->> @(apply os/sh args)
                   (clojure.string/trim)
                   (clojure.string/split-lines)
                   (filter not-empty))]
    (cond->> lines
      (:json opts) (mapv #(json/read % json/+keyword-mapper+)))))

(defn raw-command
  "executes a docker command"
  {:added "4.0"}
  [command & [opts tail]]
  (let [{:keys [host format]} opts
        args (concat ["docker"]
                     (when host ["--host" host])
                     command
                     (cond (= false format)
                           []

                           :else
                           ["--format" (or format "{{json .}}")])
                     tail)]
    (raw-exec args (merge {:json true} opts))))

(defn get-ip
  "gets the ip of a container"
  {:added "4.0"}
  [container-id]
  (first (raw-command ["inspect" "-f"
                       "{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}"
                       container-id]
                      {:format false
                       :json false})))

(defn list-containers
  "gets all local containers
 
   (list-containers)
   => vector?"
  {:added "4.0"}
  ([]
   (list-containers {} ["-f" "name=.*"]))
  ([opts tail]
   (raw-command ["ps"]
                (merge {:format "{\"id\":\"{{ .ID }}\", \"image\": \"{{ .Image }}\", \"name\":\"{{ .Names }}\"}"}
                       opts)
                (or tail ["-f" "name=.*"]))))

(defn has-container?
  "checks that a container exists"
  {:added "4.0"}
  ([{:keys [id group]
     :or {group "testing"}}]
   (not (empty? (filter #(-> % :name (= (str group "_" id)))
                        (list-containers))))))

(defn start-container
  "starts a container"
  {:added "4.0"}
  ([{:keys [group id image cmd flags labels volumes
            environment ports expose remove detached no-host] :as m
     :or {remove true detached true
          group "testing"}}
    & [repeat]]
   (let [volume-args (reduce (fn [acc [from to]]
                               (conj acc "-v" (str from ":" to)))
                             []
                             volumes)
         label-args (reduce (fn [acc [k v]]
                              (conj acc "-l" (str k "=" v)))
                            volume-args
                            labels)
         env-args   (reduce (fn [acc [k v]]
                              (conj acc "-e" (str k "=" v)))
                            label-args
                            environment)
         port-args  (reduce (fn [acc ports]
                              (conj acc "-p"
                                    (if (vector? ports)
                                      (clojure.string/join ":" ports)
                                      ports)))
                            env-args
                            expose)
         base-args  (cond-> port-args
                      (not no-host)
                      (conj "--add-host=host.docker.internal:host-gateway"))
         name       (str group "_" (or id (f/error "Id required")))
         cmd-args   (cond
                      (coll? cmd) cmd
                      (some? cmd) [cmd]
                      :else [])
         args       (-> (concat base-args
                                ["--name" name image]
                                cmd-args)
                        vec)
         run-args   (->> (concat ["docker" "run"]
                                 (when *host* ["--host" *host*])
                                 (when detached ["-d"])
                                 (when remove ["--rm"])
                                 flags
                                 args)
                        (clojure.core/remove nil?)
                        vec)
         cid        (cond
                      (has-container? m)
                      nil

                      :else
                      @(apply os/sh run-args))
         cid        (if (empty? cid)
                      @(os/sh {:args (concat ["docker" "ps"]
                                            (when *host* ["--host" *host*])
                                            ["-aqf" (str "name=^" name "$")])})
                      cid)
         ip         (get-ip cid)]
     (assoc
      (if (or repeat ip)
        (assoc m :container-id cid :container-ip ip :container-name name)
        (start-container m true))
       :args args))))

(defn stop-container
  "stops a container"
  {:added "4.0"}
  ([{:keys [group id] :as m
     :or {group "testing"}}]
   (when (has-container? m)
     @(os/sh {:args (concat ["docker" "kill"]
                           (when *host* ["--host" *host*])
                           [(str group "_" (or id (f/error "Id required")))])}))))
