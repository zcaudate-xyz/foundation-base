(ns code.mcp.tool.code-doc-test
  (:require [code.mcp.tool.code-doc :refer :all])
  (:use code.test))

^{:refer code.mcp.tool.code-doc/init-template-fn :added "4.0"}
(fact "parses EDN input and delegates to code.doc/init-template"
  (let [calls (atom nil)]
    (with-redefs [code.doc/init-template (fn [site params]
                                           (reset! calls [site params]))]
      (let [result (init-template-fn nil {:site ":demo"
                                          :params "{:write true}"})]
        [@calls result])))
  => [[:demo {:write true}]
      {:content [{:type "text" :text "Initialized template for :demo"}]
       :isError false}])

^{:refer code.mcp.tool.code-doc/deploy-template-fn :added "4.0"}
(fact "parses EDN input and delegates to code.doc/deploy-template"
  (let [calls (atom nil)]
    (with-redefs [code.doc/deploy-template (fn [site params]
                                             (reset! calls [site params]))]
      (let [result (deploy-template-fn nil {:site ":demo"
                                            :params "{:write true}"})]
        [@calls result])))
  => [[:demo {:write true}]
      {:content [{:type "text" :text "Deployed template for :demo"}]
       :isError false}])

^{:refer code.mcp.tool.code-doc/publish-fn :added "4.0"}
(fact "parses EDN input and delegates to code.doc/publish"
  (let [calls (atom nil)]
    (with-redefs [code.doc/publish (fn [site params]
                                     (reset! calls [site params]))]
      (let [result (publish-fn nil {:site ":demo"
                                    :params "{:write true}"})]
        [@calls result])))
  => [[:demo {:write true}]
      {:content [{:type "text" :text "Published documentation for :demo"}]
       :isError false}])
