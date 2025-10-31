(ns std.block.type
  (:require [std.protocol.block :as protocol.block]
            [std.block.base :as base]
            [std.block.check :as check]
            [std.block.value :as value]
            [std.string :as str])
  (:import (std.protocol.block IBlock
                               IBlockModifier
                               IBlockExpression
                               IBlockContainer)))

(def ^:dynamic *tab-width* 4)

(defn block-compare
  "compares equality of two blocks
 
   (block-compare (construct/void \\space)
                  (construct/void \\space))
   => 0"
  {:added "3.0"}
  ([^IBlock this ^IBlock other]
   (+ (.compareTo (base/block-tag this)
                  (base/block-tag other))
      (.compareTo (base/block-string this)
                  (base/block-string other)))))

(deftype VoidBlock [tag ^Character character width height]

  IBlock
  (_type     [_] :void)
  (_tag      [_] tag)
  (_string   [_] (str character))
  (_length   [_] 1)
  (_width    [_] width)
  (_height   [_] height)
  (_prefixed [_] 0)
  (_suffixed [_] 0)
  (_verify   [_] (and tag (= tag (check/void-tag character))))

  Comparable
  (compareTo [this other]
    (block-compare this other))

  Object
  (toString [_]
    (str (or (base/*void-representations* character) character))))

(defmethod print-method VoidBlock
  ([v ^java.io.Writer w]
   (.write w (str v))))

(defn void-block?
  "checks if the block is a void block
 
   (void-block? (construct/void))
   => true"
  {:added "3.0"}
  ([^IBlock block]
   (instance? VoidBlock block)))

(defn void-block
  "constructs a void block
 
   (-> (void-block :linespace \\tab 1 0)
       (base/block-info))
   => {:type :void, :tag :linespace, :string \"\\t\", :height 0, :width 1}"
  {:added "3.0"}
  ([tag ch width height]
   (->VoidBlock tag ch width height)))

(defn space-block?
  "checks if block is of type \\space
 
   (space-block? (construct/space))
   => true"
  {:added "3.0"}
  ([block]
   (and (void-block? block)
        (= (.-character ^VoidBlock block) \space))))

(defn linebreak-block?
  "checks if block is of type :linebreak
 
   (linebreak-block? (construct/newline))
   => true"
  {:added "3.0"}
  ([block]
   (and (void-block? block)
        (= :linebreak (base/block-tag block)))))

(defn linespace-block?
  "checks if block is of type :linespace
 
   (linespace-block? (construct/space))
   => true"
  {:added "3.0"}
  ([block]
   (and (void-block? block)
        (= :linespace (base/block-tag block)))))

(defn eof-block?
  "checks if input is an eof block
 
   (eof-block? (construct/void nil))
   => true"
  {:added "3.0"}
  ([block]
   (and (void-block? block)
        (= :eof (base/block-tag block)))))

(defn nil-void?
  "checks if block is nil or type void block
 
   (nil-void? nil) => true
 
   (nil-void? (construct/block nil)) => false
 
   (nil-void? (construct/space)) => true"
  {:added "3.0"}
  ([block]
   (or (nil? block)
       (void-block? block))))

(deftype CommentBlock [string width]

  IBlock
  (_type   [_] :comment)
  (_tag    [_] :comment)
  (_string [_] string)
  (_length [_] width)
  (_width  [_] width)
  (_height [_] 0)
  (_prefixed [_] 0)
  (_suffixed [_] 0)
  (_verify [_] (check/comment? string))

  Comparable
  (compareTo [this other]
    (block-compare this other))

  Object
  (toString [_] string))

(defmethod print-method CommentBlock
  ([v ^java.io.Writer w]
   (.write w (str v))))

(defn comment-block?
  "checks if the block is a token block
 
   (comment-block? (construct/comment \";;hello\"))
   => true"
  {:added "3.0"}
  ([block]
   (instance? CommentBlock block)))

(defn comment-block
  "constructs a comment block
 
   (-> (comment-block \";hello\")
       (base/block-info))
   => {:type :comment, :tag :comment, :string \";hello\", :height 0, :width 6}"
  {:added "3.0"}
  ([string]
   (->CommentBlock string (count string))))

(deftype TokenBlock [tag string value value-string width height]

  IBlock
  (_type   [_] :token)
  (_tag    [_] tag)
  (_string [_] string)
  (_length [_] (count string))
  (_width  [_] width)
  (_height [_] height)
  (_prefixed [_] 0)
  (_suffixed [_] 0)
  (_verify [_] (and tag (= tag (check/token-tag value))))

  IBlockExpression
  (_value  [_] value)
  (_value_string [_] value-string)

  Comparable
  (compareTo [this other]
    (block-compare this other))

  Object
  (toString [_] (pr-str value)))

(defmethod print-method TokenBlock
  ([v ^java.io.Writer w]
   (.write w (str v))))

(defn token-block?
  "checks if the block is a token block
 
   (token-block? (construct/token \"hello\"))
   => true"
  {:added "3.0"}
  ([block]
   (instance? TokenBlock block)))

(defn token-block
  "creates a token block
 
   (base/block-info (token-block :symbol \"abc\" 'abc \"abc\" 3 0))
   => {:type :token, :tag :symbol, :string \"abc\", :height 0, :width 3}"
  {:added "3.0"}
  ([tag string value value-string width height]
   (->TokenBlock tag string value value-string width height)))

(defn container-width
  "calculates the width of a container
 
   (container-width (construct/block [1 2 3 4]))
   => 9"
  {:added "3.0"}
  ([block]
   (loop [total (base/block-suffixed block)
          [child & more] (reverse (base/block-children block))]
     (cond (nil? child)
           (+ total
              (base/block-prefixed block))

           (linebreak-block? child)
           total

           (zero? (base/block-height child))
           (recur (+ total (base/block-width child))
                  more)

           (base/container? child)
           (+ total (base/block-width child))

           :else total))))

(defn container-height
  "calculates the height of a container
 
   (container-height (construct/block [(construct/newline)
                                       (construct/newline)]))
   => 2"
  {:added "3.0"}
  ([block]
   (reduce (fn [total child]
             (+ total (base/block-height child)))
           0
           (base/block-children block))))

(defmulti container-string
  "returns the string for the container
 
   (container-string (construct/block [1 2 3]))
   => \"[1 2 3]\""
  {:added "3.0"}
  base/block-tag)

(declare container-value-string)

(deftype ContainerBlock [tag children props]

  IBlock
  (_type     [_] :collection)
  (_tag      [_] tag)
  (_string   [this] (container-string this))
  (_length   [this] (count (base/block-string this)))
  (_width    [this] (container-width this))
  (_height   [this] (container-height this))
  (_prefixed [_] (count (:start props)))
  (_suffixed [_] (count (:end props)))

  Comparable
  (compareTo [this other]
    (block-compare this other))

  Object
  (toString [obj] (base/block-string obj))

  IBlockExpression
  (_value [block] ((:value props) block))
  (_value_string [block] (container-value-string block))

  IBlockContainer
  (_children [_] children)
  (_replace_children [_ children]
    (ContainerBlock. tag children props)))

(defn container-value-string
  "returns the string for 
 
   (container-value-string (construct/block [::a :b :c]))
   => \"[:std.block.type-test/a :b :c]\"
 
   (container-value-string (parse/parse-string \"[::a :b :c]\"))
   => \"[(keyword \\\":a\\\") (keyword \\\"b\\\") (keyword \\\"c\\\")]\""
  {:added "3.0"}
  ([^ContainerBlock block]
   (let [{:keys [start end]} (.props block)
         children (._children block)
         all  (->> (keep (fn [block]
                           (or (if (base/expression? block)
                                 (base/block-value-string block))
                               (if (base/modifier? block)
                                 (base/block-string block))))
                         children)
                   (str/join " "))]
     (str start all end))))

(defmethod container-string :root
  ([block]
   (->> (base/block-children block)
        (map base/block-string)
        (apply str))))

(defmethod container-string :default
  ([^ContainerBlock block]
   (let [{:keys [start end]} (.props block)
         children (._children block)
         all (apply str (map base/block-string children))]
     (str start all end))))

(defmethod print-method ContainerBlock
  ([v ^java.io.Writer w]
   (.write w (str v))))

(defn container-block?
  "checks if block is a container block
 
   (container-block? (construct/block []))
   => true"
  {:added "3.0"}
  ([block]
   (instance? ContainerBlock block)))

(defn container-block
  "constructs a container block
 
   (-> (container-block :fn [(construct/token '+)
                             (construct/void)
                             (construct/token '1)]
                        (construct/*container-props* :fn))
       (base/block-value))
   => '(fn* [] (+ 1))"
  {:added "3.0"}
  ([tag children props]
   (->ContainerBlock tag children props)))

(deftype ModifierBlock [tag string command]

  IBlock
  (_type     [_] :modifier)
  (_tag      [_] tag)
  (_string   [_] string)
  (_length   [_] (count string))
  (_width    [_] (count string))
  (_height   [_] 0)
  (_prefixed [_] 0)
  (_suffixed [_] 0)

  Comparable
  (compareTo [this other]
    (block-compare this other))

  Object
  (toString [obj] string)

  IBlockModifier
  (_modify [_ accumulator input]
    (command accumulator input)))

(defmethod print-method ModifierBlock
  ([v ^java.io.Writer w]
   (.write w (str v))))

(defn modifier-block?
  "checks if block is a modifier block
 
   (modifier-block? (construct/uneval))
   => true"
  {:added "3.0"}
  ([block]
   (instance? ModifierBlock block)))

(defn modifier-block
  "creates a modifier block, specifically #_
 
   (modifier-block :hash-uneval \"#_\" (fn [acc _] acc))"
  {:added "3.0"}
  ([tag string command]
   (->ModifierBlock tag string command)))

(defn code-block?
  [block]
  (or (token-block? block)
      (container-block? block)
      (modifier-block? block)))
