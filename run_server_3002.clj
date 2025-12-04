(ns run-server-custom
  (:require [mcp-clj.mcp-server.core :as mcp-server]
            [mcp-clj.tools.clj-eval :as clj-eval]
            [code.ai.server.tool.basic :as basic]
            [code.ai.server.tool.std-lang :as std-lang]
            [code.ai.server.tool.code-test :as code-test]
            [code.ai.server.tool.code-doc :as code-doc]
            [code.ai.server.tool.code-manage :as code-manage]))

(println "Starting server on 3002...")
(mcp-server/create-server
 {:transport {:type :sse :port 3002}
  :tools {"echo" basic/echo-tool
          "ping" basic/ping-tool
          "lang-emit-as" std-lang/lang-emit-as-tool
          "std-lang-list" std-lang/list-languages-tool
          "std-lang-modules" std-lang/list-modules-tool
          "clj-eval" clj-eval/clj-eval-tool
          "code-test" code-test/run-tests-tool
          "code-doc-init" code-doc/init-template-tool
          "code-doc-deploy" code-doc/deploy-template-tool
          "code-doc-publish" code-doc/publish-tool
          "code-manage" code-manage/manage-tool}})

(println "Server started on 3002")
@(promise)
