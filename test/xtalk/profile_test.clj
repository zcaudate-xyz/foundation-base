(ns xtalk.profile-test
  (:require [std.lang.base.grammar-xtalk-system :as grammar-xtalk]
            [std.lang.base.grammar :as grammar])
  (:use code.test))

(defn all-grammar-xtalk-ops
  []
  (->> grammar-xtalk/+xtalk-profile->ops+
       vals
       (apply clojure.set/union)))

^{:refer std.lang.base.grammar-xtalk-system/xtalk-profiles :added "4.1"}
(fact "profiles follow the upfront xtalk capability order"
  (grammar-xtalk/xtalk-profiles)
  => [:xtalk-common
      :xtalk-functional
      :xtalk-language-specific
      :xtalk-std-lang-link-specific
      :xtalk-runtime-specific])

^{:refer std.lang.base.grammar-xtalk-system/xtalk-areas :added "4.1"}
(fact "profiles are grouped by implementation area"
  (grammar-xtalk/xtalk-areas)
  => [:common
      :functional
      :language-specific
      :std-lang-link-specific
      :runtime-specific]

  (grammar-xtalk/xtalk-area-profiles :common)
  => [:xtalk-common])

^{:refer std.lang.base.grammar-xtalk-system/xtalk-profile-ops :added "4.1"}
(fact "every grammar xtalk op is classified upfront"
  (set grammar-xtalk/+xtalk-ops+)
  => (all-grammar-xtalk-ops)

  (grammar-xtalk/xtalk-unclassified-ops)
  => [])

^{:refer std.lang.base.grammar/build-xtalk :added "4.1"}
(fact "kernel intrinsic macros pick up the standard macro and value hooks via grammar build"
  (select-keys (get (grammar/build-xtalk) :x-add)
               [:emit :value/template :value/standalone])
  => {:emit :macro
      :value/template #'std.lang.base.grammar-xtalk/tf-add
      :value/standalone true}

  (select-keys (get (grammar/build-xtalk) :x-arr-first)
               [:emit :value/template :value/standalone])
  => {:emit :macro
      :value/template #'std.lang.base.grammar-xtalk/tf-first
      :value/standalone true}

  (select-keys (get (grammar/build-xtalk) :x-obj-clone)
               [:emit :raw :value/standalone])
  => {:emit :hard-link
      :raw 'xt.lang.common-data/obj-clone
      :value/standalone 'xt.lang.common-data/obj-clone})

^{:refer std.lang.base.grammar-xtalk-system/xtalk-grammar-supported-profiles :added "4.1"}
(fact "grammars expose the xtalk profiles they fully support"
  (set (grammar-xtalk/xtalk-grammar-supported-profiles (grammar/build-xtalk)))
  => (set (grammar-xtalk/xtalk-profiles))

  (grammar-xtalk/xtalk-grammar-supported-profiles (grammar/build-min))
  => [])
