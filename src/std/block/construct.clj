(ns std.block.construct
  (:require [std.block.base :as base]
            [std.block.check :as check]
            [std.block.type :as type]
            [std.block.value :as value]
            [std.lib.collection :as c]
            [std.string :as str])
  (:refer-clojure :exclude [empty newline comment]))

(def ^:dynamic *tags* {:void       (set (keys check/*void-checks*))
                       :token      (set (keys check/*token-checks*))
                       :collection (set (keys check/*collection-checks*))
                       :comment    #{:comment}
                       :meta       #{:meta :hash-meta}
                       :cons       #{:var
                                     :deref
                                     :quote
                                     :select
                                     :select-splice
                                     :unquote
                                     :unquote-splice}
                       :literal    #{:hash-token :hash-keyword}
                       :macro      #{:fn :hash-eval :syntax}
                       :modifier   #{:hash-uneval}})

(defonce +space+
  (type/void-block :linespace \space 1 0))

(defonce +newline+
  (type/void-block :linebreak \newline 0 1))

(defonce +return+
  (type/void-block :linebreak \return 0 1))

(defonce +formfeed+
  (type/void-block :linebreak \formfeed 0 1))

(def void-lookup
  {\space    +space+
   \newline  +newline+
   \return   +return+
   \formfeed +formfeed+})

(defn void
  "creates a void block
 
   (str (void))
   => \"␣\""
  {:added "3.0"}
  ([] +space+)
  ([ch]
   (or (void-lookup ch)
       (let [tag  (check/void-tag ch)
             width (case tag
                     :eof       0
                     :linebreak 0
                     :linetab type/*tab-width*
                     1)
             height (if (= tag :linebreak) 1 0)]
         (if tag
           (type/void-block tag ch width height)
           (throw (ex-info "Not a valid void character." {:input ch})))))))

(defn space
  "creates a space block
 
   (str (space))
   => \"␣\""
  {:added "3.0"}
  ([] +space+))

(defn spaces
  "creates multiple space blocks
 
   (apply str (spaces 5))
   => \"␣␣␣␣␣\""
  {:added "3.0"}
  ([n] (repeat n +space+)))

(defn tab
  "creates a tab
 
   (str (tab))
   => \"\\\t\""
  {:added "3.0"}
  ([] (void \tab)))

(defn tabs
  "creates multiple tabs
 
   (apply str (tabs 5))
   => \"\\\t\\\t\\\t\\\t\\\t\""
  {:added "3.0"}
  ([n]
   (repeat n (tab))))

(defn newline
  "creates a newline
 
   (str (newline))
   => \"\\\n\""
  {:added "3.0"}
  ([] +newline+))

(defn newlines
  "creates multiple newlines
 
   (apply str (newlines 5))
   => \"\\\n\\\n\\\n\\\n\\\n\""
  {:added "3.0"}
  ([n] (repeat n +newline+)))

(defn newline?
  "checks if a block is a newline
 
   (newline? (newline))
   => true"
  {:added "4.0"}
  ([block]
   (and (type/void-block? block)
        (= :linebreak (base/block-tag block)))))

(defn comment
  "creates a comment block
 
   (str (comment \";hello\"))
   => \";hello\""
  {:added "3.0"}
  ([s]
   (if (check/comment? s)
     (type/comment-block s)
     (throw (ex-info "Not a valid comment string." {:input s})))))

(defn token-dimensions
  "returns the dimensions of the token
 
   (token-dimensions :regexp \"#\\\"hello\\\nworld\\\"\")
   => [15 0]
 
   (token-dimensions :regexp \"#\\\"hello\\nworld\\\"\")
   => [6 1]"
  {:added "3.0"}
  ([tag string]
   (cond (nil? tag)
         (throw (ex-info "Not a valid token." {:input string}))

         (= tag :regexp)
         (let [lines (str/split-lines string)]
           [(count (last lines)) (dec (count lines))])
         
         :else
         [(count string) 0])))

(defn string-token
  "constructs a string token
 
   (str (string-token \"hello\"))
   => \"\\\"hello\\\"\"
 
   (str (string-token \"hello\\nworld\"))
   => \"\\\"hello\\\nworld\\\"\""
  {:added "3.0"}
  ([form]
   (let [tag     :string
         lines   (str/split-lines form)
         height  (dec (max 1 (count lines)))
         width   (cond-> (inc (count (last lines)))
                   (zero? height) inc)
         string  (str "\"" form "\"")]
     (type/token-block tag string form string width height))))


(defn token
  "creates a token
 
   (str (token 'abc))
   => \"abc\""
  {:added "3.0"}
  ([form]
   (let [tag (check/token-tag form)]
     (cond (= :string tag)
           (string-token form)

           :else
           (let [string (pr-str form)
                 [width height] (token-dimensions tag string)]
             (type/token-block tag string form string width height))))))

(defn token-from-string
  "creates a token from a string input
 
   (str (token-from-string \"abc\"))
   => \"abc\""
  {:added "3.0"}
  ([string]
   (let [value (read-string string)]
     (token-from-string string value string)))
  ([string value value-string]
   (let [tag  (check/token-tag value)
         [width height] (token-dimensions tag string)]
     (type/token-block tag string value (or value-string string) width height))))

(def ^:dynamic *container-props*
  (merge-with merge
              base/*container-limits*
              value/*container-values*))

(defn container-checks
  "performs checks for the container"
  {:added "3.0"}
  ([tag children props]
   (let [cs    (:cons props)
         _     (if (nil? tag)
                 (throw (ex-info "Not a valid container tag." {:input tag})))
         vals  (if cs (filter base/expression? children))
         _     (if (and cs (not= cs (count vals)))
                 (throw (ex-info "Invalid children." {:tag tag
                                                      :cons cs
                                                      :values vals})))]
     true)))

(defn container
  "creates a container
 
   (str (container :list [(void) (void)]))
   => \"(  )\""
  {:added "3.0"}
  ([tag children]
   (let [props (get *container-props* tag)
         _    (container-checks tag children props)]
     (type/container-block tag children props))))

(defn uneval
  "creates a hash-uneval block
 
   (str (uneval))
   => \"#_\""
  {:added "3.0"}
  ([]
   (type/modifier-block :hash-uneval "#_" (fn [accumulator input] accumulator))))

(defn cursor
  "creates a cursor for the navigator
 
   (str (cursor))
   => \"|\""
  {:added "3.0"}
  ([]
   (type/modifier-block :hash-cursor "|" conj)))

(defmulti construct-collection
  "constructs a collection
 
   (str (construct-collection [1 2 (void) (void) 3]))
   => \"[1 2  3]\""
  {:added "3.0"}
  check/collection-tag)

(defn construct-children
  "constructs the children 
 
   (mapv str (construct-children [1 (newline) (void) 2]))
   => [\"1\" \"\\\n\" \"␣\" \"2\"]"
  {:added "3.0"}
  ([form]
   (loop [[elem & more :as form] form
          out []]
     (cond (empty? form)
           out

           (type/void-block? elem)
           (recur more (conj out elem))

           (not (or (empty? out)
                    (type/void-block? (last out))
                    (type/modifier-block? (last out))))
           (recur form (conj out (space)))

           (type/comment-block? elem)
           (let [nelem (first more)]
             (if (and (type/void-block? nelem)
                      (zero? (compare nelem +newline+)))
               (recur more (conj out elem))
               (recur more (conj out elem +newline+))))

           (base/block? elem)
           (recur more (conj out elem))

           (check/collection? elem)
           (recur more (conj out (construct-collection elem)))

           (check/token? elem)
           (recur more (conj out (token elem)))

           :else
           (throw (ex-info "Invalid block input." {:input form}))))))

(defmethod construct-collection :list
  ([form]
   (let [children (construct-children form)]
     (container :list children))))

(defmethod construct-collection :vector
  ([form]
   (let [metadata (meta form) 
         tag      (or (:tag metadata)
                      (first
                       (keys metadata)))
         #_#_tag      (if (contains? base/*container-limits* tag)
                        tag)
         children (construct-children form)]
     (container (or tag :vector) children))))

(defmethod construct-collection :set
  ([form]
   (let [children (construct-children (vec form))]
     (container :set children))))

(defmethod construct-collection :map
  ([form]
   (let [children (->> (sort form)
                       (mapcat identity)
                       (construct-children))]
     (container :map children))))

(defn block
  "creates a block
 
   (base/block-info (block 1))
   => {:type :token, :tag :long, :string \"1\", :height 0, :width 1}
 
   (str (block [1 (newline) (void) 2]))
   => \"[1\\n 2]\""
  {:added "3.0"}
  ([elem]
   (cond (base/block? elem)
         elem

         (check/token? elem)
         (token elem)

         (check/collection? elem)
         (construct-collection elem)

         :else
         (throw (ex-info "Invalid input." {:input elem})))))

(defn add-child
  "adds a child to a container block
 
   (-> (block [])
       (add-child 1)
       (add-child 2)
       (str))
   => \"[12]\""
  {:added "3.0"}
  ([container elem]
   (base/replace-children container
                          (conj (vec (base/block-children container))
                                (block elem)))))

(defn empty
  "constructs empty list
 
   (str (empty))
   => \"()\""
  {:added "3.0"}
  ([]
   (container :list [])))

(defn root
  "constructs a root block"
  {:added "3.0"}
  ([children]
   (block (container :root (construct-children children)))))

(defn contents
  "reads out the contents of a container
 
   (contents (block [1 2 3]))
   => '[1 ␣ 2 ␣ 3]"
  {:added "3.0"}
  ([block]
   (if (base/expression? block)
     (-> (base/block-children block)
         pr-str
         read-string)
     (-> block
         pr-str
         read-string))))

(defn max-width
  "gets the max width of given block, along with starting offset"
  {:added "3.0"}
  ([block]
   (max-width block 0))
  ([block offset]
   (cond (= 0 (base/block-height block))
         (base/block-width block)
         
         :else
         (let [counts (->> (base/block-string block)
                           (str/split-lines)
                           (mapv count))
               counts (if (zero? offset)
                        counts
                        (update counts 0 + offset))]
           
           (- (apply max counts)
              offset)))))

(defn line-width
  "get the width at the line which it finishes"
  {:added "4.0"}
  ([block]
   (line-width block 0))
  ([block offset]
   (cond (= 0 (base/block-height block))
         (base/block-width block)
         
         :else
         (- (base/block-width block) offset))))

(defn rep
  "returns the clojure forms representation of the dsl"
  {:added "4.0"}
  [block]
  (read-string (pr-str block)))

(defn get-lines
  "gets the lines for a block"
  {:added "4.0"}
  [block]
  (->> (base/block-string block)
       (str/split-lines)))

;;
;; indentation
;;

(defn line-split
  "splits a block collection into"
  {:added "4.0"}
  [block]
  (let [lines  (->> (base/block-children block)
                    (c/split-by newline?))
        lines  (mapv (fn [line]
                       (mapv (fn [block]
                               (if (or (not (type/container-block? block))
                                       (= 0 (base/block-height block)))
                                 block
                                 (line-split block)))
                             line))
                     lines)]
    {:tag   (base/block-tag block)
     :lines lines}))

(defn line-restore
  "restores a dom rep, adding body indentation"
  {:added "4.0"}
  [dom offset]
  (let [{:keys [tag lines]} dom
        process-fn (fn [line offset]
                     (mapv (fn [elem]
                             (if (map? elem)
                               (line-restore elem offset)
                               elem))
                           line))
        children (reduce (fn [out nline]
                           (-> (reduce (fn [out _] (conj out (space)))
                                       (conj out (newline))
                                       (range offset))
                               (concat (process-fn nline offset))
                               (vec)))
                         
                         (process-fn (first lines) 0)
                         (rest lines))]
    (container tag children)))

(defn indent-body
  "indents the body given offset"
  {:added "4.0"}
  ([block offset]
   (cond (or (not (type/container-block? block))
             (= 0 (base/block-height block)))
         block
         
         :else
         (-> (line-split block)
             (line-restore offset)))))
