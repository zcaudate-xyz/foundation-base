(ns hara.model.annex.spec-rust-test
  (:use code.test)
  (:require [hara.model.annex.spec-rust :refer :all]))

^{:refer hara.model.annex.spec-rust/rst-typesystem :added "4.1"}
(fact "emits rust typesystem syntax")

^{:refer hara.model.annex.spec-rust/rst-vector :added "4.1"}
(fact "emits rust vector syntax")

^{:refer hara.model.annex.spec-rust/rst-attributes :added "4.1"}
(fact "emits rust attributes")

^{:refer hara.model.annex.spec-rust/rst-defenum :added "4.1"}
(fact "emits rust enum definitions")

^{:refer hara.model.annex.spec-rust/rst-deftrait :added "4.1"}
(fact "emits rust trait definitions")

^{:refer hara.model.annex.spec-rust/rst-defimpl :added "4.1"}
(fact "emits rust impl definitions")

^{:refer hara.model.annex.spec-rust/rst-new :added "4.1"}
(fact "emits rust new expressions")

^{:refer hara.model.annex.spec-rust/rst-exec :added "4.1"}
(fact "emits rust exec blocks")

^{:refer hara.model.annex.spec-rust/rst-defstruct :added "4.1"}
(fact "emits rust struct definitions")
