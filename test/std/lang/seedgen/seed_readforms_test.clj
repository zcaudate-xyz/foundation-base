(ns std.lang.seedgen.form-parse-test
  (:use code.test)
  (:require [code.project :as project]
            [std.block.base :as block]
            [std.lang.seedgen.form-parse :as seed-readforms]))

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

^{:refer std.lang.seedgen.form-parse/seedgen-readforms :added "4.1"}
(fact "returns globals and analyse entries in the train-002 seedgen shape"
  (let [output (project/in-context
                (seed-readforms/seedgen-readforms 'xt.sample.train-002-test {}))
        entry  (get-in output '[:entries xt.lang.spec-base for:array])]
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
                       :derived [:lua]}
                :global-script {:root "^{:seedgen/root         {:all true}}\n(l/script- :js\n  {:runtime :basic\n   :require [[xt.lang.spec-base :as xt]]})"
                                :derived ["(l/script- :lua\n  {:runtime :basic\n   :require [[xt.lang.spec-base :as xt]]})"]}
                :global-fact-setup {:root ["(!.js (+ 3 4 5))"]
                                    :derived ["(!.lua (+ 1 2 3))"]
                                    :scaffold ["(l/rt:restart)"]}
                :global-fact-teardown {:root []
                                        :derived []
                                        :scaffold ["(l/rt:stop)"]}
                :global-top {:root []
                             :derived []
                             :scaffold ["^{:seedgen/scaffold         {:python  {:suppress true}}}\n(l/script+ [:db :postgres])"
                                        "(def +a+ (inc 1))"
                                        "(l/! :db (+ 1 2 3))"]}}
      :entry {:fact-setup {:root ["(!.js (+ 1 2 3))"]
                           :derived ["^{:seedgen/derived   {:lang :lua}} ;; this is derived the meta is optional\n             (!.lua (+ 1 2 3))"]
                           :scaffold ["(def +a+ (+ 1 2 3))"]}
              :fact-teardown {:root ["(!.js (+ 1 2 3))"]
                              :derived []
                              :scaffold []}
              :checks {:root [["(!.js               ;; this is foundation\n    (var out [])\n    (xt/for:array [e [1 2 3 4]]\n      (when (> e 3)\n        (break))\n      (xt/x:arr-push out e))\n    out)"
                               "[1 2 3]"]]
                       :derived [["(!.lua              ;; this is derived and can be removed\n    (var out [])\n    (xt/for:array [e [1 2 3 4]]\n      (when (> e 3)\n        (break))\n      (xt/x:arr-push out e))\n    out)"
                                  "[1 2 3]"]]
                       :scaffold []}}})
