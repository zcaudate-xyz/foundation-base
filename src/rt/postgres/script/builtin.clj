(ns rt.postgres.script.builtin
  (:require [std.lib :as h]
            [std.lang :as l]
            [std.string :as str]
            [rt.postgres.grammar])
  (:refer-clojure :exclude [abs concat format replace repeat reverse mod
                            bit-and bit-or count max min]))

(l/script :postgres
  rt.postgres
  {:macro-only true})

(def +functions+
  '[;; array
    

    ;;
    enum-first
    enum-last
    enum-range
    
    ;; string
    ascii
    btrim
    chr
    concat-ws
    format
    initcap
    left
    length
    lpad
    ltrim
    md5
    parse-ident
    quote-ident
    quote-literal
    quote-nullable
    repeat
    rpad
    rtrim
    split-part
    strpos
    substr
    replace
    starts-with
    string-agg
    substring
    to-ascii
    to-hex
    translate
    trim
    upper
    lower
    position
    overlay
    

    ;;
    to-char
    to-date
    to-number
    to-timestamp
    
    ;; checks
    exists
    

    ;; aggregate
    bool-and
    bool-or
    count
    every
    max
    min
    sum
    corr
    covar-pop
    covar-samp
    regr-avgx
    regr-avgy
    regr-count
    regr-intercept
    regr-r2
    regr-slope
    regr-sxx
    regr-sxy
    regr-syy
    stddev
    stddev-pop
    stddev-samp
    variance
    var-pop
    var-samp
    mode
    percentile-cont
    percentile-disc
    rank
    dense-rank
    percent-rank
    cume-dist

    ;;
    grouping
    rollup


    ;; window
    row-number
    ntile
    lag
    lead
    first-value
    last-value
    nth-value
        
    ;; range
    lower
    upper
    isempty
    lower-inc
    upper-inc
    lower-inf
    upper-inf
    range-merge

    ;; sequence
    nextval
    setval
    currval
    lastval

    ;; security
    random
    setseed
    encode
    digest
    
    ;; util
    collate
    coalesce
    generate-series
    generate-subscripts
    
    ;; time
    now
    timeofday
    
    ;; uuid
    gen-random-uuid
    uuid-nil
    uuid-generate-v1
    uuid-generate-v1mc
    uuid-generate-v4

    ;; util
    gen-random-bytes
    
    ;;
    normal-rand
    crosstab
    crosstab2
    crosstab3
    crosstab4
    connectby

    ;; crypto
    crypt
    encrypt
    decrypt
    encode
    decode
    hmac
    gen-salt])

(def +array+
  '[array-agg
    array-append
    array-cat
    array-dims
    array-eq
    array-fill
    array-ge
    array-gt
    array-in
    array-larger
    array-le
    array-length
    array-lower
    array-lt
    array-ndims
    array-ne
    array-out
    array-position
    array-positions
    array-prepend
    array-recv
    array-remove
    array-replace
    array-send
    array-smaller
    array-to-json
    array-to-string
    array-to-tsvector
    array-typanalyze
    array-unnest-support
    array-upper
    regexp-split-to-array
    string-to-array
    tsvector-to-array
    unnest])

(def +math+
  '[abs
    acos
    acosd
    acosh
    asin
    asind
    asinh
    atan
    atan2
    atan2d
    atand
    atanh
    cbrt
    ceil
    ceiling
    cos
    cosd
    cosh
    cot
    cotd
    degrees
    div
    exp
    factorial
    floor
    gcd
    log10
    log
    lcm
    ln
    mod
    pi
    pow
    radians
    round
    scale
    sign
    sin
    sind
    sinh
    sqrt
    tan
    tand
    tanh
    trunc])

(def +jsonb+
  '[jsonb-agg
    jsonb-array-elements
    jsonb-array-elements-text
    jsonb-array-length
    jsonb-build-array
    jsonb-build-object
    jsonb-cmp
    jsonb-concat
    jsonb-contained
    jsonb-contains
    jsonb-delete
    jsonb-delete-path
    jsonb-each
    jsonb-each-text
    jsonb-eq
    jsonb-exists
    jsonb-exists-all
    jsonb-exists-any
    jsonb-extract-path
    jsonb-extract-path-text
    jsonb-ge
    jsonb-gt
    jsonb-hash
    jsonb-hash-extended
    jsonb-in
    jsonb-insert
    jsonb-le
    jsonb-lt
    jsonb-ne
    jsonb-object
    jsonb-object-agg
    jsonb-object-agg-finalfn
    jsonb-object-agg-transfn
    jsonb-object-field
    jsonb-object-field-text
    jsonb-object-keys
    jsonb-out
    jsonb-path-exists
    jsonb-path-exists-opr
    jsonb-path-exists-tz
    jsonb-path-match
    jsonb-path-match-opr
    jsonb-path-match-tz
    jsonb-path-query
    jsonb-path-query-array
    jsonb-path-query-array-tz
    jsonb-path-query-first
    jsonb-path-query-first-tz
    jsonb-path-query-tz
    jsonb-populate-record
    jsonb-populate-recordset
    jsonb-pretty
    jsonb-recv
    jsonb-send
    jsonb-set
    jsonb-set-lax
    jsonb-strip-nulls
    jsonb-to-record
    jsonb-to-recordset
    jsonb-to-tsvector
    jsonb-typeof
    to-jsonb])

(def +current+
  '[current-database
    current-query
    current-schema
    current-schemas
    current-setting
    set-config
    current-user])

(def +has+
  '[has-any-column-privilege
    has-column-privilege
    has-database-privilege
    has-foreign-data-wrapper-privilege
    has-function-privilege
    has-language-privilege
    has-schema-privilege
    has-sequence-privilege
    has-server-privilege
    has-table-privilege
    has-tablespace-privilege
    has-type-privilege])

(def +xml+
  '[cursor-to-xml
    cursor-to-xmlschema
    database-to-xml
    database-to-xml-and-xmlschema
    database-to-xmlschema
    query-to-xml
    query-to-xml-and-xmlschema
    query-to-xmlschema
    schema-to-xml
    schema-to-xml-and-xmlschema
    schema-to-xmlschema
    table-to-xml
    table-to-xml-and-xmlschema
    table-to-xmlschema
    xml
    xml-is-well-formed
    xml-is-well-formed-content
    xml-is-well-formed-document
    xmlagg
    xmlcomment
    xmlconcat2
    xmlexists
    xmlvalidate])

(def +bit+
  '[bit
    bit-and
    bit-length
    bit-or
    bitand
    bitcat
    bitcmp
    biteq
    bitge
    bitgt
    bitle
    bitlt
    bitne
    bitnot
    bitor
    bitshiftleft
    bitshiftright
    bitxor
    get-bit
    set-bit
    octet-length
    position
    overlay])

(def +regexp+
  '[regexp-match regexp-matches regexp-replace regexp-split-to-array regexp-split-to-table])

(def +pgjwt+
  '[verify])

(defn- pg-tmpl
  "creates fragments in builtin"
  {:added "4.0"}
  [sym]
  (h/$ (def$.pg ~sym ~sym)))

(h/template-entries [pg-tmpl]
                    +array+
                    +bit+
                    +current+
                    +functions+
                    +has+
                    +jsonb+
                    +math+
                    +regexp+
                    +xml+
                    +pgjwt+)

(comment (l/ns:reset))
