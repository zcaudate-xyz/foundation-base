(ns code.tool.maven.task
  (:require [std.config :as config]
            [std.config.ext.gpg :deps true]
            [std.lib :as h]
            [std.task :as task]
            [code.framework.link :as link]
            [lib.aether :as aether]))

(defn make-project
  "makes a maven compatible project
 
   (make-project)
   => map?"
  {:added "3.0"}
  ([]
   (make-project nil))
  ([_]
   (assoc (link/make-project nil)
          :aether (aether/aether))))

(def +main+
  {:construct {:input    (fn [_] :list)
               :lookup   (fn [task project]
                           (link/make-linkages project))
               :env      make-project}
   :params    {:print  {:item    true
                        :result  true
                        :summary true}
               :return :summary}
   :arglists '([] [pkg] [pkg params] [pkg params project] [pkg params lookup project])
   :main      {:count 4}})

(def +main+
  {:construct {:input    (fn [_] :list)
               :lookup   (fn [task project]
                           (link/make-linkages project))
               :env      make-project}
   :params    {:print  {:item    true
                        :result  true
                        :summary true}
               :return :summary}
   :arglists '([] [pkg] [pkg params] [pkg params project] [pkg params lookup project])
   :main      {:count 4}})

(defmethod task/task-defaults :deploy.linkage
  ([_]
   (merge +main+
          {:item      {:list    (fn [lookup _] (vec (sort (keys lookup))))}
           :result    {:columns [{:key    :key
                                  :align  :left}
                                 {:key    :time
                                  :align  :left
                                  :length 10
                                  :format "%d ms"
                                  :color  #{:bold}}]}
           :summary  {:aggregate {}}})))
