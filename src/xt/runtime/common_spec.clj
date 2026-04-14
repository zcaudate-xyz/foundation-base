(ns xt.runtime.common-spec
  (:require [std.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.common-spec :as xt]]})

(def +metatypes+
  {:runtime    {:var {:trigger {}
                      :watch   {}}}
   :iterator   {}
   :promise    {}
   :stream     {}
   :code       {:symbol  {}
                :keyword {}
                :syntax  {}}
   :collection {:list    {}
                :vector  {}
                :hashmap {}
                :hashset {}}
   :executor   {}})
