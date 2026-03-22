(ns rt.solidity.script.builtin
  (:require [std.lang :as l]
            [std.lib.foundation]
            [std.lib.template]
            [std.string.case]
            [std.string.common]
            [std.string.wrap])
  (:refer-clojure :exclude [assert require bytes]))

(l/script :solidity
  {:macro-only true})

(def +globals+
  '[abi.decode
    abi.encode
    abi.encodePacked
    abi.encodeWithSelector
    abi.encodeCall
    abi.encodeWithSignature
    bytes.concat
    string.concat
    block.basefee
    block.chainid
    block.coinbase
    block.difficulty
    block.gaslimit
    block.number
    block.timestamp
    gasleft
    msg.data
    msg.sender
    msg.sig
    msg.value
    tx.gasprice
    tx.origin
    assert
    require
    revert
    blockhash
    keccak256
    sha256
    ripemd160
    ecrecover
    addmod
    mulmod
    this
    super
    selfdestruct

    bytes
    string
    payable])

(defn- sol-fn-name-raw
  [name]
  (std.string.common/join "-" (map std.string.case/spear-case (std.string.common/split name #"\."))))

(def sol-fn-name (std.string.wrap/wrap sol-fn-name-raw))

(defn- sol-tmpl
  "creates fragments in builtin"
  {:added "4.0"}
  [sym]
  (std.lib.template/$ (def$.sol ~(sol-fn-name sym) ~sym)))

(std.lib.foundation/template-entries [sol-tmpl]
  +globals+)
