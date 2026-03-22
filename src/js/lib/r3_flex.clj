(ns js.lib.r3-flex
  (:require [std.lang :as l]
            [std.lib.foundation]))

(l/script :js
  {:import [["@react-three/flex" :as [* ReactThreeFlex]]]})

(std.lib.foundation/template-entries [l/tmpl-entry {:type :fragment
                                   :base "ReactThreeFlex"
                                   :tag "js"}]
  [Box
   Flex
   useContext
   useFlexNode
   useFlexSize
   useReflow
   useSetSize
   useSyncGeometrySize])
