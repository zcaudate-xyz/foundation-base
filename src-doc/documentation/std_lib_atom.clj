(ns documentation.std-lib-atom
  (:use code.test))

[[:chapter {:title "Introduction"}]]

[[:section {:title "Overview"}]]

"`std.lib.atom` provides enhanced atom operations for managing state with:

- Swapping with return values
- Nested path updates
- Batch operations
- Change tracking
- Derived atoms
"

[[:chapter {:title "Basic Operations" :link "std.lib.atom"}]]

[[:api {:namespace "std.lib.atom"
        :only [swap-return! update-diff]}]]

[[:chapter {:title "Nested Access" :link "std.lib.atom"}]]

[[:api {:namespace "std.lib.atom"
        :only [atom:keys atom:get atom:mget]}]]

[[:chapter {:title "Put and Set" :link "std.lib.atom"}]]

[[:api {:namespace "std.lib.atom"
        :only [atom:put atom:set atom:set-keys]}]]

[[:chapter {:title "Change Tracking" :link "std.lib.atom"}]]

[[:api {:namespace "std.lib.atom"
        :only [atom:set-changed atom:put-changed]}]]

[[:chapter {:title "Batch Operations" :link "std.lib.atom"}]]

[[:api {:namespace "std.lib.atom"
        :only [atom:batch atom:clear atom:delete]}]]

[[:chapter {:title "Derived Atoms" :link "std.lib.atom"}]]

[[:api {:namespace "std.lib.atom"
        :only [atom:cursor atom:derived]}]]
