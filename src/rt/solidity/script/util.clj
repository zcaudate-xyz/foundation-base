(ns rt.solidity.script.util
  (:require [std.lang :as l])
  (:refer-clojure :exclude [assert require bytes]))

(l/script :solidity
  rt.solidity
  {:require [[rt.solidity.script.builtin :as s]]
   })

(l/intern-macros :solidity
                 'rt.solidity.script.builtin
                 'rt.solidity)

(defn.sol ^{:- [:public :pure]
            :static/returns [:bool]}
  ut:str-comp
  "compares two strings together"
  {:added "4.0"}
  [:string :memory a
   :string :memory b]
  (var (:bytes :memory abytes) (s/bytes a))
  (var (:bytes :memory bbytes) (s/bytes b))
  (return (and (== (. abytes length)
                   (. bbytes length))
               (== (s/keccak256 abytes)
                   (s/keccak256 bbytes)))))

(comment
  (l/get-book (l/default-library)
              :solidity)
    )

