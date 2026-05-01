(ns rt.postgres.typed
  (:require [rt.postgres.base.typed :as base]
            [rt.postgres.base.typed.typed-common :as common]
            [std.lib.foundation :as f]))

(f/intern-in
 common/register-type!
 common/clear-registry!
 base/Type)
