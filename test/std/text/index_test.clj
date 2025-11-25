(ns std.text.index-test
  (:use code.test)
  (:require [std.text.index :refer :all]))

(def +index+
  (let [idx (make-index)]
    (index-text idx "doc1" "hello world" 1)
    (index-text idx "doc2" "hello there" 1)
    (await idx)
    idx))

^{:refer std.text.index/make-index :added "3.0"}
(fact "creates a index for search"

  (class (make-index))
  => clojure.lang.Agent)

^{:refer std.text.index/add-entry :added "3.0"}
(fact "helper function for `index`"

  (let [idx (make-index)]
    (add-entry idx "a" "b" 1)
    (await idx)
    (-> @idx :data))
  => {"a" {"b" 1}})

^{:refer std.text.index/remove-entries :added "3.0"}
(fact "helper function for `unindex"

  (let [idx (make-index)]
    (add-entry idx "a" "b" 1)
    (add-entry idx "c" "b" 1)
    (await idx)
    (remove-entries idx "b" ["a"])
    (await idx)
    (-> @idx :data))
  => {"c" {"b" 1}})

^{:refer std.text.index/index-text :added "3.0"}
(fact "adds text to index"

  (-> @+index+ :data keys set)
  => #{"world" "hello"})

^{:refer std.text.index/unindex-text :added "3.0"}
(fact "removes text from index"

  (let [idx (make-index)]
    (index-text idx "doc1" "hello world" 1)
    (await idx)
    (unindex-text idx "doc1" "hello world")
    (await idx)
    (-> @idx :data))
  => {})

^{:refer std.text.index/unindex-all :added "3.0"}
(fact "clears index"

  (let [idx (make-index)]
    (index-text idx "doc1" "hello world" 1)
    (await idx)
    (unindex-all idx "doc1")
    (await idx)
    (-> @idx :data))
  => {})

^{:refer std.text.index/query :added "3.0"}
(fact "queries index for results"

  (query +index+ "hello")
  => {"hello" {"doc1" 1, "doc2" 1}})

^{:refer std.text.index/merge-and :added "3.0"}
(fact "merges results using and"

  (merge-and (query +index+ "hello world"))
  => {"doc1" 1})

^{:refer std.text.index/merge-or :added "3.0"}
(fact "merges results using or"

  (merge-or (query +index+ "hello world"))
  => {"doc1" 2, "doc2" 1})

^{:refer std.text.index/search :added "3.0"}
(fact "searchs index for text"

  (search +index+ "hello world" :and)
  => [["doc1" 1]]

  (search +index+ "hello world" :or)
  => [["doc1" 2] ["doc2" 1]])

^{:refer std.text.index/save-index :added "3.0"}
(fact "saves index to file"

  (let [idx (make-index)
        _ (index-text idx "doc1" "text" 1)
        _ (await idx)
        tmpfile (doto (java.io.File/createTempFile "test" ".idx")
                  .deleteOnExit)]
    (save-index idx tmpfile)
    (slurp tmpfile))
  => "{\"text\" {\"doc1\" 1}}\n")


^{:refer std.text.index/load-index :added "3.0"}
(fact "loads index from file"

  (let [idx (make-index)
        _ (index-text idx "doc1" "text" 1)
        _ (await idx)
        tmpfile (doto (java.io.File/createTempFile "test" ".idx")
                  .deleteOnExit)
        _ (save-index idx tmpfile)
        new-idx (make-index)]
    (load-index new-idx tmpfile)
    (await new-idx)
    (-> @new-idx :data))
  => {"text" {"doc1" 1}})

(comment
  (./import))
