(ns documentation.std-lib-apply
  (:use code.test))

[[:chapter {:title "Introduction"}]]

[[:section {:title "Overview"}]]

"`std.lib.apply` provides applicative invocation, allowing functions to be applied within a configurable runtime context.

Common uses include:

- Invoking a form with a default runtime
- Applying arguments within an explicit context
- Building host applicatives that wrap plain functions"

[[:chapter {:title "Apply within a context" :link "std.lib.apply"}]]

"`apply-in` runs an applicative within a given runtime. `apply-as` picks the runtime automatically."

[[:api {:namespace "std.lib.apply"
        :only [apply-in apply-as invoke-as]}]]

[[:chapter {:title "Host applicatives" :link "std.lib.apply"}]]

"`host-applicative` constructs a plain-function applicative that does not need an external context."

[[:api {:namespace "std.lib.apply"
        :only [host-applicative]}]]
