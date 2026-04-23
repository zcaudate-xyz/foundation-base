(ns code.manage.unit.snapto-test
  (:require [code.manage.unit.snapto :refer :all]
            [code.project :as project]
            [std.block :as block])
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
(fact "TODO")

^{:refer code.manage.unit.snapto/render-item :added "4.1"}
(fact "TODO")

^{:refer code.manage.unit.snapto/render-form :added "4.1"}
(fact "preserves nested indentation relative to the form start"
  (let [entry (->> (block/parse-first "(fact \"iterates arrays in order\"\n  (!.js\n    (var out [])\n    out))")
                   child-entries
                   (drop 2)
                   first)]
    (render-form entry))
  => "(!.js\n  (var out [])\n  out)")

^{:refer code.manage.unit.snapto/snap-form-string :added "4.1"}
(fact "formats a single fact form into snap-to layout"
  (snap-form-string (block/parse-first +wrong-fact+))
  => +right-fact+)

^{:refer code.manage.unit.snapto/snap-form-string :added "4.1"}
(fact "does not add a blank line when the fact only has a docstring"
  (snap-form-string (block/parse-first "(fact \"OEOEUEOU\")"))
  => "(fact \"OEOEUEOU\")")

^{:refer code.manage.unit.snapto/snap-block-string :added "4.1"}
(fact "preserves multiline metadata blocks and reader sugar"
  (let [source   "^{:refer xt.db.base-util/collect-routes,\n  :added \"4.0\",\n  :setup\n  [(def +routes+\n     [{:id \"ping\"}])\n   (def +result+\n     (contains-in {\"api/ping\" {:id \"ping\"}}))]}\n(fact\n \"collect routes\"\n ^{:hidden true}\n (!.lua (ut/collect-routes (@! +routes+) \"db\"))\n =>\n +result+)"
        expected "^{:refer xt.db.base-util/collect-routes,\n  :added \"4.0\",\n  :setup\n  [(def +routes+\n     [{:id \"ping\"}])\n   (def +result+\n     (contains-in {\"api/ping\" {:id \"ping\"}}))]}\n(fact \"collect routes\"\n\n  ^{:hidden true}\n  (!.lua (ut/collect-routes (@! +routes+) \"db\"))\n  => +result+)"]
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
