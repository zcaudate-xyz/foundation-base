(ns std.block.base
  (:require [std.protocol.block :as protocol.block]
            [std.block.check :as check])
  (:import (std.protocol.block IBlock
                               IBlockModifier
                               IBlockExpression
                               IBlockContainer)))

(def ^:dynamic *block-types*
  #{:void
    :token
    :comment
    :container
    :modifier})

(def ^:dynamic *container-tags*
  {:collection (set (keys check/*collection-checks*))
   :meta       #{:meta :hash-meta}
   :cons       #{:var
                 :deref
                 :quote
                 :select
                 :select-splice
                 :unquote
                 :unquote-splice}
   :literal   #{:hash-token
                :hash-keyword}
   :macro     #{:syntax
                :fn
                :hash-eval}})

(def ^:dynamic *block-tags*
  (merge *container-tags*
         {:void       (set (keys check/*void-checks*))
          :token      (set (keys check/*token-checks*))
          :comment    #{:comment}
          :modifier   #{:hash-uneval}}))

(def ^:dynamic *void-representations*
  {nil       :eof
   \space    "␣"
   \newline  "\\n"
   \return   "\\r"
   \tab      "\\t"
   \formfeed "\\f"})

(def ^:dynamic *container-limits*
  {:root           {:start ""    :end ""}
   :fn             {:start "#("  :end ")"}
   :list           {:start "("   :end ")"}
   :map            {:start "{"   :end "}"}
   :set            {:start "#{"  :end "}"}
   :vector         {:start "["   :end "]"}
   :deref          {:start "@"   :end "" :cons 1}
   :meta           {:start "^"   :end "" :cons 2}
   :quote          {:start "'"   :end "" :cons 1}
   :syntax         {:start "`"   :end "" :cons 1}
   :var            {:start "#'"  :end "" :cons 1}
   :hash-keyword   {:start "#"   :end "" :cons 2}
   :hash-token     {:start "#"   :end "" :cons 2}
   :hash-meta      {:start "#^"  :end "" :cons 2}
   :hash-eval      {:start "#="  :end "" :cons 1}
   :select         {:start "#?"  :end "" :cons 1}
   :select-splice  {:start "#?@" :end "" :cons 1}
   :unquote        {:start "~"   :end "" :cons 1}
   :unquote-splice {:start "~@"  :end "" :cons 1}})

(defn block?
  "check whether an object is a block
 
   (block? (construct/void nil))
   => true
 
   (block? (construct/token \"hello\"))
   => true"
  {:added "3.0"}
  ([obj]
   (instance? IBlock obj)))

(defn ^clojure.lang.Keyword block-type
  "returns block type as keyword
 
   (block-type (construct/void nil))
   => :void
 
   (block-type (construct/token \"hello\"))
   => :token"
  {:added "3.0"}
  ([^IBlock block]
   (._type block)))

(defn ^clojure.lang.Keyword block-tag
  "returns block tag as keyword
 
   (block-tag (construct/void nil))
   => :eof
 
   (block-tag (construct/void \\space))
   => :linespace"
  {:added "3.0"}
  ([^IBlock block]
   (._tag block)))

(defn ^String block-string
  "returns block string as representated in the file
 
   (block-string (construct/token 3/4))
   => \"3/4\"
 
   (block-string (construct/void \\space))
   => \"\""
  {:added "3.0"}
  ([^IBlock block]
   (._string block)))

(defn ^Number block-length
  "return the length of the string
 
   (block-length (construct/void))
   => 1
 
   (block-length (construct/block [1 2 3 4]))
   => 9"
  {:added "3.0"}
  ([^IBlock block]
   (._length block)))

(defn ^Number block-width
  "returns the width of a block
 
   (block-width (construct/token 'hello))
   => 5"
  {:added "3.0"}
  ([^IBlock block]
   (._width block)))

(defn ^Number block-height
  "returns the height of a block
 
   (block-height (construct/block
                  ^:list [(construct/newline)
                          (construct/newline)]))
   => 2"
  {:added "3.0"}
  ([^IBlock block]
   (._height block)))

(defn ^Number block-prefixed
  "returns the length of the starting characters
 
   (block-prefixed (construct/block #{}))
   => 2"
  {:added "3.0"}
  ([^IBlock block]
   (._prefixed block)))

(defn ^Number block-suffixed
  "returns the length of the ending characters
 
   (block-suffixed (construct/block #{}))
   => 1"
  {:added "3.0"}
  ([^IBlock block]
   (._suffixed block)))

(defn ^Boolean block-verify
  "checks that the block has correct data"
  {:added "3.0"}
  ([^IBlock block]
   (._verify block)))

(defn expression?
  "checks if the block has a value associated with it
 
   (expression? (construct/token 1.2))
   => true"
  {:added "3.0"}
  ([^IBlock block]
   (instance? IBlockExpression block)))

(defn block-value
  "returns the value of the block
 
   (block-value (construct/token 1.2))
   => 1.2"
  {:added "3.0"}
  ([^IBlockExpression block]
   (._value block)))

(defn ^String block-value-string
  "returns the string for which a value will be generated
 
   (block-value-string (parse/parse-string \"#(+ 1 ::2)\"))
   => \"#(+ 1 (keyword \\\":2\\\"))\""
  {:added "3.0"}
  ([^IBlockExpression block]
   (._value_string block)))

(defn modifier?
  "checks if the block is of type INodeModifier
 
   (modifier? (construct/uneval))
   => true"
  {:added "3.0"}
  ([^IBlock block]
   (instance? IBlockModifier block)))

(defn block-modify
  "allows the block to modify an accumulator
 
   (block-modify (construct/uneval) [1 2] 'ANYTHING)
   => [1 2]"
  {:added "3.0"}
  ([^IBlockModifier block accumulator input]
   (._modify block accumulator input)))

(defn container?
  "determines whether a block has children
 
   (container? (parse/parse-string \"[1 2 3]\"))
   => true
 
   (container? (parse/parse-string \" \"))
   => false"
  {:added "3.0"}
  ([^IBlock block]
   (instance? IBlockContainer block)))

(defn block-children
  "returns the children in the block
 
   (->> (block-children (parse/parse-string \"[1   2]\"))
        (apply str))
   => \"1␣␣␣2\""
  {:added "3.0"}
  ([^IBlockContainer block]
   (._children block)))

(defn replace-children
  "replace childer
 
   (->> (replace-children (construct/block [])
                          (conj (vec (block-children (construct/block [1 2])))
                                (construct/void \\space)
                                (construct/block [3 4])))
        str)
   => \"[1 2 [3 4]]\""
  {:added "3.0"}
  ([^IBlockContainer block children]
   (._replace_children block children)))

(defn block-info
  "returns the data associated with the block
 
   (block-info (construct/token true))
   => {:type :token, :tag :boolean, :string \"true\", :height 0, :width 4}
   => {:type :token, :tag :boolean, :string \"true\"}
 
   (block-info (construct/void \\tab))
   => {:type :void, :tag :linetab, :string \"\\t\", :height 0, :width 4}"
  {:added "3.0"}
  ([^IBlock block]
   {:type     (block-type block)
    :tag      (block-tag block)
    :string   (block-string block)
    :height   (block-height block)
    :width    (block-width block)}))
