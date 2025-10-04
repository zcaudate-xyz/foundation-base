(ns js.lib.supabase-test
  (:use code.test)
  (:require [js.lib.supabase :refer :all]))

^{:refer js.lib.supabase/js-rpc :added "4.0"}
(fact "creates a js rpc call")
