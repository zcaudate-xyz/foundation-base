(ns hara.model.spec-ruby-test
  (:require [hara.lang :as l]
            [hara.model.spec-ruby :as spec-ruby]
            [hara.model.spec-ruby.rewrite :as rewrite]
            [hara.model.annex.spec-xtalk.fn-ruby :as fn-ruby])
  (:use code.test))

(fact "Ruby Basic Emit"
  (l/emit-as :ruby
   '[(do
      (var a 1)
      (var b 2)
      (puts (+ a b)))])
  => "a = 1\nb = 2\nputs (a + b)")

(fact "Ruby Control Flow"
  (l/emit-as :ruby
   '[(if true
       (puts "true")
       (puts "false"))])
  => "if true\n  puts \"true\"\nelse\n  puts \"false\"\nend")

(fact "Ruby Functions"
  (l/emit-as :ruby
   '[(defn add [a b]
      (return (+ a b)))])
  => "def add(a,b)\n  return a + b\nend")

(fact "Ruby Arrays and Maps"
  (l/emit-as :ruby
   '[(do
      (var arr [1 2 3])
      (var m {:a 1 :b 2}))])
  => "arr = [1,2,3]\nm = {\"a\" => 1, \"b\" => 2}")

(fact "Ruby String Operations"
  (l/emit-as :ruby
   '[(do
       (var s "hello")
       (puts (. s (upcase))))])
  => "s = \"hello\"\nputs s.upcase")

