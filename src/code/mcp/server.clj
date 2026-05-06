(ns code.mcp.server
  (:require [code.mcp.base.server :as base-server]
            [code.mcp.tool.basic :as basic]
            [code.mcp.tool.clj-eval :as clj-eval]
            [code.mcp.tool.code-doc :as code-doc]
            [code.mcp.tool.code-maven :as code-maven]
            [code.mcp.tool.code-manage :as code-manage]
            [code.mcp.tool.code-test :as code-test]
            [code.mcp.tool.form-heal :as form-heal]
            [code.mcp.tool.jvm-namespace :as jvm-namespace]
            [code.mcp.tool.hara-lang :as hara.lang]))

(defonce *server* (atom nil))

(defn default-instructions
  []
  (str "Use the project-specific tools before falling back to generic evaluation.\n\n"
       "- Use `code-test` for targeted `code.test` runs, namespace reloads, or rerunning errored tests.\n"
       "- Use `code-manage` for repository maintenance, scaffolding, grep/refactor, and namespace hygiene tasks.\n"
       "- Use `jvm-namespace` for live namespace inspection, reloads, clearing aliases/mappings, and in-memory inspection.\n"
       "- Use `lang-emit-as` and `hara.lang-*` for language inventories, emit probes, and module inspection.\n"
       "- Use `code-doc-*` for documentation template init/deploy/publish workflows.\n"
       "- Use `code-maven` for linkage, packaging, install, and deploy-oriented automation.\n"
       "- Use `clj-eval` only for focused probes that are not already covered by a higher-level project tool.\n\n"
       "All project tools prefer EDN input strings for symbols, vectors, sets, and option maps. Keep tasks targeted "
       "to the smallest namespace, site, or package set needed for the current autopilot step."))

(defn default-tools
  []
  [basic/echo-tool
   basic/ping-tool
   clj-eval/clj-eval-tool
   code-test/code-test-tool
   code-manage/manage-tool
   jvm-namespace/jvm-namespace-tool
   hara.lang/lang-emit-as-tool
   hara.lang/list-languages-tool
   hara.lang/list-modules-tool
   code-doc/init-template-tool
   code-doc/deploy-template-tool
   code-doc/publish-tool
   code-maven/code-maven-tool
   form-heal/list-edits-tool
   form-heal/get-dsl-deps-tool
   form-heal/refactor-directory-tool])

(defn create-server
  ([]
   (create-server {}))
  ([opts]
   (base-server/create-server
     (merge {:transport {:type :stdio}
             :server-info {:name "foundation-mcp" :version "0.1.0"}
             :instructions (default-instructions)
             :tools (default-tools)}
            opts))))

(defn start-server
  ([]
   (start-server {}))
  ([opts]
   (swap! *server*
          (fn [current]
            (or current
                (create-server opts))))))

(defn stop-server
  []
  (swap! *server*
         (fn [current]
           (when current
             (base-server/close! current))
           nil)))
