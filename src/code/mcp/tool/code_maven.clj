(ns code.mcp.tool.code-maven
  (:require [code.mcp.tool.common :as common]
            [code.tool.maven :as maven]))

(def +operations+
  {"linkage"     maven/linkage
   "package"     maven/package
   "infer"       maven/infer
   "clean"       maven/clean
   "install"     maven/install
   "deploy"      maven/deploy
   "deploy-lein" maven/deploy-lein})

(defn code-maven-fn
  [_ {:keys [task target options]}]
  (let [task-name   (or task "install")
        func        (get +operations+ task-name)
        target-arg  (common/read-edn target :all)
        options-arg (common/merge-print-options
                     (common/read-edn options {}))]
    (if-not func
      (common/error-response (str "Maven task not found: " task-name))
      (try
        (common/response (func target-arg options-arg))
        (catch Throwable t
          (common/error-response
           (str "Error executing code.tool.maven task `" task-name "`: "
                (or (.getMessage t) (str t)))))))))

(def code-maven-tool
  {:name "code-maven"
   :description (str "Run `code.tool.maven` packaging and publishing workflows from MCP. Use this when an "
                     "agent needs to inspect package linkage, build interim artifacts, infer deployment "
                     "metadata, install artifacts into the local Maven repository, or drive deploy-oriented "
                     "automation without manually composing `lein install` or `lein deploy` commands.")
   :inputSchema {:type "object"
                 :properties {"task" {:type "string"
                                      :description (str "The `code.tool.maven` operation to run. Supported values: "
                                                        "linkage, package, infer, clean, install, deploy, deploy-lein.")}
                              "target" {:type "string"
                                        :description (str "Optional EDN string naming the package target, namespace "
                                                          "prefix, or `:all`, such as `xyz.zcaudate/std.lib`, "
                                                          "`[xyz.zcaudate]`, or `:all`.")}
                              "options" {:type "string"
                                         :description (str "Optional EDN string for the Maven task options map, "
                                                           "for example `{:tag :all}` or `{:tag :all :write true}`. "
                                                           "Quiet print options are merged in by default.")}}
                 :required ["task"]}
   :implementation #'code-maven-fn})
