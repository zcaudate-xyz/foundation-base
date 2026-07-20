(ns js.react.view.polyfill
  "React-specific substrate view polyfills expressed as lower-level IR.

   Empty by default: every catalog component lowers natively to
   @xtalk/figma-ui or a DOM tag. Apps may pass their own polyfill map to
   `js.react.view.runtime/runtime-create` for components they do not
   implement natively."
  (:require [hara.lang :as l]))

(l/script :js
  {:require [[xt.lang.spec-base :as xt]]})

(defn.js registry
  []
  (return {}))
