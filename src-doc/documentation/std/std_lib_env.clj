(ns documentation.std-lib-env
  (:use code.test))

[[:chapter {:title "Introduction"}]]

[[:section {:title "Overview"}]]

"`std.lib.env` provides environment and system utilities including:

- Namespace introspection
- Resource management
- Development helpers
- Pretty printing utilities
- Debug facilities
"

[[:chapter {:title "Namespace Utilities" :link "std.lib.env"}]]

[[:api {:namespace "std.lib.env"
        :only [ns-sym ns-get require dev?]}]]

[[:chapter {:title "Resource Management" :link "std.lib.env"}]]

[[:api {:namespace "std.lib.env"
        :only [sys:resource sys:resource-cached sys:resource-content sys:ns-url sys:ns-file sys:ns-dir]}]]

[[:chapter {:title "Pretty Printing" :link "std.lib.env"}]]

[[:api {:namespace "std.lib.env"
        :only [p pr pp-str pp-fn pl prf]}]]

[[:chapter {:title "Debug Utilities" :link "std.lib.env"}]]

[[:api {:namespace "std.lib.env"
        :only [dbg-print dbg-global dbg:add-filters dbg:remove-filters wrap-print]}]]

[[:chapter {:title "Local State" :link "std.lib.env"}]]

[[:api {:namespace "std.lib.env"
        :only [local:set local:clear local]}]]

[[:chapter {:title "Error Handling" :link "std.lib.env"}]]

[[:api {:namespace "std.lib.env"
        :only [throwable-string close]}]]
