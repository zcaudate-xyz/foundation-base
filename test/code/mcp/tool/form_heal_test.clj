(ns code.mcp.tool.form-heal-test
  (:require [code.mcp.heal.form :as form]
            [code.mcp.tool.form-heal :as tool])
  (:use code.test))

^{:refer code.mcp.tool.form-heal/available-edits :added "4.1"}
(fact "lists known form healing edits"
  (set (tool/available-edits))
  => #{"fix:namespaced-symbol-no-dot"
       "fix:dash-indexing"
       "fix:set-arg-destructuring"
       "fix:remove-fg-extra-references"
       "fix:replace-fg-extra-namepspaces"
       "fix:remove-mistranslated-syms"})

^{:refer code.mcp.tool.form-heal/list-edits-fn :added "4.1"}
(fact "returns available edits in tool format"
  (tool/list-edits-fn nil nil)
  => (contains {:isError false
                :structuredContent map?
                :content vector?}))

^{:refer code.mcp.tool.form-heal/get-dsl-deps-fn :added "4.1"}
(fact "delegates get-dsl-deps through form heal module"
  (with-redefs [form/get-dsl-deps (fn [env]
                                    {:env env})]
    (tool/get-dsl-deps-fn nil {:root "/tmp/project"
                               :source-paths ["src"]})
    => {:content [{:type "text"
                   :text "{:env {:root \"/tmp/project\", :source-paths [\"src\"]}}"}]
        :structuredContent {:deps {:env {:root "/tmp/project"
                                         :source-paths ["src"]}}}
        :isError false}))

^{:refer code.mcp.tool.form-heal/refactor-directory-fn :added "4.1"}
(fact "resolves edits and delegates to refactor-directory"
  (with-redefs [form/refactor-directory (fn [env edits opts]
                                          {:env env
                                           :edits edits
                                           :opts opts})]
    (let [result (tool/refactor-directory-fn nil {:root "/tmp/project"
                                                  :source-paths ["src"]
                                                  :edits ["fix:dash-indexing"]
                                                  :write true})]
      (:isError result) => false
      (get-in result [:structuredContent :result :env])
      => {:root "/tmp/project" :source-paths ["src"]}
      (count (get-in result [:structuredContent :result :edits]))
      => 1
      (get-in result [:structuredContent :result :opts])
      => {:write true})))

^{:refer code.mcp.tool.form-heal/refactor-directory-fn :added "4.1"}
(fact "throws for unknown edits"
  (tool/refactor-directory-fn nil {:root "/tmp/project"
                                   :source-paths ["src"]
                                   :edits ["fix:not-real"]
                                   :write false})
  => (throws))
