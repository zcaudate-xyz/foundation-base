(ns code.dev.build.build-vite
  (:require [std.make :as make :refer [def.make]]
            [std.lib :as h]
            [std.lang :as l]
            [std.fs :as fs]
            [code.heal :as heal]
            [std.text.diff :as diff]))

(def.make CODE_DEV
  {:tag      "code.dev"
   :build    ".build/code.dev/src"
   :default  [{:type   :module.root
               :target "."
               :lang   :js
               :main   'code.dev.index-main
               :emit   {:code   {:label true
                                 :link  {:root-prefix  "@"
                                         :path-separator "/"
                                         :path-suffix  ".jsx"
                                         :ns-label  {'code.dev.index-main "main"}
                                         :ns-suffix {#"^xt" ".js"}}}}}]})

(def +init+
  (make/triggers-set
   CODE_DEV
   #{"code.dev"}))



(comment
  (`make/build-all
   
   `CODE_DEV
   )
  
  (std.make/build-all code.dev.build.build-vite/CODE_DEV)
  (std.make/run:dev code.dev.build.build-vite/CODE_DEV)
  
  (h/p (h/sh {:args ["yarn" "create" "vite" "my-project" "--template" "react"]
              }))

  (h/p (h/sh {:root ".build"
              :args ["yarn" "create" "vite" "code.dev" "--template" "react"]}))
  (h/p (h/sh {:root ".build/code.dev"
              :args ["pnpm" "install"]
              :inherit true}))
  (h/p (h/sh {:root ".build/code.dev"
              :args ["npm" "install"]
              :inherit true}))
  (h/p (h/sh {:root ".build/code.dev"
              :args ["yarn" "install"]}))
  (h/p (h/sh {:root ".build/code.dev"
              :args ["yarn" "add" "@measured/puck"]}))
  (h/p (h/sh {:root ".build/code.dev"
              :args ["yarn" "add" "lucide-react"]}))
  (h/p (h/sh {:root ".build/code.dev"
              :args ["yarn" "add" "react-dnd"]}))
  (h/p (h/sh {:root ".build/code.dev"
              :args ["yarn" "add" "@dnd-kit/core"]}))
  (h/p (h/sh {:root ".build/code.dev"
              :args ["yarn" "add" "@radix-ui/themes"]}))
  (h/p (h/sh {:root ".build/code.dev"
              :args ["yarn" "add" "@xtalk/figma-ui"]}))
  (h/p (h/sh {:root ".build/code.dev"
              :args ["yarn" "add" "@nextjournal/clojure-mode"]}))
  (spit ".build/code.dev/src/main.jsx"
        (emit-main))

  
  (defn emit-main
    []
    (l/emit-script
     '(code.dev.index-main/main)
     {:lang :js
      :library (l/default-library)
      :module  (l/get-module (l/default-library)
                             :js
                             'code.dev.index-main)
      :emit { ;;:native {:suppress true}
             :lang/jsx false}
      :layout :full}))

  (defonce +server+ (atom nil))
  
  (defn start-server
    []
    (swap! +server+
           (fn [m]           
             (h/sh {:root ".build/code.dev"
                    :args ["npm" "run" "dev"]
                    :inherit true}))))

  (defn stop-server
    []
    (swap! +server+
           (fn [m]
             (when m
               (h/sh {:root ".build/code.dev"
                      :args ["yarn" "dev"]
                      :inherit true})))))

  
  )
