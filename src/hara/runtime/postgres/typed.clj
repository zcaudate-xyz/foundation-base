(ns hara.runtime.postgres.typed
  (:require [hara.runtime.postgres.base.typed :as base]
            [hara.runtime.postgres.base.typed.typed-common :as common]
            [std.lib.foundation :as f]))

(f/intern-in
 common/register-type!
 common/clear-registry!
 base/Type)
