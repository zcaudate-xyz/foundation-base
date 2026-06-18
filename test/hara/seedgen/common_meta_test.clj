(ns hara.seedgen.common-meta-test
  (:use code.test)
  (:require [hara.seedgen.common-meta :refer :all]))

^{:refer hara.seedgen.common-meta/normalize-runtime-lang :added "4.1"}
(fact "normalizes language identifiers to canonical keywords"
  (normalize-runtime-lang :js) => :js
  (normalize-runtime-lang "js") => :js
  (normalize-runtime-lang 'js) => :js
  (normalize-runtime-lang :ruby) => :rb)

^{:refer hara.seedgen.common-meta/runtime-lang-config :added "4.1"}
(fact "returns the configuration map for a runtime language"
  (runtime-lang-config :js)
  => (contains {:script :js
                :dispatch '!.js
                :suffix "js"
                :runtime :basic
                :check-mode :realtime})

  (runtime-lang-config :ruby)
  => (contains {:script :ruby
                :dispatch '!.rb
                :suffix "rb"})

  (runtime-lang-config :unknown) => nil)

^{:refer hara.seedgen.common-meta/runtime-script-lang :added "4.1"}
(fact "returns the script keyword for a runtime language"
  (runtime-script-lang :js) => :js
  (runtime-script-lang :rb) => :ruby
  (runtime-script-lang :unknown) => nil)

^{:refer hara.seedgen.common-meta/runtime-dispatch-symbol :added "4.1"}
(fact "returns the dispatch symbol for a runtime language"
  (runtime-dispatch-symbol :js) => '!.js
  (runtime-dispatch-symbol :dart) => '!.dt
  (runtime-dispatch-symbol :unknown) => nil)

^{:refer hara.seedgen.common-meta/runtime-type :added "4.1"}
(fact "returns the runtime type for a language"
  (runtime-type :js) => :basic
  (runtime-type :dart) => :twostep
  (runtime-type :unknown) => nil)

^{:refer hara.seedgen.common-meta/runtime-check-mode :added "4.1"}
(fact "returns the check mode for a runtime language"
  (runtime-check-mode :js) => :realtime
  (runtime-check-mode :dart) => :batched
  (runtime-check-mode :unknown) => nil)

^{:refer hara.seedgen.common-meta/runtime-suite-groups :added "4.1"}
(fact "groups languages by their check mode"
  (runtime-suite-groups [:js :dart :python])
  => {:batched [:dart]
      :realtime [:js :python]}

  (runtime-suite-groups [:ruby :r :scheme])
  => {:realtime [:r :rb :scheme]})

^{:refer hara.seedgen.common-meta/runtime-lang-suffix :added "4.1"}
(fact "returns the file suffix for a runtime language"
  (runtime-lang-suffix :js) => "js"
  (runtime-lang-suffix :ruby) => "rb"
  (runtime-lang-suffix :r) => "r"
  (runtime-lang-suffix :unknown) => nil)
