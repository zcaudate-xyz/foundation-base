(ns js.lib.react-spring
  (:require [std.lang :as l]
            [std.lib.foundation]))

(l/script :js
  {:import [["@react-spring/native" :as [* ReactSpring]]
            ["@react-spring/web" :as [* ReactSpring]]]})

(std.lib.foundation/template-entries [l/tmpl-entry {:type :fragment
                                   :base "ReactSpring"
                                   :tag "js"}]
    [useChain
     useSpring
     useSprings
     useTrail
     useTransition
     animated
     Spring
     SpringContext
     SpringRef
     SpringValue
     [SpringTrail Trail]
     [SpringTransition Transition]
     [SpringController Controller]])
