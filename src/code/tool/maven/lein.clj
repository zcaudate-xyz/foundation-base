(ns code.tool.maven.lein
  (:require [std.fs :as fs]
            [std.lib :as h]
            [code.tool.maven.package :as package]))

(defn deploy-lein
  "temporary hack to deploy by shelling out to leiningen"
  {:added "4.0"}
  ([name {:keys [interim] :as params} _ {:keys [root version]}]
   (let [interim (or interim package/+interim+)
         interim (fs/path root interim (str name))
         _       (h/sh {:root (str interim)
                        :args ["rm" "-R" "deploy"]
                        :ignore-errors true})
         _       (h/sh {:root (str interim)
                        :args ["mkdir" "deploy"]})
         _       (h/sh {:root (str interim)
                        :args ["cp" "-R" "package" "deploy/src"]})
         _       (h/sh {:root (str interim)
                        :args ["cp" "project.clj" "deploy"]})
         _       (h/sh {:root (str (fs/path interim "deploy" "src"))
                        :args ["rm" "-R" "META-INF" "MANIFEST.MF"]})
         out     (h/sh {:root (str (fs/path interim "deploy"))
                        :args ["lein" "deploy" "clojars"]})]
     {:results out
      :interim (str interim)})))
