(ns js.lib.r3-base
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :js
  {:bundle  {:default [["@react-three/fiber" :as [* ReactThree]]]
             :lena    [["leva" :as  [* ReactLena]]]}})

(h/template-entries [l/tmpl-entry {:type :fragment
                                   :base "ReactThree"
                                   :tag "js"}]
  [Canvas
   ReactThreeFiber
   _roots
   act
   addAfterEffect
   addEffect
   addTail
   advance
   applyProps
   context
   createEvents
   createPortal
   createRoot
   dispose
   events
   extend
   flushGlobalEffects
   getRootState
   invalidate
   reconciler
   render
   unmountComponentAtNode
   useFrame
   useGraph
   useInstanceHandle
   useLoader
   useStore
   useThree])

(h/template-entries [l/tmpl-entry {:type :fragment
                                   :base "ReactLena"
                                   :tag "js"}]
  [useControls])



