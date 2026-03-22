(ns rt.solidity.script.builtin
  (:require [clojure.string]
            [std.lang :as l]
            [std.lib.foundation :as f]
            [std.lib.template :as template]
            [std.string.case :as case]
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
  (clojure.string/join "-" (map case/spear-case (clojure.string/split name #"\."))))

(def sol-fn-name (std.string.wrap/wrap sol-fn-name-raw))

(defn- sol-tmpl
  "creates fragments in builtin"
  {:added "4.0"}
  [sym]
  (template/$ (def$.sol ~(sol-fn-name sym) ~sym)))

(f/template-entries [sol-tmpl]
  +globals+)
