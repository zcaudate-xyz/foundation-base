(ns std.block.construct-test
  (:use code.test)
  (:require [std.block.construct :refer :all]
            [std.block.base :as base])
  (:refer-clojure :exclude [comment newline empty]))

^{:refer std.block.construct/void :added "3.0"}
(fact "creates a void block"

  (str (void))
  => "␣")

^{:refer std.block.construct/space :added "3.0"}
(fact "creates a space block"

  (str (space))
  => "␣")

^{:refer std.block.construct/spaces :added "3.0"}
(fact "creates multiple space blocks"

  (apply str (spaces 5))
  => "␣␣␣␣␣")

^{:refer std.block.construct/tab :added "3.0"}
(fact "creates a tab"

  (str (tab))
  => "\\t")

^{:refer std.block.construct/tabs :added "3.0"}
(fact "creates multiple tabs"

  (apply str (tabs 5))
  => "\\t\\t\\t\\t\\t")

^{:refer std.block.construct/newline :added "3.0"}
(fact "creates a newline"

  (str (newline))
  => "\\n")

^{:refer std.block.construct/newlines :added "3.0"}
(fact "creates multiple newlines"

  (apply str (newlines 5))
  => "\\n\\n\\n\\n\\n")

^{:refer std.block.construct/newline? :added "4.0"}
(fact "checks if a block is a newline"

  (newline? (newline))
  => true)

^{:refer std.block.construct/comment :added "3.0"}
(fact "creates a comment block"

  (str (comment ";hello"))
  => ";hello")

^{:refer std.block.construct/token-dimensions :added "3.0"}
(fact "returns the dimensions of the token"

  (token-dimensions :regexp "#\"hello\\nworld\"")
  => [15 0]

  (token-dimensions :regexp "#\"hello\nworld\"")
  => [6 1])

^{:refer std.block.construct/string-token :added "3.0"}
(fact "constructs a string token"

  (str (string-token "hello"))
  => "\"hello\""

  (str (string-token "hello\nworld"))
  => "\"hello\\nworld\"")

