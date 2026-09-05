(ns lang.model.annex.spec-php
  (:require [lang.common.book :as book]
            [lang.base.emit :as emit]
            [lang.base.emit-common :as common]
            [lang.base.grammar :as grammar]
            [lang.core.script :as script]
            [lang.model.spec-xtalk]
            [lang.model.annex.spec-xtalk.fn-php :as fn]
            [lang.model.annex.spec-php.rewrite :as rewrite]
            [std.lib.collection :as collection]))

(defn php-emit-input-rest
  [{:keys [symbol]} grammar mopts]
  (str "..." (common/*emit-fn* symbol grammar mopts)))

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
          :eq  {:raw "==="}
          :neq {:raw "!=="}
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
                          :emit :prefix}
                 :foreach {:op :foreach
                           :symbol '#{foreach}
                           :raw "foreach"
                           :type :block
                           :block {:main #{:parameter :body}}}})))))

(def +template+
  (->> {:banned #{:keyword}
        :allow  {:assign #{:symbol}}
        :default {:common   {:statement ";"}
                  :invoke   {:apply "->"
                             :static "::"}
                  :function {:raw "function"
                             :args {:rest #'php-emit-input-rest}}}
        :block {:script {:start "<?php\n"
                         :end   "\n?>"}
                :try    {:control {:catch {:parameter {:start "("
                                                     :end   ")"
                                                     :sep   ""}}}}
                :foreach {:parameter {:start " (" :end ")" :sep " as "}
                          :body      {:start " {" :end "}"}}}
        :token {:nil     {:as "null"}
                :boolean {:as (fn [b] (if b "true" "false"))}
                :string  {:quote :single}}
        :rewrite {:staging [#'rewrite/php-rewrite-stage]}
        :data  {:vector    {:start "[" :end "]" :space ", "}
                :map       {:start "[" :end "]" :space ", "}
                :map-entry {:assign " => "
                            :space  ""
                            :keyword :string}}
        :define {:def       {:raw ""}
                 :defglobal {:raw ""}
                 :shorthand true}}
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
