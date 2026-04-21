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
                          [(block/block-string form) line])
                        (get m k))])
             [:root :derived :scaffold])))

(defn- summarize-checks
  [m]
  (into {}
        (map (fn [k]
               [k (mapv (fn [{:keys [form line expected]}]
                          [(block/block-string form)
                           line
                           (some-> expected :form block/block-string)
                           (some-> expected :line)])
                        (get m k))])
             [:root :derived :scaffold])))

^{:refer std.lang.seedgen.form-parse/seedgen-readforms :added "4.1"}
(fact "returns globals and analyse entries in the train-002 seedgen shape"
  (let [output (project/in-context
                (seed-readforms/seedgen-readforms 'xt.sample.train-002-test {}))
        entry  (get-in output '[:entries xt.lang.common-spec for:array])]
    {:globals {:lang (get-in output [:globals :lang])
               :global-script {:root (some-> (get-in output [:globals :global-script :root])
                                             ((fn [{:keys [form line]}]
                                                [(block/block-string form) line])))
                               :derived (mapv (fn [{:keys [form line]}]
                                                [(block/block-string form) line])
                                              (get-in output [:globals :global-script :derived]))}
               :global-fact-setup (summarize-block-items (get-in output [:globals :global-fact-setup]))
               :global-fact-teardown (summarize-block-items (get-in output [:globals :global-fact-teardown]))
               :global-top (summarize-block-items (get-in output [:globals :global-top]))}
     :entry {:fact-setup (summarize-block-items (:fact-setup entry))
             :fact-teardown (summarize-block-items (:fact-teardown entry))
             :checks (summarize-checks (:checks entry))}})
  => {:globals {:lang {:root :js
                       :derived [:lua]}
                :global-script {:root ["^{:seedgen/root         {:all true}}\n(l/script- :js\n  {:runtime :basic\n   :require [[xt.lang.common-spec :as xt]]})"
                                       {:row 9 :col 1 :end-row 12 :end-col 45}]
                                :derived [["(l/script- :lua\n  {:runtime :basic\n   :require [[xt.lang.common-spec :as xt]]})"
                                           {:row 14 :col 1 :end-row 16 :end-col 45}]]}
                :global-fact-setup {:root [["(!.js (+ 3 4 5))"
                                            {:row 20 :col 11 :end-row 20 :end-col 27}]]
                                    :derived [["(!.lua (+ 1 2 3))"
                                               {:row 21 :col 11 :end-row 21 :end-col 28}]]
                                    :scaffold [["(l/rt:restart)"
                                                {:row 19 :col 11 :end-row 19 :end-col 25}]]}
                :global-fact-teardown {:root []
                                       :derived []
                                       :scaffold [["(l/rt:stop)"
                                                   {:row 23 :col 14 :end-row 23 :end-col 25}]]}
                :global-top {:root []
                             :derived []
                             :scaffold [["^{:seedgen/scaffold         {:python true}}\n(l/script+ [:db :postgres])"
                                         {:row 6 :col 1 :end-row 7 :end-col 28}]
                                        ["(def +a+ (inc 1))"
                                         {:row 26 :col 1 :end-row 26 :end-col 18}]
                                        ["(l/! :db (+ 1 2 3))"
                                         {:row 28 :col 1 :end-row 28 :end-col 20}]]}}
      :entry {:fact-setup {:root [["(!.js (+ 1 2 3))"
                                   {:row 32 :col 14 :end-row 32 :end-col 30}]]
                           :derived [["^{:seedgen/derived   {:lua true}} ;; this is derived the meta is optional\n             (!.lua (+ 1 2 3))"
                                      {:row 33 :col 14 :end-row 34 :end-col 31}]]
                           :scaffold [["(def +a+ (+ 1 2 3))"
                                       {:row 31 :col 14 :end-row 31 :end-col 33}]]}
              :fact-teardown {:root [["(!.js (+ 1 2 3))"
                                      {:row 35 :col 14 :end-row 35 :end-col 30}]]
                              :derived []
                              :scaffold []}
              :checks {:root [["(!.js               ;; this is foundation\n    (var out [])\n    (xt/for:array [e [1 2 3 4]]\n      (when (> e 3)\n        (break))\n      (xt/x:arr-push out e))\n    out)"
                               {:row 38 :col 3 :end-row 44 :end-col 9}
                               "[1 2 3]"
                               {:row 45 :col 6 :end-row 45 :end-col 13}]]
                       :derived [["(!.lua              ;; this is derived and can be removed\n    (var out [])\n    (xt/for:array [e [1 2 3 4]]\n      (when (> e 3)\n        (break))\n      (xt/x:arr-push out e))\n    out)"
                                  {:row 48 :col 3 :end-row 54 :end-col 9}
                                  "[1 2 3]"
                                  {:row 55 :col 6 :end-row 55 :end-col 13}]]
                       :scaffold []}}})
