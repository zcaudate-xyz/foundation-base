(ns js.lib.react-spring
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :js
  {:bundle  {:native [["@react-spring/native" :as [* ReactSpring]]]
             :web    [["@react-spring/web" :as [* ReactSpring]]]}})

(h/template-entries [l/tmpl-entry {:type :fragment
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
