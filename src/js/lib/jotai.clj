(ns js.lib.jotai
  (:require [std.lang :as l]
            [std.lib.foundation :as f])
  (:refer-clojure :exclude [use val proxy]))

(l/script :js
  {:require [[js.react :as r]
             [js.lib.optics :as optics]]
   :import [["jotai" :as [* Jotai]]
            ["jotai/utils" :as [* JotaiUtils]]
            ["jotai" :as [* Jotai]]
            ["jotai/utils" :as [* JotaiUtils]]]})

(f/template-entries [l/tmpl-entry {:type :fragment
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

(f/template-entries [l/tmpl-entry {:type :fragment
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

