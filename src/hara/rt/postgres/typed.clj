(ns hara.rt.postgres.typed
  (:require [hara.rt.postgres.base.typed :as base]
            [hara.rt.postgres.base.typed.typed-common :as common]
            [std.lib.foundation :as f]))

(f/intern-in
 common/register-type!
 common/clear-registry!
 base/Type)
