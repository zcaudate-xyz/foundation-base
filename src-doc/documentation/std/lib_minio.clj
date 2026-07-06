(ns documentation.lib-minio
  (:use code.test))

[[:hero {:title "lib.minio"
         :subtitle "local MinIO arrays for integration and benchmark environments"
         :lead "Start and stop coordinated MinIO instances and inspect the ports allocated to the running array."
         :actions [{:label "All libraries" :href "index.html"}]}]]

[[:chapter {:title "Overview" :link "overview"}]]

"The namespace exposes the public lifecycle functions from `lib.minio.bench`. It is intended for local integration, distributed-storage experiments, and repeatable benchmark environments rather than as a general object-storage client."

[[:chapter {:title "Walkthrough" :link "walkthrough"}]]

[[:section {:title "Start an array"}]]

(comment
  (require '[lib.minio :as minio])

  (def array
    (minio/start-minio-array
     {:count 4}))

  (minio/all-minio-ports))

[[:section {:title "Stop the environment"}]]

"Retain the returned array value and stop it during test teardown. Port discovery is useful when constructing clients after the processes have started."

(comment
  (try
    (run-storage-tests array)
    (finally
      (minio/stop-minio-array array))))

[[:chapter {:title "API" :link "api"}]]

[[:api {:namespace "lib.minio"}]]
[[:api {:namespace "lib.minio.bench"}]]
