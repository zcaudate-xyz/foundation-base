(ns hara.lang.model-annex.spec-php
  (:require [hara.lang.base.book :as book]
            [hara.lang.base.emit :as emit]
            [hara.lang.base.grammar :as grammar]
            [hara.lang.base.script :as script]
            [hara.lang.model.spec-xtalk]
            [hara.lang.model-annex.spec-xtalk.fn-php :as fn]
            [std.lib.collection :as collection]))

(def +features+
  (let [base        (grammar/build :exclude [:pointer :block :data-range])
        base-keys   (set (keys base))
        fn-override (select-keys fn/+php+ base-keys)
        fn-extend   (apply dissoc fn/+php+ (keys fn-override))]
    (-> base
        (merge (grammar/build-xtalk))
        (grammar/build:override
         {:and {:raw "&&"}
          :or  {:raw "||"}
          :not {:raw "!"}
          :eq  {:raw "=="}
          :neq {:raw "!="}
          :gt  {:raw ">"}
          :lt  {:raw "<"}
          :gte {:raw ">="}
          :lte {:raw "<="}})
        (grammar/build:override fn-override)
        (grammar/build:extend
         (merge fn-extend
                {:concat {:op :concat
                          :symbol #{'concat}
                          :raw "."
                          :emit :infix
                          :value true}
                 :echo   {:op :echo
                          :symbol #{'echo}
                          :raw "echo"
                          :emit :prefix}
                 :die    {:op :die
                          :symbol #{'die}
                          :raw "die"
                          :emit :prefix}})))))

(def +template+
  (->> {:banned #{:keyword}
        :allow  {:assign #{:symbol}}
        :default {:common   {:statement ";"}
                  :invoke   {:apply "->"
                             :static "::"}
                  :function {:raw "function"}}
        :block {:script {:start "<?php\n"
                         :end   "\n?>"}
                :try    {:control {:catch {:parameter {:start "("
                                                     :end   ")"
                                                     :sep   ""}}}}}
        :token {:nil     {:as "null"}
                :boolean {:as (fn [b] (if b "true" "false"))}
                :string  {:quote :single}}
        :data  {:vector    {:start "[" :end "]" :space ", "}
                :map       {:start "[" :end "]" :space ", "}
                :map-entry {:assign " => "
                            :space  ""
                            :keyword :string}}
        :define {:def       {:raw ""}
                 :defglobal {:raw ""}}}
       (collection/merge-nested (emit/default-grammar))))

(def +grammar+
  (grammar/grammar :php
    (grammar/to-reserved +features+)
    +template+))

(def +book+
  (book/book {:lang :php
              :parent :xtalk
              :meta (book/book-meta {})
              :grammar +grammar+}))

(def +init+
  (script/install +book+))
