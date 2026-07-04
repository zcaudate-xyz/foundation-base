(ns hara.runtime.jocl.env
  "Environment helpers for the JOCL runtime.

   The functions here deliberately avoid importing `org.jocl.CL` so that
   this namespace can be loaded even when the native OpenCL library is
   not installed on the host.

   See: https://github.com/zcaudate-xyz/foundation-base/actions/runs/28710051629/job/85142005252"
  {:added "4.1"})

(defn opencl-available?
  "Returns true when the OpenCL library can be loaded and at least one
   platform can be enumerated.

   This is used by `code.test` skips so that JOCL tests are reported as
   skipped rather than errored on machines without OpenCL."
  {:added "4.1"}
  ([]
   (try
     (let [cl         (Class/forName "org.jocl.CL")
           platform-id-class (Class/forName "org.jocl.cl_platform_id")
           platforms-class   (.getClass (java.lang.reflect.Array/newInstance platform-id-class 0))
           method     (.getMethod cl "clGetPlatformIDs"
                                  (into-array Class [Integer/TYPE
                                                     platforms-class
                                                     (Class/forName "[I")]))
           num        (int-array 1)]
       (>= (.invoke method nil (object-array [(int 0) nil num])) 0)
       (pos? (aget num 0)))
     (catch UnsatisfiedLinkError _
       false)
     (catch LinkageError _
       false)
     (catch Throwable _
       false))))

(defn cl-field
  "Reflectively reads a static field from `org.jocl.CL`.

   Useful in test namespaces that need CL constants but must not import
   `org.jocl.CL` (because that would trigger class initialization on
   machines without OpenCL)."
  {:added "4.1"}
  [^String name]
  (try
    (clojure.lang.Reflector/getStaticField (Class/forName "org.jocl.CL") name)
    (catch Throwable _
      nil)))

(defmacro with-stubs
  "Defines no-op vars for `syms` when OpenCL is not available.

   Test namespaces use this so they can still compile on machines that do
   not have the native OpenCL library installed.  When OpenCL is available
   the macro expands to nothing and the real functions are used instead."
  {:added "4.1"}
  [& syms]
  (when-not (opencl-available?)
    `(do ~@(map (fn [s]
                  `(def ~s (fn [& _#] nil)))
                syms))))

(defmacro with-script-stubs
  "Provides no-op `defn.c` and `define.c` macros when OpenCL is not
   available.  Test namespaces call this before any script forms so the
   file can compile even when the JOCL runtime is not loaded."
  {:added "4.1"}
  []
  (when-not (opencl-available?)
    `(do
       (defmacro ~'defn.c [& _#] nil)
       (defmacro ~'define.c [& _#] nil))))

(defmacro when-available
  "Executes `body` only when the OpenCL native library is available.

   This is used in test namespaces around top-level `(l/script- ...)` and
   `defn.c` forms.  When OpenCL is missing the body is dropped entirely,
   so the script macros never see the unavailable runtime."
  {:added "4.1"}
  [& body]
  (when (opencl-available?)
    `(do ~@body)))
