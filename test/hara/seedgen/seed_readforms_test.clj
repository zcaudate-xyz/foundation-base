(ns hara.seedgen.form-parse-test
  (:use code.test)
  (:require [code.project :as project]
            [std.block.base :as block]
            [std.block.navigate :as nav]
            [hara.seedgen.form-common :as form-common]
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
(fact "returns an empty classification map"
  (seed-readforms/class-empty) => {:root [] :derived [] :scaffold []})

^{:refer hara.seedgen.form-parse/class-explicit :added "4.1"}
(fact "returns explicit seedgen classification from metadata"
  (seed-readforms/class-explicit (with-meta '(l/script- :js) {:seedgen/root true})) => :root
  (seed-readforms/class-explicit (with-meta '(l/script- :lua) {:seedgen/derived true})) => :derived
  (seed-readforms/class-explicit (with-meta '(+ 1 2 3) {:seedgen/scaffold true})) => :scaffold
  (seed-readforms/class-explicit '(+ 1 2 3)) => nil)

^{:refer hara.seedgen.form-parse/form-script? :added "4.1"}
(fact "recognizes hara.lang/script- forms"
  (seed-readforms/form-script? #{'hara.lang/script-} '(hara.lang/script- :js {})) => true
  (seed-readforms/form-script? #{'hara.lang/script-} '(+ 1 2 3)) => false
  (seed-readforms/form-script? #{'l/script-} '(l/script- :js {})) => true)

^{:refer hara.seedgen.form-parse/class-form :added "4.1"}
(fact "classifies forms as root, derived or scaffold"
  (seed-readforms/class-form :js #{'hara.lang/script-} '(hara.lang/script- :js {})) => :root
  (seed-readforms/class-form :js #{'hara.lang/script-} '(hara.lang/script- :lua {})) => :derived
  (seed-readforms/class-form :js #{'hara.lang/script-} '(!.js 1)) => :root
  (seed-readforms/class-form :js #{'hara.lang/script-} '(!.lua 1)) => :derived
  (seed-readforms/class-form :js #{'hara.lang/script-} '(+ 1 2 3)) => :scaffold)

^{:refer hara.seedgen.form-parse/class-navs :added "4.1"}
(fact "classifies a sequence of navigable forms"
  (let [text "(hara.lang/script- :js {:runtime :basic})\n\n(!.js 1)\n\n(+ 1 2 3)"
        root (nav/parse-root text)
        navs (form-common/nav-top-levels root)]
    (seed-readforms/class-navs :js #{'hara.lang/script-} navs)
    => (contains {:root (fn [v] (= 2 (count v)))
                  :derived []
                  :scaffold (fn [v] (= 1 (count v)))})))

^{:refer hara.seedgen.form-parse/class-merge :added "4.1"}
(fact "merges two classifications by concatenating entries"
  (seed-readforms/class-merge {:root [1] :derived [2] :scaffold [3]}
                              {:root [4] :derived [5] :scaffold [6]})
  => {:root [1 4] :derived [2 5] :scaffold [3 6]})

^{:refer hara.seedgen.form-parse/check-arrow? :added "4.1"}
(fact "recognizes assertion arrow symbols"
  (seed-readforms/check-arrow? '=>) => true
  (seed-readforms/check-arrow? '=>*) => true
  (seed-readforms/check-arrow? '+) => false)

^{:refer hara.seedgen.form-parse/check-classify :added "4.1"}
(fact "classifies fact checks by runtime language"
  (let [text "(fact \"hello\"\n  (!.js 1)\n  => 1\n\n  (!.lua 2)\n  => 2)"
        root (nav/parse-root text)
        fact-nav (nav/down root)]
    (seed-readforms/check-classify :js #{'hara.lang/script-} fact-nav)
    => (contains {:root (fn [v] (= 1 (count v)))
                  :derived (fn [v] (= 1 (count v)))
                  :scaffold []})))

^{:refer hara.seedgen.form-parse/fact-classify-meta :added "4.1"}
(fact "classifies setup and teardown metadata on a fact"
  (let [text "^{:setup [(!.js (setup))]\n  :teardown [(!.lua (tear))]}\n(fact \"hello\"\n  (!.js 1)\n  => 1)"
        root (nav/parse-root text)
        fact-nav (nav/down root)]
    (seed-readforms/fact-classify-meta :js #{'hara.lang/script-} fact-nav)
    => (contains {:fact-setup (contains {:root (fn [v] (= 1 (count v)))
                                         :derived []
                                         :scaffold []})
                  :fact-teardown (contains {:root []
                                            :derived (fn [v] (= 1 (count v)))
                                            :scaffold []})})))

^{:refer hara.seedgen.form-parse/fact-config-nav :added "4.1"}
(fact "navigates to the config map inside a fact:global form"
  (let [text "(fact:global {:setup [(!.js (setup))]})"
        root (nav/parse-root text)
        fact-nav (first (form-common/nav-top-levels root))]
    (some-> (seed-readforms/fact-config-nav fact-nav)
            nav/value)
    => {:setup '[(!.js (setup))]}))

^{:refer hara.seedgen.form-parse/global-context :added "4.1"}
(fact "builds global context for a set of top-level forms"
  (let [text (str "(ns sample\n"
                  "  (:use code.test)\n"
                  "  (:require [hara.lang :as l]))\n\n"
                  "^{:seedgen/root {:all true :langs [:js :lua]}}\n"
                  "(l/script- :js {:runtime :basic})\n\n"
                  "(l/script- :lua {:runtime :basic})\n\n"
                  "^{:refer sample/hello :added \"4.1\"}\n"
                  "(fact \"hello\"\n"
                  "  (!.js 1)\n"
                  "  => 1)\n")
        root (nav/parse-root text)
        top-navs (form-common/nav-top-levels root)
        forms (mapv nav/value top-navs)
        context (seed-readforms/global-context forms top-navs)]
    context
    => (contains {:lang {:root :js
                         :derived [:lua]}
                  :global-script map?
                  :global-fact-setup (contains {:root [] :derived [] :scaffold []})
                  :global-fact-teardown (contains {:root [] :derived [] :scaffold []})})))

^{:refer hara.seedgen.form-parse/entry-enrich :added "4.1"}
(fact "enriches an entry with fact classification"
  (let [text "^{:setup [(!.js (setup))]}\n(fact \"hello\"\n  (!.js 1)\n  => 1)"
        root (nav/parse-root text)
        fact-nav (nav/down root)
        entry {}]
    (seed-readforms/entry-enrich :js #{'hara.lang/script-} fact-nav entry)
    => (contains {:checks map?
                  :fact-setup map?
                  :fact-teardown map?})))

^{:refer hara.seedgen.form-parse/seedgen-readforms :added "4.1"}
(fact "returns globals and analyse entries in the train-002 seedgen shape"
  (let [project (assoc (project/project)
                       :test-paths (vec (distinct (concat (:test-paths (project/project))
                                                          ["test-data"]))))
        lookup  (project/file-lookup project)
        output  (seed-readforms/seedgen-readforms 'xt.sample.train-002-test {} lookup project)
        entry   (get-in output '[:entries xt.lang.spec-base for:array])]
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
