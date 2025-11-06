(ns std.block.check
  (:require [std.lib.foundation :as h]
            [std.lib.collection :as c]))

(def ^:dynamic *boundaries*
  #{\" \: \; \' \@ \^ \` \~ \( \) \[ \] \{ \} \\ nil})

(def ^:dynamic *linebreaks*
  #{\newline \return \formfeed})

(def ^:dynamic *delimiters*
  #{\} \] \) \( \[ \{})

(defn boundary?
  "returns whether a char is of a boundary type
 
   (boundary? (first \"[\")) => true
 
   (boundary? (first \"\\\"\")) => true"
  {:added "3.0"}
  ([c]
   (contains? *boundaries* c)))

(defn whitespace?
  "returns whether a char is of a whitespace type
 
   (whitespace? \\space) => true"
  {:added "3.0"}
  ([^Character c]
   (and c (Character/isWhitespace c))))

(defn comma?
  "returns whether a char is a comma
 
   (comma? (first \",\")) => true"
  {:added "3.0"}
  ([c]
   (= c \,)))

(defn linebreak?
  "returns whether a char is a linebreak type
 
   (linebreak? \\newline) => true"
  {:added "3.0"}
  ([c]
   (contains? *linebreaks* c)))

(defn delimiter?
  "check for delimiter char
 
   (delimiter? (first \")\")) => true"
  {:added "3.0"}
  ([c]
   (contains? *delimiters* c)))

(defn voidspace?
  "determines if an input is a void input
 
   (voidspace? \\newline)
   => true"
  {:added "3.0"}
  ([c]
   (or (whitespace? c)
       (comma? c))))

(defn linetab?
  "checs if character is a tab
 
   (linetab? (first \"\\t\"))
   => true"
  {:added "3.0"}
  ([c]
   (= c \tab)))

(defn linespace?
  "returs whether a char is a linespace type
 
   (linespace? \\space) => true"
  {:added "3.0"}
  ([c]
   (and (whitespace? c)
        (not (linebreak? c))
        (not (linetab? c)))))

(defn voidspace-or-boundary?
  "check for void or boundary type
 
   (->> (map voidspace-or-boundary? (concat *boundaries*
                                            *linebreaks*))
        (every? true?))
   => true"
  {:added "3.0"}
  ([c]
   (or (voidspace? c) (boundary? c))))

(def ^:dynamic *void-checks*
  {:eof       nil?
   :linetab   linetab?
   :linebreak linebreak?
   :comma     comma?
   :delimiter delimiter?
   :linespace linespace?})

(def ^:dynamic *token-checks*
  {:nil     nil?
   :boolean boolean?
   :ratio   ratio?
   :byte    h/byte?
   :short   h/short?
   :long    integer?
   :bigint  h/bigint?
   :float   float?
   :double  double?
   :bigdec  h/bigdec?
   :keyword keyword?
   :symbol  symbol?
   :string  string?
   :char    char?
   :inst    inst?
   :regexp  h/regexp?})

(def ^:dynamic *collection-checks*
  {:list   c/form?
   :map    c/hash-map?
   :set    set?
   :vector vector?})

(defn tag
  "takes a set of checks and returns the key
 
   (tag *void-checks* \\space)
   => :linespace
 
   (tag *collection-checks* [])
   => :vector"
  {:added "3.0"}
  ([checks input]
   (->> checks
        (keep (fn [[tag check]]
                (if (check input) tag)))
        (first))))

(defn void-tag
  "returns the tag associated with input
 
   (void-tag \\newline)
   => :linebreak"
  {:added "3.0"}
  ([ch]
   (tag *void-checks* ch)))

(defn void?
  "determines if an input is a void input
 
   (void? \\newline)
   => true"
  {:added "3.0"}
  ([ch]
   (boolean (void-tag ch))))

(defn token-tag
  "returns the tag associated with the input
 
   (token-tag 'hello)
   => :symbol"
  {:added "3.0"}
  ([form]
   (tag *token-checks* form)))

(defn token?
  "determines if an input is a token
 
   (token? 3/4)
   => true"
  {:added "3.0"}
  ([form]
   (boolean (token-tag form))))

(defn collection-tag
  "returns th tag associated with the input
 
   (collection-tag [])
   => :vector"
  {:added "3.0"}
  ([form]
   (tag *collection-checks* form)))

(defn collection?
  "determines if an input is a token
 
   (collection? {})
   => true"
  {:added "3.0"}
  ([form]
   (boolean (collection-tag form))))

(defn comment?
  "determines if an input is a comment string
 
   (comment? \"hello\")
   => false
 
   (comment? \";hello\")
   => true"
  {:added "3.0"}
  ([^String s]
   (and (string? s)
        (.startsWith s ";"))))
