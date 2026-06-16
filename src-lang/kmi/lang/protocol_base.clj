(ns kmi.lang.protocol-base
  (:require [hara.lang :as l]
            [xt.lang.common-protocol :as proto])
  (:refer-clojure :exclude [assoc dissoc empty find first hash key keys name
                            namespace next nth pop push reduce rest seq to-array val vals]))

(l/script :xtalk
  {:require [[xt.lang.common-protocol :as proto]]})

(proto/defprotocol.xt IAssoc
  (assoc [impl key val]))

(proto/defprotocol.xt IAssocMutable
  (assoc-mutable [impl key val]))

(proto/defprotocol.xt IColl
  (to-iter [impl])
  (to-array [impl]))

(proto/defprotocol.xt IDissoc
  (dissoc [impl key]))

(proto/defprotocol.xt IDissocMutable
  (dissoc-mutable [impl key]))

(proto/defprotocol.xt IEdit
  (is-mutable [impl])
  (to-mutable [impl])
  (is-persistent [impl])
  (to-persistent [impl]))

(proto/defprotocol.xt IEmpty
  (empty [impl]))

(proto/defprotocol.xt IEq
  (eq [impl o]))

(proto/defprotocol.xt IFind
  (find [impl key]))

(proto/defprotocol.xt IHash
  (hash [impl]))

(proto/defprotocol.xt ILookup
  (keys [impl])
  (vals [impl])
  (lookup [impl key default-val]))

(proto/defprotocol.xt INamespaced
  (name [impl])
  (namespace [impl]))

(proto/defprotocol.xt INth
  (nth [impl idx]))

(proto/defprotocol.xt IPair
  (key [impl])
  (val [impl]))

(proto/defprotocol.xt IPop
  (pop [impl]))

(proto/defprotocol.xt IPopMutable
  (pop-mutable [impl]))

(proto/defprotocol.xt IPush
  (push [impl x]))

(proto/defprotocol.xt IPushMutable
  (push-mutable [impl x]))

(proto/defprotocol.xt IShow
  (show [impl]))

(proto/defprotocol.xt ISize
  (size [impl]))

;;
;; Protocols (in dependency order)
;;
