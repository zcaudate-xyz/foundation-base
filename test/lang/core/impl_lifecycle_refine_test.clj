(ns ^{:clj-kondo/config '{:linters {:unresolved-symbol {:level :off}}}}
  lang.core.impl-lifecycle-refine-test
  (:require [clojure.string :as string]
            [lang.core.compile :as compile]
            [lang.core.impl :as impl]
            [lang.core.impl-lifecycle :as lifecycle])
  (:use code.test))

(def +library+
  (let [lib (impl/clone-default-library)]
    (impl/with:library [lib]
      (require '[js.react] :reload))
    lib))

^{:refer lang.core.impl-lifecycle/emit-module-prep
  :id treeshakes-selected-module-entries
  :added "4.1"}
(fact "treeshakes selected entries and their same-module dependencies"
  (impl/with:library [+library+]
    (let [refine '{js.react {:treeshake true
                             :ensure [useInterval runIntervalStart]}}
          link {:path-suffix ".js" :root-prefix "#app"}
          opts {:lang :js :emit {:code {:refine refine :link link}}}
          [_ full] (lifecycle/emit-module-prep 'js.react {:lang :js})
          selected-prep (lifecycle/emit-module-prep 'js.react opts)
          selected (second selected-prep)
          [_ unlisted] (lifecycle/emit-module-prep 'xt.lang.common-lib opts)
          [_ unlisted-full] (lifecycle/emit-module-prep 'xt.lang.common-lib {:lang :js})
          links (compile/compile-module-create-links (conj (:direct selected) 'js.react)
                                                     'lang-demos.tui-000-counter.main
                                                     link)
          imports (lifecycle/emit-module-setup-link-arr
                   (assoc-in opts [:emit :compile]
                             {:type :directory :links links})
                   selected-prep)
          output (lifecycle/emit-module-setup 'js.react
                                              (assoc-in opts [:emit :lang/format] :commonjs))]
      {:full-count (count (:code full))
       :selected (set (map :id (:code selected)))
       :native (set (keys (:native selected)))
       :unlisted-unchanged (= (set (map :id (:code unlisted)))
                              (set (map :id (:code unlisted-full))))
       :link (some #(string/includes? % "#app/libs/xt/lang/common-lib.js") imports)
       :exports {:use-interval (string/includes? output "useInterval")
                 :run-start (string/includes? output "runIntervalStart")
                 :dependency (string/includes? output "useFollowRef")
                 :unused (string/includes? output "useTimeout")
                 :unused-native (string/includes? output "react-dom/client")}}))
  => {:full-count 32
      :selected '#{useInterval runIntervalStart runIntervalStop useFollowRef}
      :native #{"react"}
      :unlisted-unchanged true
      :link true
      :exports {:use-interval true
                :run-start true
                :dependency true
                :unused false
                :unused-native false}})
