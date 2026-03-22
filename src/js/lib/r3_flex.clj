(ns js.lib.r3-flex
  (:require [std.lang :as l]
            [std.lib.foundation :as f]))

(l/script :js
  {:import [["@react-three/flex" :as [* ReactThreeFlex]]]})

(f/template-entries [l/tmpl-entry {:type :fragment
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
