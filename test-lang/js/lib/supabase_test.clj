(ns js.lib.supabase-test
  (:require [js.lib.supabase :refer :all])
  (:use code.test))

^{:refer js.lib.supabase/js-rpc :added "4.0" :unchecked true}
(fact "creates a js rpc call")
