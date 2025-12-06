(ns indigo.build.build-vite
  (:require [std.make :as make :refer [def.make]]
            [std.lib :as h]
            [std.lang :as l]
            [std.fs :as fs]
            [std.block.heal.core :as heal]
            [std.text.diff :as diff]))

(def.make CODE_DEV
  {:tag      "indigo"
   :build    ".build/code.dev/src"
   :default  [{:type   :module.root
               :target "."
               :lang   :js
               :main   'indigo.index-main
               :emit   {:code   {:label true
                                 :link  {:root-prefix  "@"
                                         :path-separator "/"
                                         :path-suffix  ".jsx"
                                         :ns-label  {'indigo.index-main "main"}
                                         :ns-suffix {#"^xt" ".js"}}}}}]})

(def +init+
  (make/triggers-set
   CODE_DEV
   #{"indigo"}))



(comment
  (`make/build-all
   
   `CODE_DEV
   )
  
  (std.make/build-all indigo.build.build-vite/CODE_DEV)
  (std.make/run:dev indigo.build.build-vite/CODE_DEV)
  
  (h/p (h/sh {:args ["yarn" "create" "vite" "my-project" "--template" "react"]
              }))

  (h/p (h/sh {:root ".build"
              :args ["yarn" "create" "vite" "indigo" "--template" "react"]}))
  (h/p (h/sh {:root ".build/indigo"
              :args ["pnpm" "install"]
              :inherit true}))
  (h/p (h/sh {:root ".build/indigo"
              :args ["npm" "install"]
              :inherit true}))
  (h/p (h/sh {:root ".build/indigo"
              :args ["yarn" "install"]}))
  (h/p (h/sh {:root ".build/indigo"
              :args ["yarn" "add" "@measured/puck"]}))
  (h/p (h/sh {:root ".build/indigo"
              :args ["yarn" "add" "lucide-react"]}))
  (h/p (h/sh {:root ".build/indigo"
              :args ["yarn" "add" "react-dnd"]}))
  (h/p (h/sh {:root ".build/indigo"
              :args ["yarn" "add" "@dnd-kit/core"]}))
  (h/p (h/sh {:root ".build/indigo"
              :args ["yarn" "add" "@radix-ui/themes"]}))
  (h/p (h/sh {:root ".build/indigo"
              :args ["yarn" "add" "@xtalk/figma-ui"]}))
  (h/p (h/sh {:root ".build/indigo"
              :args ["yarn" "add" "@nextjournal/clojure-mode"]}))
  (spit ".build/indigo/src/main.jsx"
        (emit-main))

  
  (defn emit-main
    []
    (l/emit-script
     '(indigo.index-main/main)
     {:lang :js
      :library (l/default-library)
      :module  (l/get-module (l/default-library)
                             :js
                             'indigo.index-main)
      :emit { ;;:native {:suppress true}
             :lang/jsx false}
      :layout :full}))

  (defonce +server+ (atom nil))
  
  (defn start-server
    []
    (swap! +server+
           (fn [m]           
             (h/sh {:root ".build/indigo"
                    :args ["npm" "run" "dev"]
                    :inherit true}))))

  (defn stop-server
    []
    (swap! +server+
           (fn [m]
             (when m
               (h/sh {:root ".build/indigo"
                      :args ["yarn" "dev"]
                      :inherit true})))))

  
  )
