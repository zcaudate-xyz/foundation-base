(ns code.manage.unit.snapto-test
  (:require [code.manage.unit.snapto :refer :all]
            [code.project            :as project]
            [std.block               :as block])
  (:use code.test))

(def +wrong-fact+
  "^{:refer xt.lang.spec-base/x:str-replace, :added \"4.1\"}
(fact
 \"replaces matching substrings\"
 (!.lua (xt/x:str-replace \"hello-world\" \"-\" \"/\"))
 =>
 \"hello/world\"
 (!.lua (xt/x:str-replace \"hello-world\" \"_\" \"/\"))
 =>
 \"hello-world\")")

(def +right-fact+
  "^{:refer xt.lang.spec-base/x:str-replace, :added \"4.1\"}
(fact \"replaces matching substrings\"

  (!.lua (xt/x:str-replace \"hello-world\" \"-\" \"/\"))
  => \"hello/world\"

  (!.lua (xt/x:str-replace \"hello-world\" \"_\" \"/\"))
  => \"hello-world\")")

(def +wrong-file+
  (str "(ns example.core-test\n"
       "  (:use code.test))\n\n"
       +wrong-fact+))

(def +right-file+
  (str "(ns example.core-test\n"
       "  (:use code.test))\n\n"
       +right-fact+))

(def +wrong-source-file+
  (str "(ns hello\n"
       " (:require [xt.db.node.runtime :as runtime] [xt.lang.spec-base :as xt] [xt.lang.common-data :as xtd]))\n\n"
       "(l/script :xtalk\n"
       "  {:export [MODULE] :require [[xt.db.node.runtime :as runtime] [xt.lang.spec-base :as xt] [xt.lang.common-data :as xtd]]})"))

(def +right-source-file+
  (str "(ns hello\n"
       "  (:require [xt.db.node.runtime  :as runtime]\n"
       "            [xt.lang.spec-base   :as xt]\n"
       "            [xt.lang.common-data :as xtd]))\n\n"
       "(l/script :xtalk\n"
       "  {:require [[xt.db.node.runtime :as runtime]\n"
       "             [xt.lang.spec-base :as xt]\n"
       "             [xt.lang.common-data :as xtd]]\n"
       "   :export [MODULE]})"))

(fact "formats ns and l/script* forms"
  (snapto-string +wrong-source-file+)
  => +right-source-file+)

^{:refer code.manage.unit.snapto/unwrap-fact-block :added "4.1"}
(fact "extracts the metadata prefix and inner block"
  (let [result (unwrap-fact-block (block/parse-first "(fact \"hello\" 1)"))]
    (:prefix result) => ""
    (-> result :block block/string) => "(fact \"hello\" 1)")
  (let [result (unwrap-fact-block (block/parse-first "^{:added \"4.1\"}\n(fact \"hello\" 1)"))]
    (:prefix result) => "^{:added \"4.1\"}\n"
    (-> result :block block/string) => "(fact \"hello\" 1)"))

^{:refer code.manage.unit.snapto/spaces :added "4.1"}
(fact "returns the requested number of spaces"
  (spaces 0) => ""
  (spaces 3) => "   "
  (spaces 5) => "     ")

^{:refer code.manage.unit.snapto/form-op :added "4.1"}
(fact "returns the top-level operator of a list form"
  (form-op (block/parse-first "(fact \"hello\" 1)")) => 'fact
  (form-op (block/parse-first "(ns example.core)")) => 'ns
  (form-op (block/parse-first "(l/script :xtalk {})")) => 'l/script)

^{:refer code.manage.unit.snapto/fact-block? :added "4.1"}
(fact "recognises fact forms"
  (fact-block? (block/parse-first "(fact \"hello\" 1)")) => 'fact
  (fact-block? (block/parse-first "(ns example.core)")) => nil
  (fact-block? (block/parse-first "(l/script :xtalk {})")) => nil)

^{:refer code.manage.unit.snapto/ns-block? :added "4.1"}
(fact "recognises ns forms"
  (ns-block? (block/parse-first "(ns example.core)")) => 'ns
  (ns-block? (block/parse-first "(fact \"hello\" 1)")) => nil
  (ns-block? (block/parse-first "(l/script :xtalk {})")) => nil)

^{:refer code.manage.unit.snapto/script-block? :added "4.1"}
(fact "recognises l/script forms"
  (script-block? (block/parse-first "(l/script :xtalk {})")) => 'l/script
  (script-block? (block/parse-first "(fact \"hello\" 1)")) => nil
  (script-block? (block/parse-first "(ns example.core)")) => nil)

^{:refer code.manage.unit.snapto/leading-indent :added "4.1"}
(fact "counts leading spaces and tabs"
  (leading-indent "hello") => 0
  (leading-indent "  hello") => 2
  (leading-indent "\thello") => 1
  (leading-indent "  \thello") => 3)

^{:refer code.manage.unit.snapto/trim-indent :added "4.1"}
(fact "removes up to n leading whitespace characters"
  (trim-indent "  hello" 2) => "hello"
  (trim-indent "    hello" 2) => "  hello"
  (trim-indent "hello" 2) => "hello"
  (trim-indent "  \thello" 2) => "\thello")

^{:refer code.manage.unit.snapto/normalise-block-string :added "4.1"}
(fact "removes parent indentation from subsequent lines"
  (normalise-block-string "(+ 1 1)") => "(+ 1 1)"
  (normalise-block-string "(!.js\n  (var out [])\n  out)" 2)
  => "(!.js\n(var out [])\nout)"
  (normalise-block-string "a\n\n  b" 2) => "a\n\nb")

^{:refer code.manage.unit.snapto/child-entries :added "4.1"}
(fact "returns non-void children together with their starting column"
  (mapv #(-> % :block block/string)
        (child-entries (block/parse-first "(fact \"hello\" (+ 1 1) => 2)")))
  => ["fact" "\"hello\"" "(+ 1 1)" "=>" "2"]
  (every? pos? (map :col (child-entries (block/parse-first "(fact)"))))
  => true)

^{:refer code.manage.unit.snapto/entry-block :added "4.1"}
(fact "returns the block for an entry"
  (-> {:block (block/parse-first "(+ 1 1)") :col 3} entry-block block/string) => "(+ 1 1)"
  (-> (block/parse-first "(+ 1 1)") entry-block block/string) => "(+ 1 1)")

^{:refer code.manage.unit.snapto/entry-col :added "4.1"}
(fact "returns the column for an entry"
  (entry-col {:block (block/parse-first "(+ 1 1)") :col 3}) => 3
  (entry-col (block/parse-first "(+ 1 1)")) => 1)

^{:refer code.manage.unit.snapto/parse-body :added "4.1"}
(fact "partitions a fact body into plain forms and expression/check pairs"
  (->> (parse-body (->> (block/parse-first "(fact \"hello\" (+ 1 1) (odd? 3) => true)")
                        child-entries
                        (drop 2)))
       (mapv (fn [{:keys [type expr expected]}]
               {:type type
                :expr (some-> expr entry-block block/string)
                :expected (some-> expected entry-block block/string)})))
  => [{:type :form
       :expr "(+ 1 1)"
       :expected nil}
      {:type :check
       :expr "(odd? 3)"
       :expected "true"}])

^{:refer code.manage.unit.snapto/render-form :added "4.1"}
(fact "preserves nested indentation relative to the form start"
  (let [entry (->> (block/parse-first "(fact \"iterates arrays in order\"\n  (!.js\n    (var out [])\n    out))")
                   child-entries
                   (drop 2)
                   first)]
    (render-form entry))
  => "(!.js\n  (var out [])\n  out)")

^{:refer code.manage.unit.snapto/render-prefixed-items :added "4.1"}
(fact "renders items with aligned continuation"
  (render-prefixed-items "[" (child-entries (block/parse-first "[a b c]")) "]" "")
  => "[a\n b\n c]"
  (render-prefixed-items "(:require" (child-entries (block/parse-first "[a]")) ")" " ")
  => "(:require a)")

^{:refer code.manage.unit.snapto/parse-map-pairs :added "4.1"}
(fact "partitions map entries into key/value pairs"
  (mapv #(-> % :key entry-block block/string)
        (parse-map-pairs (child-entries (block/parse-first "{:a 1 :b 2}"))))
  => [":a" ":b"]
  (mapv #(-> % :value entry-block block/string)
        (parse-map-pairs (child-entries (block/parse-first "{:a 1 :b 2}"))))
  => ["1" "2"])

^{:refer code.manage.unit.snapto/script-option-rank :added "4.1"}
(fact "ranks known script options before unknown ones"
  (script-option-rank {:index 0 :key (block/parse-first ":require")}) => 9
  (script-option-rank {:index 0 :key (block/parse-first ":export")}) => 14
  (script-option-rank {:index 5 :key (block/parse-first ":unknown")}) => 1005)

^{:refer code.manage.unit.snapto/render-script-value :added "4.1"}
(fact "formats script option values"
  (render-script-value (block/parse-first ":runtime") (block/parse-first ":xtalk"))
  => ":xtalk"
  (render-script-value (block/parse-first ":require") (block/parse-first "[[a :as x]]"))
  => "[[a :as x]]"
  (render-script-value (block/parse-first ":export") (block/parse-first "[MODULE]"))
  => "[MODULE]")

^{:refer code.manage.unit.snapto/render-script-map-pair :added "4.1"}
(fact "formats a single script option"
  (render-script-map-pair (block/parse-first ":runtime") (block/parse-first ":xtalk") 1)
  => ":runtime :xtalk")

^{:refer code.manage.unit.snapto/render-script-map :added "4.1"}
(fact "sorts and formats a script config map"
  (render-script-map (block/parse-first "{:export [MODULE] :require [[a]]}"))
  => "{:require [[a]]\n :export [MODULE]}")

^{:refer code.manage.unit.snapto/render-ns-clause :added "4.1"}
(fact "formats a single ns clause"
  (render-ns-clause (block/parse-first "(:require [a :as x])"))
  => "(:require [a :as x])"
  (render-ns-clause (block/parse-first "(:require [a :as x] [bb :as y])"))
  => "(:require [a  :as x]\n          [bb :as y])")

^{:refer code.manage.unit.snapto/render-item :added "4.1"}
(fact "formats a plain form or expression/check pair"
  (render-item {:type :form :expr (block/parse-first "(+ 1 1)")})
  => "  (+ 1 1)"
  (render-item {:type :check :expr (block/parse-first "(+ 1 1)") :expected (block/parse-first "2")})
  => "  (+ 1 1)\n  => 2")

^{:refer code.manage.unit.snapto/snap-ns-string :added "4.1"}
(fact "formats a single ns form"
  (snap-ns-string "(ns example.core (:require [a :as x]))")
  => "(ns example.core\n  (:require [a :as x]))")

^{:refer code.manage.unit.snapto/snap-script-string :added "4.1"}
(fact "formats a single l/script form"
  (snap-script-string "(l/script :xtalk {:require [[a]] :export [MODULE]})")
  => "(l/script :xtalk\n  {:require [[a]]\n   :export [MODULE]})")

^{:refer code.manage.unit.snapto/snap-form-string :added "4.1"}
(fact "does not add a blank line when the fact only has a docstring"
  (snap-form-string (block/parse-first "(fact \"OEOEUEOU\")"))
  => "(fact \"OEOEUEOU\")")

^{:refer code.manage.unit.snapto/snap-block-string :added "4.1"}
(fact "preserves multiline metadata blocks and reader sugar"
  (let [source   "^{:refer xt.db.schema.base-util/collect-routes,\n  :added \"4.0\",\n  :setup\n  [(def +routes+\n     [{:id \"ping\"}])\n   (def +result+\n     (contains-in {\"api/ping\" {:id \"ping\"}}))]}\n(fact\n \"collect routes\"\n ^{:hidden true}\n (!.lua (ut/collect-routes (@! +routes+) \"db\"))\n =>\n +result+)"
        expected "^{:refer xt.db.schema.base-util/collect-routes,\n  :added \"4.0\",\n  :setup\n  [(def +routes+\n     [{:id \"ping\"}])\n   (def +result+\n     (contains-in {\"api/ping\" {:id \"ping\"}}))]}\n(fact \"collect routes\"\n\n  ^{:hidden true}\n  (!.lua (ut/collect-routes (@! +routes+) \"db\"))\n  => +result+)"]
    (snap-block-string (block/parse-first source))
    => expected))

^{:refer code.manage.unit.snapto/snapto-string :added "4.1"}
(fact "formats all top-level fact forms in a test file"
  (snapto-string +wrong-file+)
  => +right-file+)

^{:refer code.manage.unit.snapto/snapto :added "4.1"}
(fact "formats fact tests into a consistent snap-to layout"
  (project/in-context (snapto {:write false}))
  => map?)