(fact "Ruby XTalk Support"
  (l/emit-as :ruby
   '[(do
       (x:print "hello")
       (x:cat "a" "b"))])
  => (fn [s]
       (and (string? s)
            (re-find #"puts \"hello\"" s)
            (re-find #"return nil" s)
            (re-find #"\.call\(\)" s)
            (re-find #"\"a\" \+ \"b\"" s))))

(fact "Ruby x:print emits multi-arg output without relying on variadic puts prefix emission"
  (l/emit-as :ruby
   '[(x:print "hello" "world")])
  => (fn [s]
       (and (string? s)
            (re-find #"puts \"hello\"" s)
            (re-find #"puts \"world\"" s)
            (re-find #"return nil" s)
            (re-find #"\.call\(\)" s))))

(fact "Ruby invoke, spread, and range forms stay structural"
  (l/emit-as :ruby
   '[(do
        (. obj (respond_to? :call))
        (. f (call (:.. args)))
        (. s [(to start -1)])
        (. s [(to-e start stop)]))])
  => "obj.respond_to?(:call)\nf.call(*args)\ns[start..-1]\ns[start...stop]")

(fact "Ruby callable rewrite does not rewrap ruby-method-ref forms"
  (rewrite/rewrite-callable-form
   '(xt.lang.common-data/swap-key
     record
     "ref_links"
     (ruby-method-ref xt.lang.common-data/obj-assign-with)
     [ref-links xt/x:obj-assign])
   #{})
  => '(xt.lang.common-data/swap-key
       record
       "ref_links"
       (ruby-method-ref xt.lang.common-data/obj-assign-with)
       [ref-links (ruby-method-ref xt/x:obj-assign)]))

(fact "Ruby callable rewrite preserves namespaced constant values"
  (rewrite/rewrite-callable-form
   '(xt/x:get-key xt.db.text.base-scope/Scopes s)
   #{})
  => '(xt/x:get-key xt.db.text.base-scope/Scopes s))

(fact "Ruby destructuring normalizes xtalk keys to Ruby hash keys"
  (let [out (rewrite/rewrite-callable-form
             '(var #{order-by} opts)
             #{})]
    [(first out)
     (mapv #(nth % 1) (rest out))
     (mapv #(nth (nth % 2) 2) (rest out))])
  => '[do* [order-by] ["order_by"]]

  (l/emit-as :ruby
   '[(var #{check-disabled id-fn} pipeline)])
  => (fn [s]
       (and (string? s)
            (re-find #"check_disabled = " s)
            (re-find #"\[\"check_disabled\"\]" s)
            (re-find #"id_fn = " s)
            (re-find #"\[\"id_fn\"\]" s))))

(fact "Ruby key helpers guard nil receivers"
  (l/emit-as :ruby
   '[(do
       (x:get-key obj "a")
       (x:get-key obj "a" nil)
       (x:set-key obj "a" 1)
       (x:has-key? obj "a"))])
  => (fn [s]
       (and (string? s)
            (re-find #"obj__.* = obj" s)
            (re-find #"if nil == obj__" s)
            (re-find #"obj = \{\}" s))))

(fact "Ruby x:get-key evaluates side-effecting receivers once"
  (l/emit-as :ruby
   ['(x:get-key (remove-listener container "a1") "meta" nil)])
  => (fn [s]
       (and (string? s)
            (= 1 (count (re-seq #"remove_listener" s))))))

(fact "Ruby key helpers treat arrays differently from hashes"
  (l/emit-as :ruby
   '[(do
       (x:get-key arr "a" 1)
       (x:get-key arr 0 nil)
       (x:has-key? arr "a")
       (x:has-key? arr 0)
       (x:set-key arr "a" 1)
       (x:set-key arr 0 1)
       (x:del-key arr "a")
       (x:del-key arr 0))])
  => (fn [s]
       (and (string? s)
            (re-find #"is_a\?\(Array\)" s)
            (re-find #"is_a\?\(Integer\)" s)
            (re-find #"delete_at" s)
            (not (re-find #"\.key\?" s)))))

(fact "Ruby x:obj-clone hard-links to common-data for stable value semantics"
  (get-in fn-ruby/+ruby-lu+ [:x-obj-clone])
  => {:raw 'xt.lang.common-data/obj-clone
      :emit :hard-link})

(fact "Ruby for:object guards nil receivers"
  (l/emit-as :ruby
   '[(for:object [[k v] obj]
        (puts k)
        (puts v))])
  => "for k, v in  obj || {}\n  puts k\n  puts v\nend\nnil")

(fact "Ruby for:array captures nested callable values without wrapping the loop body"
  (l/emit-as :ruby
   '[(for:array [value values]
       (when flag
         (return value))
       (. out
          (push
           (fn [_]
             (return value)))))])
  => (fn [s]
       (and (string? s)
            (re-find #"for value in values" s)
            (re-find #"return value__capture__" s)
            (re-find #"\.call\(value\)" s)
            (re-find #"return value" s)
            (not (re-find #"arr__|idx__" s))
            (not (re-find #"->\(value\)\s*\{" s)))))

(fact "Ruby promise overrides hard-link spec-promise forms to concurrent-ruby"
  (select-keys fn-ruby/+ruby-promise+
               [:x-async-run
                :x-promise
                :x-promise-all
                :x-promise-then
                :x-promise-catch
                :x-promise-finally
                :x-promise-native?
                :x-with-delay])
  => {:x-async-run       {:raw 'ruby.lang.concurrent-promise/async-run
                          :emit :hard-link}
      :x-promise         {:raw 'ruby.lang.concurrent-promise/promise
                          :emit :hard-link}
      :x-promise-all     {:raw 'ruby.lang.concurrent-promise/promise-all
                          :emit :hard-link}
      :x-promise-then    {:raw 'ruby.lang.concurrent-promise/promise-then
                          :emit :hard-link}
      :x-promise-catch   {:raw 'ruby.lang.concurrent-promise/promise-catch
                          :emit :hard-link}
      :x-promise-finally {:raw 'ruby.lang.concurrent-promise/promise-finally
                          :emit :hard-link}
       :x-promise-native? {:raw 'ruby.lang.concurrent-promise/promise-native?
                           :emit :hard-link}
       :x-with-delay      {:raw 'ruby.lang.concurrent-promise/with-delay
                           :emit :hard-link}})

(fact "Ruby def forms with arglists emit wrapper methods for direct aliases"
  (l/emit-as :ruby
   '[(def ^{:arglists '([value])} forward other-fn)])
  => (fn [s]
       (and (string? s)
            (re-find #"def forward\(value\)" s)
            (re-find #"return other_fn\(value\)" s))))

(fact "Ruby throw wraps payloads in raise(...)"
  (l/emit-as :ruby
   '[(x:throw {:status "error" :tag "db/op-not-available"})])
  => "raise({\"status\" => \"error\", \"tag\" => \"db/op-not-available\"})")

(fact "Ruby local normalization rewrites question-mark locals in bodies"
  (l/emit-as :ruby
   '[(defn process [flag]
       (var update? flag)
       (when update?
         (return true))
       (return false))])
  => (fn [s]
       (and (string? s)
            (re-find #"update_p = " s)
            (re-find #"if update_p" s))))

(fact "Ruby x:global helpers write through !:G without assigning to the globals expression"
  (fn-ruby/ruby-tf-x-global-set '(x:global-set XT 1))
  => '(:= (. !:G ["XT"]) 1)

  (fn-ruby/ruby-tf-x-global-del '(x:global-del XT))
  => '(:= (. !:G ["XT"]) nil)

  (fn-ruby/ruby-tf-x-global-has? '(x:global-has? XT))
  => '(not (x:nil? (. !:G ["XT"]))))

(fact "Ruby prototype helpers use the _xt_proto fallback layout"
  (fn-ruby/ruby-tf-prototype-create '(proto:create {"describe" 1}))
  => '{"describe" 1}

  (fn-ruby/ruby-tf-prototype-get '(proto:get obj))
  => '(x:get-key obj "_xt_proto" nil)

  (fn-ruby/ruby-tf-prototype-method '(proto:method obj "describe"))
  => '(:? (not= nil (x:get-key obj "describe" nil))
          (x:get-key obj "describe" nil)
          (x:get-key (or (proto:get obj) {}) "describe" nil)))

^{:refer hara.model.spec-ruby/ruby-symbol :added "4.1"}
(fact "emit ruby symbol"

  (spec-ruby/ruby-symbol :a spec-ruby/+grammar+ {})
  => ":a"
  (spec-ruby/ruby-symbol 'a spec-ruby/+grammar+ {})
  => "a"
  (spec-ruby/ruby-symbol 'respond_to? spec-ruby/+grammar+ {})
  => "respond_to?"
  (spec-ruby/ruby-symbol 'HEX spec-ruby/+grammar+
                         {:module {:id 'xt.lang.common-color
                                   :code {'HEX {:op-key :def}}}})
  => "($__globals__ ||= {})[\"xt_lang_common_color____HEX\"]"
  (spec-ruby/ruby-symbol 'xt.lang.common-color/HEX spec-ruby/+grammar+
                         {:module {:id 'xt.lang.common-color
                                   :code {'HEX {:op-key :def}}}})
  => "($__globals__ ||= {})[\"xt_lang_common_color____HEX\"]")

^{:refer hara.model.spec-ruby/ruby-destructure-key :added "4.1"}
(fact "rewrites destructuring keys for ruby")

^{:refer hara.model.spec-ruby/ruby-method-ref :added "4.1"}
(fact "creates ruby method references")

^{:refer hara.model.spec-ruby/ruby-symbol-global :added "4.1"}
(fact "emit ruby global symbol"

  (spec-ruby/ruby-symbol-global '!:G spec-ruby/+grammar+ {})
  => '(:- "($__globals__ ||= {})")

  (spec-ruby/ruby-symbol-global 'XT spec-ruby/+grammar+ {})
  => '(. (:- "($__globals__ ||= {})") ["XT"])
  (spec-ruby/ruby-symbol-global 'xt.lang.common-color/HEX spec-ruby/+grammar+ {})
  => '(. (:- "($__globals__ ||= {})") ["xt_lang_common_color____HEX"]))

^{:refer hara.model.spec-ruby/ruby-var :added "4.1"}
(fact "emit ruby variable"
 
  (spec-ruby/ruby-var '(var a 1))
  => '(:= a 1)

  (let [out (spec-ruby/ruby-var '(var #{a-var b-var} opts))]
    [(first out)
     (mapv #(nth % 1) (rest out))
     (mapv #(nth (nth % 2) 2) (rest out))])
  => '[do* [a-var b-var] ["a_var" "b_var"]]

  (spec-ruby/ruby-var '(var [a b] values))
  => '(do* (:= a (x:get-idx values (x:offset 0) nil))
           (:= b (x:get-idx values (x:offset 1) nil)))

  (l/emit-as :ruby ['(var #{a-var b-var} opts)])
  => (fn [s]
       (and (string? s)
            (re-find #"a_var = " s)
            (re-find #"\[\"a_var\"\]" s)
            (re-find #"b_var = " s)
            (re-find #"\[\"b_var\"\]" s))))

^{:refer hara.model.spec-ruby/ruby-map :added "4.1"}
(fact "emit ruby hash"

  (l/emit-as :ruby '[{:a 1 :b 2}])
  => "{\"a\" => 1, \"b\" => 2}"

  (l/emit-as :ruby '[{:ref-links {} :first-name "Root"}])
  => "{\"ref_links\" => {}, \"first_name\" => \"Root\"}")

^{:refer hara.model.spec-ruby/ruby-emit-args :added "4.1"}
(fact "emits ruby argument lists")

^{:refer hara.model.spec-ruby/ruby-div :added "4.1"}
(fact "emits ruby division")

^{:refer hara.model.spec-ruby/ruby-invoke :added "4.1"}
(fact "emits ruby invocations")

^{:refer hara.model.spec-ruby/ruby-dot :added "4.1"}
(fact "emits ruby dot access")

^{:refer hara.model.spec-ruby/ruby-emit-range :added "4.1"}
(fact "emits ruby ranges")

^{:refer hara.model.spec-ruby/ruby-defn- :added "4.1"}
(fact "emits private ruby functions")

^{:refer hara.model.spec-ruby/ruby-defn :added "4.1"}
(fact "emit ruby function definition"

  (l/emit-as :ruby '[(defn add [a b] (return (+ a b)))])
  => "def add(a,b)\n  return a + b\nend")

^{:refer hara.model.spec-ruby/ruby-defgen :added "4.1"}
(fact "emits ruby generator definitions with deterministic iterator bindings"
  (spec-ruby/ruby-defgen '(defgen items [xs] (yield xs)))
  => '(defn- items [xs]
        (return
         (x:iter-generator
          (fn [items__iter__]
            (. items__iter__ (<< xs))))))

  (spec-ruby/ruby-defgen
   '(defgen items [items__iter__] (yield items__iter__)))
  => '(defn- items [items__iter__]
        (return
         (x:iter-generator
          (fn [items__iter____2]
            (. items__iter____2 (<< items__iter__)))))))

^{:refer hara.model.spec-ruby/ruby-fn :added "4.1"}
(fact "basic transform for ruby blocks"

  (spec-ruby/ruby-fn '(fn [a] (+ a 1)))
  => '(:- "->(" "a" ") {\n" "(do (+ a 1))" "\n}"))

^{:refer hara.model.spec-ruby/tf-for-array :added "4.1"}
(fact "transforms for:array loops using native Ruby iteration"
  (spec-ruby/tf-for-array
   '(for:array [value values]
      (puts value)))
  => '(do
        (for [value :in values]
          (puts value))
        nil)

  (spec-ruby/tf-for-array
   '(for:array [[i value] values]
      (puts i)
      (puts value)))
  => '(do
        (for [[value i] :in (. values (each_with_index))]
          (puts i)
          (puts value))
        nil))

^{:refer hara.model.spec-ruby/tf-for-object :added "4.1"}
(fact "transforms for:object loops using native Ruby iteration"
  (spec-ruby/tf-for-object
   '(for:object [[k v] obj]
      (puts k)
      (puts v)))
  => '(do
        (for [[k v] :in (or obj {})]
          (puts k)
          (puts v))
        nil)

  (spec-ruby/tf-for-object
   '(for:object [[_ v] obj]
      (puts v)))
  => '(do
        (for [v :in (. (or obj {}) (values))]
          (puts v))
        nil))

^{:refer hara.model.spec-ruby/tf-for-iter :added "4.1"}
(fact "transforms for:iter loops")

^{:refer hara.model.spec-ruby/tf-for-index :added "4.1"}
(fact "transforms for:index loops without generated temporaries"
  (spec-ruby/tf-for-index
   '(for:index [i [0 3 1]]
      (puts i)))
  => '(do
        (var i 0)
        (while (< i 3)
          (puts i)
          (:= i (+ i 1)))))


^{:refer hara.model.spec-ruby/ruby-string :added "4.1"}
(fact "emits ruby strings")

^{:refer hara.model.spec-ruby/ruby-throw :added "4.1"}
(fact "emits ruby exceptions")

^{:refer hara.model.spec-ruby/ruby-def :added "4.1"}
(fact "emits ruby definitions")


^{:refer hara.model.spec-ruby/ruby-emit-input-rest :added "4.1"}
(fact "emits a Ruby splat parameter"
  (with-redefs [hara.common.emit-common/*emit-fn*
                (fn [symbol _ _] (name symbol))]
    (spec-ruby/ruby-emit-input-rest {:symbol 'args} nil nil))
  => "*args")
