(ns js.lib.jotai
  (:require [std.lang :as l]
            [std.lib :as h])
  (:refer-clojure :exclude [use val proxy]))

(l/script :js
  {:bundle {:default [["jotai" :as [* Jotai]]
                      ["jotai/utils" :as [* JotaiUtils]]]}
   :import [["jotai" :as [* Jotai]]
            ["jotai/utils" :as [* JotaiUtils]]]
   :require [[js.react :as r]
             [js.lib.optics :as optics]]})

(h/template-entries [l/tmpl-entry {:type :fragment
                                   :base "JotaiUtils"
                                   :tag "js"}]
  [RESET
   atomFamily
   atomWithDefault
   atomWithLazy
   atomWithObservable
   atomWithReducer
   atomWithRefresh
   atomWithReset
   atomWithStorage
   createJSONStorage
   freezeAtom
   freezeAtomCreator
   loadable
   selectAtom
   splitAtom

   unwrap
   useAtomCallback
   useHydrateAtoms
   useReducerAtom
   useResetAtom])

(h/template-entries [l/tmpl-entry {:type :fragment
                                   :base "Jotai"
                                   :tag "js"}]
  [atom
   createStore
   getDefaultStore
   Provider
   useAtom
   useAtomValue
   useSetAtom
   useStore])

