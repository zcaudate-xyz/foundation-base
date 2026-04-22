(ns std.lang.seedgen.form-common-test
  (:use code.test)
  (:require [std.block.base :as block]
            [std.block.navigate :as nav]
            [std.lang.seedgen.form-common :refer :all]))

(defn- sample-root
  []
  (nav/parse-root (str "^{:refer demo/example\n"
                       "  :setup [(!.js (+ 1 2))\n"
                       "          (!.lua (+ 1 2))]}\n"
                       "(fact \"demo\"\n"
                       "  (!.js (+ 1 2))\n"
                       "  => 3)\n\n"
                       "(def +k+ 1)\n")))

(defn- sample-top-navs
  []
  (nav-top-levels (sample-root)))

^{:refer std.lang.seedgen.form-common/target-normalize-langs :added "4.1"}
(fact "normalizes requested runtimes while preserving sentinel cases"
  [(target-normalize-langs :js)
   (target-normalize-langs '("js" :lua))
   (target-normalize-langs :all [:js :lua])
   (target-normalize-langs nil [:js :lua])
   (target-normalize-langs :all)]
  => [[:js]
      [:js :lua]
      [:js :lua]
      [:js :lua]
      :all])

^{:refer std.lang.seedgen.form-common/nav-meta-block? :added "4.1"}
(fact "detects metadata-wrapped top-level forms"
  (let [[fact-nav def-nav] (sample-top-navs)]
    [(nav-meta-block? fact-nav)
     (nav-meta-block? def-nav)])
  => [true false])

^{:refer std.lang.seedgen.form-common/nav-meta :added "4.1"}
(fact "returns the metadata map location when present"
  (let [[fact-nav] (sample-top-navs)]
    (some-> fact-nav
            nav-meta
            nav/block
            block/block-value))
  => '{:refer demo/example
       :setup [(!.js (+ 1 2))
               (!.lua (+ 1 2))]})

^{:refer std.lang.seedgen.form-common/nav-body :added "4.1"}
(fact "unwraps metadata forms to the underlying body form"
  (let [[fact-nav def-nav] (sample-top-navs)]
    [(-> fact-nav nav-body nav/value first)
     (-> def-nav nav-body nav/value first)])
  => '[fact def])

^{:refer std.lang.seedgen.form-common/nav-top-levels :added "4.1"}
(fact "collects each top-level form from the parsed root"
  (mapv #(-> % nav/block block/block-string)
        (sample-top-navs))
  => ["^{:refer demo/example\n  :setup [(!.js (+ 1 2))\n          (!.lua (+ 1 2))]}\n(fact \"demo\"\n  (!.js (+ 1 2))\n  => 3)"
      "(def +k+ 1)"])

^{:refer std.lang.seedgen.form-common/nav-entry :added "4.1"}
(fact "captures the original block and line metadata for a zipper location"
  (let [[fact-nav] (sample-top-navs)
        entry      (nav-entry fact-nav)]
    [(-> entry :form block/block-string)
     (select-keys (:line entry) [:row :col])])
  => ["^{:refer demo/example\n  :setup [(!.js (+ 1 2))\n          (!.lua (+ 1 2))]}\n(fact \"demo\"\n  (!.js (+ 1 2))\n  => 3)"
      {:row 1 :col 1}])

^{:refer std.lang.seedgen.form-common/nav-map-value :added "4.1"}
(fact "finds the value location for a key inside a map zipper"
  (let [[fact-nav] (sample-top-navs)]
    (some-> fact-nav
            nav-meta
            (nav-map-value :setup)
            nav/block
            block/block-value))
  => '[(!.js (+ 1 2))
       (!.lua (+ 1 2))])

^{:refer std.lang.seedgen.form-common/nav-vector-items :added "4.1"}
(fact "iterates vector items from left to right"
  (let [[fact-nav] (sample-top-navs)]
    (-> fact-nav
        nav-meta
        (nav-map-value :setup)
        nav-vector-items
        ((fn [items]
           (mapv #(-> % nav/block block/block-value) items)))))
  => '[(!.js (+ 1 2))
       (!.lua (+ 1 2))])

^{:refer std.lang.seedgen.form-common/item-line-key :added "4.1"}
(fact "reduces line maps to a sortable coordinate tuple"
  (item-line-key {:row 3 :col 4 :end-row 5 :end-col 6})
  => [3 4 5 6])

^{:refer std.lang.seedgen.form-common/item-form :added "4.1"}
(fact "returns the stored block for an item entry"
  (let [[fact-nav] (sample-top-navs)]
    (-> fact-nav nav-entry item-form block/block-string))
  => "^{:refer demo/example\n  :setup [(!.js (+ 1 2))\n          (!.lua (+ 1 2))]}\n(fact \"demo\"\n  (!.js (+ 1 2))\n  => 3)")

^{:refer std.lang.seedgen.form-common/item-line :added "4.1"}
(fact "returns the original line map for an item entry"
  (let [[fact-nav] (sample-top-navs)]
    (-> fact-nav nav-entry item-line (select-keys [:row :col])))
  => {:row 1 :col 1})

^{:refer std.lang.seedgen.form-common/item-string :added "4.1"}
(fact "renders an item block back to source text"
  (let [[fact-nav] (sample-top-navs)]
    (-> fact-nav nav-entry item-string))
  => "^{:refer demo/example\n  :setup [(!.js (+ 1 2))\n          (!.lua (+ 1 2))]}\n(fact \"demo\"\n  (!.js (+ 1 2))\n  => 3)")

^{:refer std.lang.seedgen.form-common/item-value :added "4.1"}
(fact "returns the EDN value represented by an item block"
  (let [[fact-nav] (sample-top-navs)]
    (-> fact-nav nav-entry item-value first))
  => 'fact)

^{:refer std.lang.seedgen.form-common/item-lang :added "4.1"}
(fact "infers the runtime from dispatch forms"
  (let [[fact-nav] (sample-top-navs)
        setup-item (-> fact-nav nav-meta (nav-map-value :setup) nav-vector-items second nav-entry)]
    (item-lang setup-item))
  => :lua)

^{:refer std.lang.seedgen.form-common/item-sort :added "4.1"}
(fact "sorts item entries by source position"
  (let [[fact-nav] (sample-top-navs)
        items      (-> fact-nav nav-meta (nav-map-value :setup) nav-vector-items)
        [js-item lua-item] (map nav-entry items)]
    (->> [lua-item js-item]
         item-sort
         (mapv item-lang)))
  => [:js :lua])

^{:refer std.lang.seedgen.form-common/item-classify-langs :added "4.1"}
(fact "flattens a classification map into source order"
  (let [[fact-nav] (sample-top-navs)
        items      (-> fact-nav nav-meta (nav-map-value :setup) nav-vector-items)
        [js-item lua-item] (map nav-entry items)]
    (->> {:root [js-item]
          :derived [lua-item]
          :scaffold []}
         item-classify-langs
         (mapv item-lang)))
  => [:js :lua])

^{:refer std.lang.seedgen.form-common/item-runtime-map :added "4.1"}
(fact "indexes classified runtime items by normalized runtime keyword"
  (let [[fact-nav] (sample-top-navs)
        items      (-> fact-nav nav-meta (nav-map-value :setup) nav-vector-items)
        [js-item lua-item] (map nav-entry items)]
    (->> {:root [js-item]
          :derived [lua-item]
          :scaffold []}
         item-runtime-map
         (reduce-kv (fn [out k v]
                      (assoc out k (item-string v)))
                    {})))
  => {:js "(!.js (+ 1 2))"
      :lua "(!.lua (+ 1 2))"})
