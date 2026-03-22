(ns indigo.build.build-vite
  (:require [std.block.heal.core :as heal]
            [std.fs :as fs]
            [std.lang :as l]
            [std.lib.env :as env]
            [std.lib.os :as os]
            [std.make :as make :refer [def.make]]
            [std.text.diff :as diff]))

(def.make CODE_DEV
  {:tag      "indigo"
   :build    ".build/code.dev/src"
   :triggers #{"indigo"}
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
  nil)



(comment
  (`make/build-all
   
   `CODE_DEV
   )
  
  (std.make/build-all indigo.build.build-vite/CODE_DEV)
  (std.make/run:dev indigo.build.build-vite/CODE_DEV)
  
  (env/p (os/sh {:args ["yarn" "create" "vite" "my-project" "--template" "react"]
              }))

  (env/p (os/sh {:root ".build"
              :args ["yarn" "create" "vite" "indigo" "--template" "react"]}))
  (env/p (os/sh {:root ".build/indigo"
              :args ["pnpm" "install"]
              :inherit true}))
  (env/p (os/sh {:root ".build/indigo"
              :args ["npm" "install"]
              :inherit true}))
  (env/p (os/sh {:root ".build/indigo"
              :args ["yarn" "install"]}))
  (env/p (os/sh {:root ".build/indigo"
              :args ["yarn" "add" "@measured/puck"]}))
  (env/p (os/sh {:root ".build/indigo"
              :args ["yarn" "add" "lucide-react"]}))
  (env/p (os/sh {:root ".build/indigo"
              :args ["yarn" "add" "react-dnd"]}))
  (env/p (os/sh {:root ".build/indigo"
              :args ["yarn" "add" "@dnd-kit/core"]}))
  (env/p (os/sh {:root ".build/indigo"
              :args ["yarn" "add" "@radix-ui/themes"]}))
  (env/p (os/sh {:root ".build/indigo"
              :args ["yarn" "add" "@xtalk/figma-ui"]}))
  (env/p (os/sh {:root ".build/indigo"
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
             (os/sh {:root ".build/indigo"
                    :args ["npm" "run" "dev"]
                    :inherit true}))))

  (defn stop-server
    []
    (swap! +server+
           (fn [m]
             (when m
               (os/sh {:root ".build/indigo"
                      :args ["yarn" "dev"]
                      :inherit true})))))

  
  )
