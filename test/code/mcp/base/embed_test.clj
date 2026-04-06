(ns code.mcp.base.embed-test
  (:require [code.mcp.base.embed :as embed]
            [code.test :refer :all]))

^{:refer code.mcp.base.embed/create-hash-embedder :added "4.1"}
(fact "hash embedder returns deterministic unit vectors"
  (let [provider (embed/create-hash-embedder {:dimensions 16})
        left (embed/embed-text provider "alpha beta")
        right (embed/embed-text provider "alpha beta")]
    [(count left)
     (boolean (some pos? left))
     (embed/cosine-similarity left right)])
  => [16 true 1.0])

^{:refer code.mcp.base.embed/cosine-similarity :added "4.1"}
(fact "related text ranks above unrelated text"
  (let [provider (embed/create-hash-embedder {:dimensions 32})
        query (embed/embed-text provider "clojure mcp server")
        close (embed/embed-text provider "clojure server wrapper")
        far (embed/embed-text provider "banana ocean")]
    (> (embed/cosine-similarity query close)
       (embed/cosine-similarity query far)))
  => true)

^{:refer code.mcp.base.embed/tokenize :added "4.1"}
(fact "tokenizes normalized alphanumeric words"
  (embed/tokenize "Hello, MCP_server-2!")
  => ["hello" "mcp_server-2"])

^{:refer code.mcp.base.embed/l2-norm :added "4.1"}
(fact "calculates vector magnitude"
  (embed/l2-norm [3 4])
  => 5.0)

^{:refer code.mcp.base.embed/unit-vector :added "4.1"}
(fact "normalizes non-zero vectors and preserves zero vectors"
  [(embed/unit-vector [3 4])
   (embed/unit-vector [0 0 0])]
  => [[0.6 0.8]
      [0 0 0]])

^{:refer code.mcp.base.embed/token->bucket :added "4.1"}
(fact "maps tokens into bounded dimensions"
  (let [bucket (embed/token->bucket 8 "alpha")]
    [(integer? bucket)
     (<= 0 bucket)
     (< bucket 8)])
  => [true true true])

^{:refer code.mcp.base.embed/embed-texts :added "4.1"}
(fact "supports function embedding providers"
  (embed/embed-texts count ["alpha" "β"])
  => [5 1])

^{:refer code.mcp.base.embed/embed-text :added "4.1"}
(fact "embeds a single text via the shared multi-text API"
  (embed/embed-text (fn [text] {:text text}) "hello")
  => {:text "hello"})
