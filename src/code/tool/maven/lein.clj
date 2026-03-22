(ns code.tool.maven.lein
  (:require [code.tool.maven.package :as package]
            [std.fs :as fs]
            [std.lib.os :as os]))

(defn deploy-lein
  "temporary hack to deploy by shelling out to leiningen"
  {:added "4.0"}
  ([name {:keys [interim] :as params} _ {:keys [root version]}]
   (let [interim (or interim package/+interim+)
         interim (fs/path root interim (str name))
         _       (os/sh {:root (str interim)
                        :args ["rm" "-R" "deploy"]
                        :ignore-errors true})
         _       (os/sh {:root (str interim)
                        :args ["mkdir" "deploy"]})
         _       (os/sh {:root (str interim)
                        :args ["cp" "-R" "package" "deploy/src"]})
         _       (os/sh {:root (str interim)
                        :args ["cp" "project.clj" "deploy"]})
         _       (os/sh {:root (str (fs/path interim "deploy" "src"))
                        :args ["rm" "-R" "META-INF" "MANIFEST.MF"]})
         out     (os/sh {:root (str (fs/path interim "deploy"))
                        :args ["lein" "deploy" "clojars"]})]
     {:results out
      :interim (str interim)})))
