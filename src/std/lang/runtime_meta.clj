(ns std.lang.runtime-meta)

(def +runtime-lang-config+
  {:js     {:script :js
            :dispatch '!.js
            :suffix "js"
            :runtime :basic
            :check-mode :realtime}
   :lua    {:script :lua
            :dispatch '!.lua
            :suffix "lua"
            :runtime :basic
            :check-mode :realtime}
   :python {:script :python
            :dispatch '!.py
            :suffix "python"
            :runtime :basic
            :check-mode :realtime}
   :scheme {:script :scheme
            :dispatch '!.scheme
            :suffix "scheme"
            :runtime :basic
            :check-mode :realtime}
   :elisp  {:script :elisp
            :dispatch '!.elisp
            :suffix "elisp"
            :runtime :basic
            :check-mode :realtime}
   :r      {:script :r
            :dispatch '!.R
            :suffix "r"
            :runtime :basic
            :check-mode :realtime}
   :rb     {:script :ruby
            :dispatch '!.rb
            :suffix "rb"
            :runtime :basic
            :check-mode :realtime}
   :dart   {:script :dart
            :dispatch '!.dt
            :suffix "dt"
            :runtime :twostep
            :check-mode :batched}
   :php    {:script :php
            :dispatch '!.php
            :suffix "php"
            :runtime :basic
            :check-mode :realtime}
   :go     {:script :go
            :dispatch '!.go
            :suffix "go"
            :runtime :twostep
            :check-mode :batched}})

(def +runtime-lang-aliases+
  {:ruby :rb})

(def +runtime-executable-langs+
  #{:js :lua :python :scheme :elisp :r :rb :php})

(def +runtime-langs+
  (->> (keys +runtime-lang-config+)
       sort
       vec))

(defn normalize-runtime-lang
  [lang]
  (let [lang (cond
               (keyword? lang) lang
               (symbol? lang) (keyword (name lang))
               (string? lang) (keyword lang)
               :else lang)]
    (or (get +runtime-lang-aliases+ lang)
        lang)))

(defn runtime-lang-config
  [lang]
  (get +runtime-lang-config+ (normalize-runtime-lang lang)))

(defn runtime-script-lang
  [lang]
  (:script (runtime-lang-config lang)))

(defn runtime-dispatch-symbol
  [lang]
  (:dispatch (runtime-lang-config lang)))

(defn runtime-type
  [lang]
  (:runtime (runtime-lang-config lang)))

(defn runtime-check-mode
  [lang]
  (:check-mode (runtime-lang-config lang)))

(defn runtime-suite-groups
  [langs]
  (->> langs
       (map normalize-runtime-lang)
       distinct
       sort
       (group-by runtime-check-mode)
       (into (sorted-map)
             (map (fn [[mode entries]]
                    [mode (vec entries)])))))

(defn runtime-lang-suffix
  [lang]
  (:suffix (runtime-lang-config lang)))
