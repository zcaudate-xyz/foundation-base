(ns code.mcp.embed-test
  (:require [code.mcp.embed :as embed]
            [code.test :refer :all]))

^{:refer code.mcp.embed/create-hash-embedder :added "4.1"}
(fact "hash embedder returns deterministic unit vectors"
  (let [provider (embed/create-hash-embedder {:dimensions 16})
        left (embed/embed-text provider "alpha beta")
        right (embed/embed-text provider "alpha beta")]
    [(count left)
     left
     (embed/cosine-similarity left right)])
  => [16 any 1.0])

^{:refer code.mcp.embed/cosine-similarity :added "4.1"}
(fact "related text ranks above unrelated text"
  (let [provider (embed/create-hash-embedder {:dimensions 32})
        query (embed/embed-text provider "clojure mcp server")
        close (embed/embed-text provider "clojure server wrapper")
        far (embed/embed-text provider "banana ocean")]
    (> (embed/cosine-similarity query close)
       (embed/cosine-similarity query far)))
  => true)
