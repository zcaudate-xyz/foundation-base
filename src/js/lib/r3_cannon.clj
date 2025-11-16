(ns js.lib.r3-cannon
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :js
  {:import [["@react-three/cannon" :as [* ReactThreeCannon]]]})

(h/template-entries [l/tmpl-entry {:type :fragment
                                   :base "ReactThreeCannon"
                                   :tag "js"}]
  [Debug
   Physics
   useBox
   useCompoundBody
   useConeTwistConstraint
   useContactMaterial
   useConvexPolyhedron
   useCylinder
   useDistanceConstraint
   useHeightfield
   useHingeConstraint
   useLockConstraint
   useParticle
   usePlane
   usePointToPointConstraint
   useRaycastAll
   useRaycastAny
   useRaycastClosest
   useRaycastVehicle
   useSphere
   useSpring
   useTrimesh])
