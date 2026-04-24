(ns std.lang.base.grammar-xtalk-system-test
  (:use code.test)
  (:require [std.lang.base.grammar-xtalk-system :refer :all]))

^{:refer std.lang.base.grammar-xtalk-system/xtalk-entry? :added "4.1"}
(fact "detects xtalk grammar entries"
  (xtalk-entry? {:op :x-add})
  => true

  (xtalk-entry? [:x-add])
  => false)

^{:refer std.lang.base.grammar-xtalk-system/xtalk-profiles :added "4.1"}
(fact "returns xtalk profiles in grammar order"
  (xtalk-profiles)
  => [:xtalk-common
      :xtalk-functional
      :xtalk-language-specific
      :xtalk-std-lang-link-specific
      :xtalk-runtime-specific])

^{:refer std.lang.base.grammar-xtalk-system/xtalk-areas :added "4.1"}
(fact "returns xtalk implementation areas in order"
  (xtalk-areas)
  => [:common
      :functional
      :language-specific
      :std-lang-link-specific
      :runtime-specific])

^{:refer std.lang.base.grammar-xtalk-system/xtalk-area-profiles :added "4.1"}
(fact "returns profiles for an xtalk area"
  (xtalk-area-profiles :common)
  => [:xtalk-common]

  (xtalk-area-profiles :unknown)
  => [])

^{:refer std.lang.base.grammar-xtalk-system/xtalk-profile-ops :added "4.1"}
(fact "returns ops for a profile"
  (let [ops (xtalk-profile-ops :xtalk-common)]
    [(contains? ops :x-add)
     (contains? ops :x-obj-keys)])
  => [true true])

^{:refer std.lang.base.grammar-xtalk-system/xtalk-op-profiles :added "4.1"}
(fact "returns profiles for an op"
  (xtalk-op-profiles :x-add)
  => #{:xtalk-common})

^{:refer std.lang.base.grammar-xtalk-system/xtalk-op-entry :added "4.1"}
(fact "returns the full xtalk entry for an op"
  (select-keys (xtalk-op-entry :x-add) [:op :symbol :emit])
  => '{:op :x-add
       :symbol #{x:add}
       :emit :macro})

^{:refer std.lang.base.grammar-xtalk-system/xtalk-symbol-op :added "4.1"}
(fact "maps symbols back to xtalk ops"
  (xtalk-symbol-op 'x:add)
  => :x-add)

^{:refer std.lang.base.grammar-xtalk-system/xtalk-symbol-entry :added "4.1"}
(fact "maps symbols back to xtalk entries"
  (-> (xtalk-symbol-entry 'x:add)
      (select-keys [:op :emit]))
  => '{:op :x-add
       :emit :macro})

^{:refer std.lang.base.grammar-xtalk-system/xtalk-op-requires :added "4.1"}
(fact "returns direct required xtalk ops"
  (xtalk-op-requires :x-arr-map)
  => #{})

^{:refer std.lang.base.grammar-xtalk-system/xtalk-op-closure :added "4.1"}
(fact "returns transitive op requirements including the op"
  (xtalk-op-closure :x-arr-map)
  => #{:x-arr-map})

^{:refer std.lang.base.grammar-xtalk-system/xtalk-ops-profiles :added "4.1"}
(fact "returns the combined profile set for ops"
  (xtalk-ops-profiles #{:x-add :x-arr-map})
  => #{:xtalk-common
       :xtalk-functional})

^{:refer std.lang.base.grammar-xtalk-system/scan-xtalk :added "4.1"}
(fact "scans xtalk usage and linked polyfill modules"
  (scan-xtalk '(do (x:obj-keys data)
                   (x:arr-map items f)
                   (x:str-ends-with s suffix)))
  => '{:ops #{:x-obj-keys
              :x-arr-map
              :x-str-ends-with}
       :symbols #{x:obj-keys
                  x:arr-map
                  x:str-ends-with}
       :profiles #{:xtalk-common
                   :xtalk-functional}
       :polyfill-modules #{xt.lang.common-data
                           xt.lang.common-string}}

  (scan-xtalk '(do (tmpl:call v))
              {'tmpl:call {:emit :hard-link}})
  => '{:ops #{}
       :symbols #{}
       :profiles #{}
       :polyfill-modules #{}
       :template? true})

^{:refer std.lang.base.grammar-xtalk-system/xtalk-grammar-supported-ops :added "4.1"}
(fact "returns supported ops from reserved grammar entries"
  (xtalk-grammar-supported-ops
   {:reserved {'x:add {:op :x-add}
               'x:arr-map {:op :x-arr-map}
               'foo {:op :foo}}})
  => #{:x-add
       :x-arr-map})

^{:refer std.lang.base.grammar-xtalk-system/xtalk-grammar-supported-profiles :added "4.1"}
(fact "returns fully supported profiles for a grammar"
  (let [reserved (into {}
                       (map (fn [op]
                              [(symbol "xtest" (name op))
                               {:op op}])
                            (xtalk-profile-ops :xtalk-common)))]
    (xtalk-grammar-supported-profiles {:reserved reserved}))
  => [:xtalk-common])

^{:refer std.lang.base.grammar-xtalk-system/xtalk-grammar-missing-profiles :added "4.1"}
(fact "returns required profiles not fully supported by a grammar"
  (let [reserved (into {}
                       (map (fn [op]
                              [(symbol "xtest" (name op))
                               {:op op}])
                            (xtalk-profile-ops :xtalk-common)))]
    (xtalk-grammar-missing-profiles {:reserved reserved}
                                    [:xtalk-common :xtalk-functional]))
  => #{:xtalk-functional})

^{:refer std.lang.base.grammar-xtalk-system/xtalk-library-profiles :added "4.1"}
(fact "returns required profiles for new xtalk library namespaces"
  (xtalk-library-profiles 'xtalk.lib.db.sql)
  => #{:xtalk-common
       :xtalk-runtime-specific})

^{:refer std.lang.base.grammar-xtalk-system/xtalk-unclassified-ops :added "4.1"}
(fact "returns xtalk ops not assigned to any profile"
  (xtalk-unclassified-ops)
  => [])
