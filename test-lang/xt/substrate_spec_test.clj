 (ns xt.substrate-spec-test
   (:use code.test)
   (:require [hara.typed.xtalk-analysis :as xtalk-analysis]
             [hara.typed.xtalk-common :as types]))

 ^{:refer xt.substrate.base-space/create-space :added "4.1"}
 (fact "node space declarations mark trailing space args as optional"
   (do
     (types/clear-registry!)
     (let [fn-def (xtalk-analysis/resolve-function-def 'xt.substrate.base-space/create-space)]
       {:input-kinds (mapv (comp :kind :type) (:inputs fn-def))
        :output (types/type->data (:output fn-def))}))
   => '{:input-kinds [:primitive :maybe :maybe]
        :output {:kind :named :name xt.substrate.base-space/NodeSpace}})

 ^{:refer xt.substrate/request :added "4.1"}
 (fact "node main request declarations keep args and meta optional"
   (do
     (types/clear-registry!)
     (let [fn-def (xtalk-analysis/resolve-function-def 'xt.substrate/request)]
       {:input-kinds (mapv (comp :kind :type) (:inputs fn-def))
        :output (types/type->data (:output fn-def))}))
   => '{:input-kinds [:named :maybe :primitive :maybe :maybe]
        :output {:kind :keyword :name :xt/promise}})

 ^{:refer xt.substrate.base-router/add-subscription :added "4.1"}
 (fact "router declarations capture optional subscription id and meta"
   (do
     (types/clear-registry!)
     (let [fn-def (xtalk-analysis/resolve-function-def 'xt.substrate.base-router/add-subscription)]
       {:input-kinds (mapv (comp :kind :type) (:inputs fn-def))
        :output (types/type->data (:output fn-def))}))
   => '{:input-kinds [:primitive :primitive :maybe :primitive :maybe :maybe]
        :output {:kind :named :name xt.substrate.base-router/RouterSubscriptionEntry}})