^{:refer std.block.construct/token :added "3.0"}
(fact "creates a token"

  (str (token 'abc))
  => "abc")

^{:refer std.block.construct/token-from-string :added "3.0"}
(fact "creates a token from a string input"

  (str (token-from-string "abc"))
  => "abc")

^{:refer std.block.construct/container-checks :added "3.0"}
(fact "performs checks for the container")

^{:refer std.block.construct/container :added "3.0"}
(fact "creates a container"

  (str (container :list [(void) (void)]))
  => "(  )")

^{:refer std.block.construct/uneval :added "3.0"}
(fact "creates a hash-uneval block"

  (str (uneval))
  => "#_")

^{:refer std.block.construct/cursor :added "3.0"}
(fact "creates a cursor for the navigator"

  (str (cursor))
  => "|")

^{:refer std.block.construct/construct-collection :added "3.0"}
(fact "constructs a collection"

  (str (construct-collection [1 2 (void) (void) 3]))
  => "[1 2  3]")

^{:refer std.block.construct/construct-children :added "3.0"}
(fact "constructs the children "

  (mapv str (construct-children [1 (newline) (void) 2]))
  => ["1" "\\n" "␣" "2"])

^{:refer std.block.construct/block :added "3.0"}
(fact "creates a block"

  (base/block-info (block 1))
  => {:type :token, :tag :long, :string "1", :height 0, :width 1}

  (str (block [1 (newline) (void) 2]))
  => "[1\n 2]")

^{:refer std.block.construct/add-child :added "3.0"}
(fact "adds a child to a container block"

  (-> (block [])
      (add-child 1)
      (add-child 2)
      (str))
  => "[12]")

^{:refer std.block.construct/empty :added "3.0"}
(fact "constructs empty list"

  (str (empty))
  => "()")

^{:refer std.block.construct/root :added "3.0"}
(fact "constructs a root block"
  ^:hidden
  
  (str (root '[a b]))
  => "a b")

^{:refer std.block.construct/contents :added "3.0"}
(fact "reads out the contents of a container"

  (contents (block [1 2 3]))
  => '[1 ␣ 2 ␣ 3])

^{:refer std.block.construct/max-width :added "3.0"}
(fact "gets the max width of given block, along with starting offset"
  ^:hidden
  
  [(max-width
    (parse/parse-string ""))
   (base/block-info
    (parse/parse-string ""))]
  => [0 {:type :void, :tag :eof, :string "", :height 0, :width 0}]

  [(max-width
    (parse/parse-root ""))
   (base/block-info
    (parse/parse-root ""))]
  => [0 {:type :collection, :tag :root, :string "", :height 0, :width 0}]  
  
  [(max-width
   (parse/parse-string "\"\n\""))
   (base/block-info
    (parse/parse-string "\"\n\""))]
  => [1 {:type :token, :tag :string, :string "\"\n\"", :height 1, :width 1}]
  
  (base/block-info
   (parse/parse-string "\"\n\n\""))
  => {:type :token, :tag :string, :string "\"\n\n\"", :height 2, :width 1}
  
  (base/block-info
   (block [:very-long-line (newline) (void) 2]))
  => {:type :collection, :tag :vector, :string "[:very-long-line\n 2]", :height 1, :width 3}

  
  ;; longest line at the start
  (max-width
   (block [:very-long-line (newline) (void) 2]))
  => 16

  (max-width
   (block [:very-long-line (newline) (void) 2])
   10)
  => 16

    
  ;; longest line in the middle
  (max-width
   (block [(newline)
           (void) :very-long-line (newline)  (void) 2]))
  => 16

  (max-width
   (block [(newline)
           (void) :very-long-line (newline)  (void) 2])
   10)
  => 6)

^{:refer std.block.construct/rep :added "4.0"}
(fact "returns the clojure forms representation of the dsl"
  
  (rep (block [1 2 3 4]))
  => [1 2 3 4])

^{:refer std.block.construct/line-split :added "4.0"}
(fact "splits a block collection into "
  ^:hidden

  (str (line-split
        (block [(newline)
                (void) :very-long-line (newline)  (void) 2])
        ))
  => "{:tag :vector, :lines [[] [␣ :very-long-line] [␣ 2]]}"
  
  (rep
   (line-split
    (block [(newline) 1 (newline)
            [(newline) 2 (newline)
             [(newline) 3 (newline)]]])))
  => {:tag :vector, :lines [[] [1] [{:tag :vector, :items [[] [2] [{:tag :vector, :items [[] [3]]}]]}]]})

^{:refer std.block.construct/line-restore :added "4.0"}
(fact "restores a dom rep, adding body indentation"
  ^:hidden

  (mapv (fn [offset]
          (str (line-restore
                (line-split
                 (block [(newline)
                         (void) :very-long-line (newline)  (void) 2]))
                offset)))
        [0 2 5 10])
  => ["[\n :very-long-line\n 2]"
      "[\n   :very-long-line\n   2]"
      "[\n      :very-long-line\n      2]"
      "[\n           :very-long-line\n           2]"]

  (mapv (fn [offset]
          (str (line-restore
                (line-split
                 (parse/parse-string "(defn add \n  []\n  (+ x y))" ))
                offset)))
        [0 2 5 10])
  => ["(defn add \n  []\n  (+ x y))"
      "(defn add \n    []\n    (+ x y))"
      "(defn add \n       []\n       (+ x y))"
      "(defn add \n            []\n            (+ x y))"])

^{:refer std.block.construct/indent-body :added "4.0"}
(fact "indents the body given offset"
  ^:hidden
  
  (str 
   (indent-body
    (block [1 2 (newline) 3 4])
    5))
  => "[1 2\n     3 4]")
