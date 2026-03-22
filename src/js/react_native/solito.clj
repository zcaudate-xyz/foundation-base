(ns js.react-native.solito
  (:require [std.lang :as l]
            [std.lib.foundation]))

(l/script :js
  {:import [["solito/router" :as [* SolRouter]]
            ["solito/link" :as [* SolLink]]
            ["solito/moti" :as [* SolMoti]]
            ["solito/image" :as [* SolImage]]
            ["solito" :as [* Solito]]]})

(std.lib.foundation/template-entries [l/tmpl-entry {:type :fragment
                                   :base "Solito"
                                   :tag "js"}]
  [createParam])

(std.lib.foundation/template-entries [l/tmpl-entry {:type :fragment
                                   :base "SolRouter"
                                   :tag "js"}]
  [useRouter])

(std.lib.foundation/template-entries [l/tmpl-entry {:type :fragment
                                   :base "SolMoti"
                                   :tag "js"}]
  [[Image SolitoImage]])

(std.lib.foundation/template-entries [l/tmpl-entry {:type :fragment
                                   :base "SolImage"
                                   :tag "js"}]
  [[Image SolitoImage]])

(std.lib.foundation/template-entries [l/tmpl-entry {:type :fragment
                                   :base "SolLink"
                                   :tag "js"}]
  [Link
   TextLink
   useLink])
