(ns documentation.lib-oshi
  (:use code.test))

[[:hero {:title "lib.oshi"
         :subtitle "cross-platform hardware, operating-system, and process information"
         :lead "Read OSHI system information as raw Java objects or converted Clojure data for diagnostics and monitoring."
         :actions [{:label "All libraries" :href "index.html"}]}]]

[[:chapter {:title "Overview" :link "overview"}]]

"The public functions create an OSHI `SystemInfo` value and navigate to hardware or operating-system sections. Bind `*raw*` to control whether values are returned directly or converted through `std.object`."

[[:chapter {:title "Walkthrough" :link "walkthrough"}]]

[[:section {:title "Read system information"}]]

(comment
  (require '[lib.oshi :as oshi])

  (binding [oshi/*raw* false]
    {:computer (oshi/computer-system)
     :cpu (oshi/cpu)
     :memory (oshi/memory)
     :operating-system (oshi/os)}))

[[:section {:title "Inspect devices and interfaces"}]]

(comment
  (binding [oshi/*raw* false]
    {:disks (oshi/fs)
     :network (oshi/network-ifs)
     :power (oshi/power)
     :sensors (oshi/sensors)
     :usb (oshi/usb)}))

[[:section {:title "Inspect processes"}]]

(comment
  (oshi/process-id)
  (binding [oshi/*raw* false]
    (oshi/process (oshi/process-id)))
  (binding [oshi/*raw* false]
    (oshi/list-processes 20 :cpu)))

[[:chapter {:title "API" :link "api"}]]

[[:api {:namespace "lib.oshi"}]]
[[:api {:namespace "lib.oshi.interop"}]]
