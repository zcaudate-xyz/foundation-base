(ns js.lib.r3-flex
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :js
  {:import [["@react-three/flex" :as [* ReactThreeFlex]]]})

(h/template-entries [l/tmpl-entry {:type :fragment
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
