(ns js.lib.jotai
  (:require [std.lang :as l]
            [std.lib :as h])
  (:refer-clojure :exclude [use val proxy]))

(l/script :js
  {:macro-only true
   :bundle {:default [["jotai" :as [* Jotai]]
                      ["jotai/utils" :as [* JotaiUtils]]]}
   :import [["jotai" :as [* Jotai]]
            ["jotai/utils" :as [* JotaiUtils]]]
   :require [[js.react :as r]
             [js.lib.optics :as optics]]
   :export [MODULE]})

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

(defn.js useListenNested
  [baseAtom path]
  (var p (r/useMemo
          (optics/compose (:.. path))
          [path]))
  (var [state] (-/useAtom baseAtom))
  (return
   (:? (:- state :instanceOf Promise)
       (. state (then (fn [value] (return (optics/preview p value)))))
       (optics/preview p state))))

(defn.js useNested
  [baseAtom path]
  (var p (r/useMemo
          (optics/compose (:.. path))
          [path]))
  (var setter (r/useCallback
                 (fn [])))
  )



(!.js
    (var v {:a {:b {:c {:d 1}}}})
    (var p (optics/compose "a" "b"))
    (optics/preview p v)
    (optics/modify p
                   (fn [] (return "CHANGED"))
                   v))

(def.js MODULE (!:module))



