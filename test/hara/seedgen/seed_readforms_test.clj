(ns hara.seedgen.form-parse-test
  (:use code.test)
  (:require [code.project :as project]
            [std.block.base :as block]
            [hara.seedgen.form-parse :as seed-readforms]))

(defn- summarize-block-items
  [m]
  (into {}
        (map (fn [k]
               [k (mapv (fn [{:keys [form line]}]
                          (block/block-string form))
                        (get m k))])
             [:root :derived :scaffold])))

(defn- summarize-checks
  [m]
  (into {}
        (map (fn [k]
               [k (mapv (fn [{:keys [form line expected]}]
                          [(block/block-string form)
                           (some-> expected :form block/block-string)])
                        (get m k))])
             [:root :derived :scaffold])))

^{:refer hara.seedgen.form-parse/class-empty :added "4.1"}
(fact "TODO")

^{:refer hara.seedgen.form-parse/class-explicit :added "4.1"}
(fact "TODO")

^{:refer hara.seedgen.form-parse/form-script? :added "4.1"}
(fact "TODO")

^{:refer hara.seedgen.form-parse/class-form :added "4.1"}
(fact "TODO")

^{:refer hara.seedgen.form-parse/class-navs :added "4.1"}
(fact "TODO")

^{:refer hara.seedgen.form-parse/class-merge :added "4.1"}
(fact "TODO")

^{:refer hara.seedgen.form-parse/check-arrow? :added "4.1"}
(fact "TODO")

^{:refer hara.seedgen.form-parse/check-classify :added "4.1"}
(fact "TODO")

^{:refer hara.seedgen.form-parse/fact-classify-meta :added "4.1"}
(fact "TODO")

^{:refer hara.seedgen.form-parse/fact-config-nav :added "4.1"}
(fact "TODO")

^{:refer hara.seedgen.form-parse/global-context :added "4.1"}
(fact "TODO")

^{:refer hara.seedgen.form-parse/entry-enrich :added "4.1"}
(fact "TODO")

^{:refer hara.seedgen.form-parse/seedgen-readforms :added "4.1"}
(fact "returns globals and analyse entries in the train-003 seedgen shape"
  (let [output (project/in-context
                (seed-readforms/seedgen-readforms 'xt.sample.train-003-test {}))
        entry  (get-in output '[:entries xt.lang.spec-base x:return-eval])]
    {:globals {:lang (get-in output [:globals :lang])
               :global-script {:root (some-> (get-in output [:globals :global-script :root])
                                             :form
                                             block/block-string)
                                :derived (mapv (fn [{:keys [form line]}]
                                                 (block/block-string form))
                                               (get-in output [:globals :global-script :derived]))}
               :global-fact-setup (summarize-block-items (get-in output [:globals :global-fact-setup]))
               :global-fact-teardown (summarize-block-items (get-in output [:globals :global-fact-teardown]))
               :global-top (summarize-block-items (get-in output [:globals :global-top]))}
     :entry {:fact-setup (summarize-block-items (:fact-setup entry))
             :fact-teardown (summarize-block-items (:fact-teardown entry))
             :checks (summarize-checks (:checks entry))}})
  => {:globals {:lang {:root :js
                       :derived []}
                 :global-script {:root "^{:seedgen/root     {:all true}}\n(l/script- :js\n  {:runtime :basic\n   :require [[xt.lang.spec-base :as xt]]})"
                                 :derived []}
                 :global-fact-setup {:root []
                                     :derived []
                                     :scaffold ["(l/rt:restart)"]}
                 :global-fact-teardown {:root []
                                        :derived []
                                        :scaffold ["(l/rt:stop)"]}
                 :global-top {:root []
                              :derived []
                              :scaffold []}}
      :entry {:fact-setup {:root []
                           :derived []
                           :scaffold []}
              :fact-teardown {:root []
                              :derived []
                              :scaffold []}
              :checks {:root [["^{:seedgen/base   {:lua    {:transform {\"1 + 1\" \"return 1 + 1\"}}\n                     :python {:suppress true}\n                     :dart   {:suppress true}}}\n   (!.js\n     (var encode-fn\n          (fn [value id key]\n            (return\n             (xt/x:return-encode value id key))))\n     (var wrap-fn\n          (fn [gen-fn wrap-fn]\n            (return\n             (xt/x:return-wrap gen-fn wrap-fn))))\n     (var eval-fn\n          (fn [s re-wrap-fn]\n            (return\n             (xt/x:return-eval s re-wrap-fn))))\n     (xt/x:json-decode\n      (eval-fn \"1 + 1\"\n               (fn [f]\n                 (return\n                  (wrap-fn f\n                           (fn [out]\n                             (return\n                              (encode-fn out \"id-A\" \"key-B\")))))))))"
                               "(contains-in {\"key\" \"key-B\", \"id\" \"id-A\", \"value\" 2, \"type\" \"data\"})"]]
                       :derived []
                       :scaffold []}}})
