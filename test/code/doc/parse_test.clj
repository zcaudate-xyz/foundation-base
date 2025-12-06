(ns code.doc.parse-test
  (:use code.test)
  (:require [code.doc.parse :refer :all]
            [std.block.navigate :as nav]
            [clojure.java.io :as io]
            [markdown.core :as md]))

^{:refer code.doc.parse/parse-ns-form :added "3.0"}
(fact "converts a ns zipper into an element"

  (-> (nav/parse-string "(ns example.core)")
      (parse-ns-form))
  => {:type :ns-form
      :indentation 0
      :meta {}
      :ns 'example.core
      :code "(ns example.core)"})

^{:refer code.doc.parse/code-form :added "3.0"}
(fact "converts a form zipper into a code string"

  (-> (nav/parse-string "(fact (+ 1 1) \n => 2)")
      (code-form 'fact))
  => "(+ 1 1) \n => 2")

^{:refer code.doc.parse/parse-fact-form :added "3.0"}
(fact "convert a fact zipper into an element"

  (-> (nav/parse-string "(fact (+ 1 1) \n => 2)")
      (parse-fact-form))
  => {:type :test :indentation 2 :code "(+ 1 1) \n => 2"})

^{:refer code.doc.parse/parse-facts-form :added "3.0"}
(fact "converts a facts zipper into an element"

  (-> (nav/parse-string "(facts (+ 1 1) \n => 2)")
      (parse-facts-form))
  => {:type :test :indentation 2 :code "(+ 1 1) \n => 2"})

^{:refer code.doc.parse/parse-comment-form :added "3.0"}
(fact "convert a comment zipper into an element"

  (-> (nav/parse-string "(comment (+ 1 1) \n => 2)")
      (parse-comment-form))
  => {:type :block :indentation 2 :code "(+ 1 1) \n => 2"})

^{:refer code.doc.parse/is-code-form :added "3.0"}
(fact "converts the `is` form into a string"

  (is-code-form (nav/parse-string "(is (= (+ 1 1) 2))"))
  => "(+ 1 1)\n=> 2"

  (is-code-form (nav/parse-string "(is true)"))
  => "true")

^{:refer code.doc.parse/parse-is-form :added "3.0"}
(fact "converts an `is` form into an element"
  (-> (nav/parse-string "(is (= (+ 1 1) 2))")
      (parse-is-form))
  => {:type :block, :indentation 2, :code "(+ 1 1)\n=> 2"})

^{:refer code.doc.parse/parse-deftest-form :added "3.0"}
(fact "converts the `deftest` form into an element"

  (-> (nav/parse-string "(deftest hello)")
      (parse-deftest-form))
  => {:type :test, :source {:row 1, :col 1, :end-row 1, :end-col 16}})

^{:refer code.doc.parse/parse-paragraph :added "3.0"}
(fact "converts a string zipper into an element"

  (-> (nav/parse-string "\"this is a paragraph\"")
      (parse-paragraph))
  => {:type :paragraph :text "this is a paragraph"})

^{:refer code.doc.parse/parse-directive :added "3.0"}
(fact "converts a directive zipper into an element"

  (-> (nav/parse-string "[[:chapter {:title \"hello world\"}]]")
      (parse-directive))
  => {:type :chapter :title "hello world"}

  (binding [*namespace* 'example.core]
    (-> (nav/parse-string "[[:ns {:title \"hello world\"}]]")
        (parse-directive)))
  => {:type :ns, :title "hello world", :ns 'example.core})

^{:refer code.doc.parse/parse-attribute :added "3.0"}
(fact "coverts an attribute zipper into an element"

  (-> (nav/parse-string "[[{:title \"hello world\"}]]")
      (parse-attribute))
  => {:type :attribute, :title "hello world"})

^{:refer code.doc.parse/parse-code-directive :added "3.0"}
(fact "coverts an code directive zipper into an element"

  (-> (nav/parse-string "[[:code {:language :ruby} \"1 + 1 == 2\"]]")
      (parse-code-directive))
  => {:type :block, :indentation 0 :code "1 + 1 == 2" :language :ruby})

^{:refer code.doc.parse/parse-whitespace :added "3.0"}
(fact "coverts a whitespace zipper into an element"

  (-> (nav/parse-root "1 2 3")
      (nav/down)
      (nav/right*)
      (parse-whitespace))
  => {:type :whitespace, :code [" "]})

^{:refer code.doc.parse/parse-code :added "3.0"}
(fact "coverts a code zipper into an element"

  (-> (nav/parse-root "(+ 1 1) (+ 2 2)")
      (nav/down)
      (parse-code))
  => {:type :code, :indentation 0, :code ["(+ 1 1)"]})

^{:refer code.doc.parse/wrap-meta :added "3.0"}
(fact "if form is meta, then go down a level"
  (let [func (wrap-meta (fn [n] (nav/string n)))]
    (func (nav/parse-string "^:a a")) => "a"
    (func (nav/parse-string "a")) => "a"))

^{:refer code.doc.parse/wrap-line-info :added "3.0"}
(fact "helper to add the line information to the parser (used by `parse-loop`)"
  (let [func (wrap-line-info (constantly {}))]
    (func (nav/parse-string "a"))
    => {:line {:row 1, :col 1, :end-row 1, :end-col 2}}))

^{:refer code.doc.parse/parse-single :added "3.0"}
(fact "parse a single zloc"
  (parse-single (nav/parse-string "a"))
  => {:type :code, :indentation 0, :code ["a"]})

^{:refer code.doc.parse/merge-current :added "3.0"}
(fact "if not whitespace, then merge output"
  (merge-current [] {:type :whitespace})
  => []

  (merge-current [] {:type :code})
  => [{:type :code}])

^{:refer code.doc.parse/parse-inner :added "3.0"}
(fact "parses the inner form of the fact and comment function"
  (parse-inner (nav/parse-string "(comment (+ 1 1))")
               #(-> % nav/down nav/right)
               []
               nil
               {})
  => (contains-in [{:type :code, :indentation 2, :code ["(+ 1 1)"]}]))

^{:refer code.doc.parse/parse-loop :added "3.0"}
(fact "the main loop for the parser"

  (-> (nav/parse-root "(ns example.core) 
                       [[:chapter {:title \"hello\"}]] 
                       (+ 1 1) 
                       (+ 2 2)")
      (nav/down)
      (parse-loop {}))

  => (contains-in
      [{:type :ns-form,
        :indentation 0,
        :ns 'example.core,
        :code "(ns example.core)"}
       {:type :chapter, :title "hello"}
       {:type :code, :indentation 0, :code (contains ["(+ 1 1)"
                                                      "(+ 2 2)"]
                                                     :gaps-ok)}]))

^{:refer code.doc.parse/parse-file :added "3.0"}
(fact "parses the entire file"
  (with-redefs [slurp (constantly "(+ 1 1)")
                io/input-stream (fn [_] (java.io.ByteArrayInputStream. (.getBytes "")))]
    (parse-file "foo.clj" {}))
  => (contains [{:type :code :indentation 0 :code ["(+ 1 1)"]
                 :line {:row 1 :col 1 :end-row 1 :end-col 8}}])

  (with-redefs [slurp (constantly "# Chapter\n\ncontent\n\n## Section")]
    (parse-file "foo.md" {}))
  => (contains [{:type :chapter :title "Chapter"}
                {:type :paragraph :text "\ncontent\n"}
                {:type :section :title "Section"}]))

^{:refer code.doc.parse/parse-markdown :added "3.0"}
(fact "parses markdown into sections"
  (parse-markdown "# Chapter\nText\n## Section\nMore text")
  => (contains [{:type :chapter :title "Chapter"}
                {:type :paragraph :text "Text"}
                {:type :section :title "Section"}
                {:type :paragraph :text "More text"}]))


^{:refer code.doc.parse/parse-header :added "4.1"}
(fact "TODO")