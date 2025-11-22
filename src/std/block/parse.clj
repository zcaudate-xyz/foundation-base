(ns std.block.parse
  (:require [std.block.base :as base]
            [std.block.check :as check]
            [std.block.construct :as construct]
            [std.block.reader :as reader]
            [std.block.type :as type]
            [std.protocol.block :as protocol.block]
            [std.string :as str]
            [clojure.tools.reader :as tools.reader]))

(def ^:dynamic *end-delimiter* nil)

(def ^:dynamic *symbol-allowed* [\' \:])

(def ^:dynamic *dispatch-options*
  {\^ :meta      \# :hash      \\ :char
   \( :list      \[ :vector    \{ :map
   \} :unmatched \] :unmatched \) :unmatched
   \~ :unquote   \' :quote     \` :syntax
   \; :comment   \@ :deref     \" :string
   \: :keyword   \, :comma     nil :eof})

(defn read-dispatch
  "dispatches on first symbol
 
   (read-dispatch \\tab)
   => :void
 
   (read-dispatch (first \"#\"))
   => :hash"
  {:added "3.0"}
  ([ch]
   (cond (nil? ch) :eof

         (= ch *end-delimiter*) :delimiter

         (check/whitespace? ch) :void

         :else (get *dispatch-options* ch :token))))

(defmulti -parse
  "extendable parsing function
 
   (base/block-info (-parse (reader/create \":a\")))
   => {:type :token, :tag :keyword, :string \":a\", :height 0, :width 2}
 
   (base/block-info (-parse (reader/create \"\\\"\\\n\\\"\")))
   => {:type :token, :tag :string, :string \"\\\"\\\n\\\"\", :height 0, :width 4}
 
   (base/block-info (-parse (reader/create \"\\\"\\n\\\"\")))
   => {:type :token, :tag :string, :string \"\\\"\\n\\\"\", :height 1, :width 1}"
  {:added "3.0"}
  (comp #'read-dispatch reader/peek-char))

(defmethod -parse :unmatched
  ([reader]
   (reader/throw-reader reader
                        "Unmatched delimiter."
                        (reader/peek-char reader))))

;; void block

(defn parse-void
  "reads a void block from reader
 
   (->> (reader/read-repeatedly (reader/create \" \\t\\n\\f\")
                                parse-void
                                eof-block?)
        (take 5)
        (map str))
   => [\"␣\" \"\\\t\" \"\\\n\" \"\\\f\"]"
  {:added "3.0"}
  ([reader]
   (construct/void (reader/read-char reader))))

(defmethod -parse :void
  ([reader]
   (parse-void reader)))

(defmethod -parse :eof
  ([reader]
   (construct/void nil)))

(defmethod -parse :comma
  ([reader]
   (parse-void reader)))

(defmethod -parse :delimiter
  ([reader]
   (parse-void reader)))

;; comment block
(defn parse-comment
  "creates a comment
 
   (-> (reader/create \";this is a comment\")
       parse-comment
       (base/block-info))
   => {:type :comment, :tag :comment, :string \";this is a comment\", :height 0, :width 18}"
  {:added "3.0"}
  ([reader]
   (let [line (-> reader
                  (reader/read-until (fn [ch]
                                       (or (nil? ch)
                                           (check/linebreak? ch)))))]
     (construct/comment line))))

(defmethod -parse :comment
  ([reader]
   (parse-comment reader)))

;; token block

(defn parse-token
  "reads token block from the reader
 
   (-> (reader/create \"abc\")
       (parse-token)
       (base/block-value))
   => 'abc
 
   (-> (reader/create \"3/5\")
       (parse-token)
       (base/block-value))
   => 3/5"
  {:added "3.0"}
  ([reader]
   (let [start (reader/read-to-boundary reader)
         val   (try (read-string start)
                    (catch Throwable t
                      (reader/throw-reader reader {:value start})))]
     (cond (symbol? val)
           (->> (reader/read-to-boundary reader *symbol-allowed*)
                (str start)
                (construct/token-from-string))

           :else
           (construct/token-from-string start)))))

(defn parse-keyword
  "reads a keyword block from the reader
 
   (-> (reader/create \":a/b\")
       (parse-keyword)
       (base/block-value))
   => :a/b
 
   (-> (reader/create \"::hello\")
       (parse-keyword)
       (base/block-value))
   => (keyword \":hello\")"
  {:added "3.0"}
  ([reader]
   (let [ksym (-> reader
                  (reader/step-char)
                  (reader/read-to-boundary *symbol-allowed*))]
     (construct/token-from-string (str ":" ksym)
                                  (keyword ksym)
                                  (format "(keyword \"%s\")" (str ksym))))))

(defn parse-reader
  "reads a :char block from the reader"
  {:added "3.0"}
  ([reader]
   (let [value (tools.reader/read reader)]
     (construct/token value))))

(defn read-string-data
  "reads string data from the reader
 
   (read-string-data (reader/create \"\\\"hello\\\"\"))
   => \"hello\""
  {:added "3.0"}
  ([reader]
   (let [reader (reader/step-char reader)
         lines (loop [buffer  (StringBuffer.)
                      escape? false
                      lines   []]
                 (if-let [ch (reader/read-char reader)]
                   (cond (and (not escape?) (= ch \"))
                         (conj lines (str buffer))

                         (= ch \newline)
                         (let [line (str buffer)]
                           (recur (doto buffer
                                    (.setLength 0))
                                  escape?
                                  (conj lines line)))

                         :else
                         (do (recur (doto buffer (.append ch))
                                    (and (not escape?)
                                         (= ch \\))
                                    lines)))
                   (reader/throw-reader reader "Unexpected EOF while reading string.")))]
     (str/joinl lines "\n"))))

(comment
  (defn read-string-data
  "reads string data from the reader
 
   (read-string-data (reader/create \"\\\"hello\\\"\"))
   => \"hello\""
  {:added "3.0"}
  ([reader]
   (let [reader (reader/step-char reader) ; Consume opening quote
         sb (StringBuilder.)]
     (loop [escape? false]
       (if-let [ch (reader/read-char reader)]
         (cond
           escape?
           (let [ch* (case ch
                       \t \tab
                       \r \return
                       \n \newline
                       \b \backspace
                       \f \formfeed
                       ch)]
             (.append sb ch*)
             (recur false))

           (= ch \\)
           (recur true)

           (= ch \")
           (str sb)

           :else
           (do (.append sb ch)
               (recur false)))
         (reader/throw-reader reader "Unexpected EOF while reading string."))))))

  )

(defmethod -parse :token
  ([reader]
   (parse-token reader)))

(defmethod -parse :keyword
  ([reader]
   (parse-keyword reader)))

(defmethod -parse :char
  ([reader]
   (parse-reader reader)))

(defmethod -parse :string
  ([reader]
   (construct/token (read-string-data reader))))

;; container block

(defn eof-block?
  "checks if block is of tag :eof
 
   (eof-block? (-parse (reader/create \"\")))
   => true"
  {:added "3.0"}
  ([block]
   (= :eof (base/block-tag block))))

(defn delimiter-block?
  "checks if block is of tag :delimiter
 
   (delimiter-block?
    (binding [*end-delimiter* (first \"}\")]
      (-parse (reader/create \"}\"))))
   => true"
  {:added "3.0"}
  ([block]
   (= :delimiter (base/block-tag block))))

(defn read-whitespace
  "reads whitespace blocks as vector
 
   (count (read-whitespace (reader/create \"   \")))
   => 3"
  {:added "3.0"}
  ([reader]
   (mapv construct/void (reader/read-while reader check/whitespace?))))

(defn parse-non-expressions
  "parses whitespace until next token
 
   (str (parse-non-expressions (reader/create \" \\na\")))
   => \"[(␣ \\\n) a]\""
  {:added "3.0"}
  ([reader]
   (reader/read-include reader -parse base/expression?)))

(defn read-start
  "helper to verify that the beginning has been read
 
   (read-start (reader/create \"~@\") \"~#\")
   => (throws)"
  {:added "3.0"}
  ([reader start]
   (doseq [current start]
     (let [ch (reader/read-char reader)
           _  (if (not= ch current)
                (reader/throw-reader reader "Starting delimiter is incorrect." {:current current
                                                                                :start start
                                                                                :reader ch}))])) reader))

(defn read-collection
  "reads all children, taking a delimiter as option
 
   (->> (read-collection (reader/create \"(1 2 3 4 5)\") \"(\" (first \")\"))
        (apply str))
   => \"1␣2␣3␣4␣5\""
  {:added "3.0"}
  ([reader start end-delimiter]
   (read-start reader start) (binding [*end-delimiter* end-delimiter]
                               (-> reader
                                   (reader/read-repeatedly
                                    (fn [reader]
                                      (let [block (-parse reader)
                                            _ (if (eof-block? block)
                                                (reader/throw-reader reader "Unexpected EOF while reading string."))]
                                        block))
                                    (fn [block]
                                      (= (str end-delimiter) (base/block-string block))))))))

(defn read-cons
  "helper method for reading 
   (->> (read-cons (reader/create \"@hello\") \"@\")
        (map base/block-string))
   => '(\"hello\")
 
   (->> (read-cons (reader/create \"^hello {}\") \"^\" 2)
        (map base/block-string))
   => '(\"hello\" \" \" \"{}\")"
  {:added "3.0"}
  ([reader start]
   (read-cons reader start 1))
  ([reader start n]
   (read-start reader start)
   (->> (fn [] (let [[nexprs block] (parse-non-expressions reader)]
                 (conj (vec nexprs) block)))
        (repeatedly n)
        (apply concat)
        vec)))

(defn parse-collection
  "parses a collection
 
   (-> (parse-collection (reader/create \"#(+ 1 2 3 4)\") :fn)
       (base/block-value))
   => '(fn* [] (+ 1 2 3 4))
 
   (-> (parse-collection (reader/create \"(1 2 3 4)\") :list)
       (base/block-value))
   => '(1 2 3 4)
 
   (-> (parse-collection (reader/create \"[1 2 3 4]\") :vector)
       (base/block-value))
   => [1 2 3 4]
 
   (-> (parse-collection (reader/create \"{1 2 3 4}\") :map)
       (base/block-value))
   => {1 2, 3 4}
 
   (-> (parse-collection (reader/create \"#{1 2 3 4}\") :set)
       (base/block-value))
   => #{1 4 3 2}"
  {:added "3.0"}
  ([reader tag]
   (let [{:keys [start end]} (construct/*container-props* tag)
         children (read-collection reader start (first end))]
     (construct/container tag children))))

(defmethod -parse :list
  ([reader]
   (parse-collection reader :list)))

(defmethod -parse :map
  ([reader]
   (parse-collection reader :map)))

(defmethod -parse :vector
  ([reader]
   (parse-collection reader :vector)))

(defn parse-cons
  "parses a cons
 
   (-> (parse-cons (reader/create \"~hello\") :unquote)
       (base/block-value))
   => '(unquote hello)
 
   (-> (parse-cons (reader/create \"~@hello\") :unquote-splice)
       (base/block-value))
   => '(unquote-splicing hello)
 
   (-> (parse-cons (reader/create \"^tag {:a 1}\") :meta)
       (base/block-value)
       ((juxt meta identity)))
   => [{:tag 'tag} {:a 1}]
 
   (-> (parse-cons (reader/create \"@hello\") :deref)
       (base/block-value))
   => '(deref hello)
 
   (-> (parse-cons (reader/create \"`hello\") :syntax)
       (base/block-value))
   => '(quote std.block.parse-test/hello)"
  {:added "3.0"}
  ([reader tag]
   (let [{:keys [cons start]} (get construct/*container-props* tag)
         children (read-cons reader start cons)]
     (construct/container tag children))))

(defmethod -parse :deref
  ([reader]
   (parse-cons reader :deref)))

(defmethod -parse :meta
  ([reader]
   (parse-cons reader :meta)))

(defmethod -parse :quote
  ([reader]
   (parse-cons reader :quote)))

(defmethod -parse :syntax
  ([reader]
   (parse-cons reader :syntax)))

(defn parse-unquote
  "parses a block starting with `~` from the reader
 
   (-> (parse-unquote (reader/create \"~hello\"))
       (base/block-value))
   => '(unquote hello)
 
   (-> (parse-unquote (reader/create \"~@hello\"))
       (base/block-value))
   => '(unquote-splicing hello)"
  {:added "3.0"}
  ([reader]
   (let [dispatch  (-> reader
                       (read-start "~")
                       (reader/peek-char))
         reader   (reader/unread-char reader \~)]
     (if (= dispatch \@)
       (parse-cons reader :unquote-splice)
       (parse-cons reader :unquote)))))

(defmethod -parse :unquote
  ([reader]
   (parse-unquote reader)))

(defn parse-select
  "parses a block starting with `#?` from the reader
 
   (-> (parse-select (reader/create \"#?(:cljs a)\"))
       (base/block-value))
   => '(? {:cljs a})
 
   (-> (parse-select (reader/create \"#?@(:cljs a)\"))
       (base/block-value))
   => '(?-splicing {:cljs a})"
  {:added "3.0"}
  ([reader]
   (let [dispatch  (-> reader
                       (read-start "#?")
                       (reader/peek-char))
         reader    (-> reader
                       (reader/unread-char \?)
                       (reader/unread-char \#))]
     (if (= dispatch \@)
       (parse-cons reader :select-splice)
       (parse-cons reader :select)))))

(defn parse-hash-uneval
  "parses the hash-uneval string
 
   (str (parse-hash-uneval (reader/create \"#_\")))
   => \"#_\""
  {:added "3.0"}
  ([reader]
   (read-start reader "#_")
   (construct/uneval)))

(defn parse-hash-cursor
  "parses the hash-cursor string
 
   (str (parse-hash-cursor (reader/create \"#|\")))
   => \"|\""
  {:added "3.0"}
  ([reader]
   (read-start reader "#|")
   (construct/cursor)))

(def ^:dynamic *hash-options*
  {:set          {:dispatch \{ :fn #(parse-collection % :set)}
   :root         {:dispatch \[ :fn #(parse-collection % :root)}
   :fn           {:dispatch \( :fn #(parse-collection % :fn)}
   :regexp       {:dispatch \" :fn parse-reader}
   :var          {:dispatch \' :fn #(parse-cons % :var)}
   :hash-keyword {:dispatch \: :fn #(parse-cons % :hash-keyword)}
   :hash-meta    {:dispatch \^ :fn #(parse-cons % :hash-meta)}
   :hash-eval    {:dispatch \= :fn #(parse-cons % :hash-eval)}
   :hash-uneval  {:dispatch \_ :fn parse-hash-uneval}
   :hash-cursor  {:dispatch \| :fn parse-hash-cursor}
   :select       {:dispatch \? :fn parse-select}})

(def ^:dynamic *hash-dispatch*
  (->> *hash-options*
       (keep (fn [[k {:keys [dispatch] :as v}]]
               (if dispatch [dispatch (:fn v)])))
       (into {})))

(defn parse-hash
  "parses a block starting with `#` from the reader
 
   (-> (parse-hash (reader/create \"#{1 2 3}\"))
       (base/block-value))
   => #{1 2 3}
 
   (-> (parse-hash (reader/create  \"#(+ 1 2)\"))
       (base/block-value))
   => '(fn* [] (+ 1 2))"
  {:added "3.0"}
  ([reader]
   (let [dispatch  (-> reader
                       (read-start "#")
                       (reader/peek-char))
         func      (get *hash-dispatch* dispatch #(parse-cons % :hash-token))]
     (-> reader
         (reader/unread-char \#)
         func))))

(defmethod -parse :hash
  ([reader]
   (parse-hash reader)))

(defn parse-string
  "parses a block from a string input
 
   (-> (parse-string \"#(:b {:b 1})\")
       (base/block-value))
   => '(fn* [] ((keyword \"b\") {(keyword \"b\") 1}))"
  {:added "3.0"}
  ([s]
   (-parse (reader/create s))))

(defn parse-root
  "parses a root
 
   (str (parse-root \"a b c\"))
   => \"a b c\""
  {:added "3.0"}
  ([s]
   (-> (reader/create s)
       (reader/read-repeatedly -parse type/eof-block?)
       (construct/root))))

(defn parse-first
  "gets the first element in root
   
   (str (parse-first \"a b c\"))
   => \"a\""
  {:added "4.0"}
  ([string]
   (->> (parse-root string)
        (base/block-children)
        (filter type/code-block?)
        (first))))

(comment
  (def rdr (reader/create (slurp "test-scratch/gather.clj"))))
