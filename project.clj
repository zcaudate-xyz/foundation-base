(defproject xyz.zcaudate/foundation-base "4.0.8"
  :description "base libraries for foundation"
  :url "https://www.github.com/zcaudate-xyz/foundation-base"
  :license  {:name "MIT License"
             :url  "http://opensource.org/licenses/MIT"}
  :aliases
  {"test"  ["exec" "-ep" "(use 'code.test) (def res (run :all)) (System/exit (+ (:failed res) (:thrown res)))"]
   "test-unit"   ["run" "-m" "code.test" "exit"]
   "publish"     ["exec" "-ep" "(use 'code.doc)     (deploy-template :all) (publish :all)"]
   "incomplete"  ["exec" "-ep" "(use 'code.manage)  (incomplete :all) (System/exit 0)"]
   "install"     ["exec" "-ep" "(use 'code.maven)   (install :all {:tag :all}) (System/exit 0)"]
   "deploy"      ["exec" "-ep" "(use 'code.maven)   (deploy :all {:tag :all}) (System/exit 0)"]
   "deploy-lein" ["exec" "-ep" "(use 'code.maven)   (deploy-lein :all {:tag :all}) (System/exit 0)"]
   "push-native-code"  ["run" "-m" "component.task-native-index"]
   "push-c-000-pthreads"        ["run" "-m" "play.c-000-pthreads-hello.build"]
   "push-ngx-000-hello"         ["run" "-m" "play.ngx-000-hello.build"]
   "push-ngx-001-eval"          ["run" "-m" "play.ngx-001-eval.build"]
   "push-tui-000-counter"       ["run" "-m" "play.tui-000-counter.build"]
   "push-tui-001-fetch"         ["run" "-m" "play.tui-001-fetch.build"]
   "push-tui-002-game-of-life"  ["run" "-m" "play.tui-002-game-of-life.build"]}
  :dependencies
  [;; dev
   ;;[org.clojure/clojure "1.11.1"]
   [org.clojure/clojure "1.12.0"]
   [javax.xml.bind/jaxb-api "2.4.0-b180830.0359"]
   [com.sun.xml.bind/jaxb-core "4.0.3"]
   [com.sun.xml.bind/jaxb-impl "4.0.3"]
   [clj-kondo "2025.02.20"]
   
   ;; code.doc
   [markdown-clj/markdown-clj "1.11.8"] ;; not mustache

   ;; code.java.compile
   [org.ow2.asm/asm "9.7.1"]
   
   ;; code.manage
   [org.clojure/tools.reader "1.3.7"]

   ;; lib.aether
   [org.eclipse.aether/aether-api "1.1.0"]
   [org.eclipse.aether/aether-spi "1.1.0"]
   [org.eclipse.aether/aether-util "1.1.0"]
   [org.eclipse.aether/aether-impl "1.1.0"]
   [org.eclipse.aether/aether-connector-basic "1.1.0"]
   [org.eclipse.aether/aether-transport-wagon "1.1.0"]
   [org.eclipse.aether/aether-transport-http "1.1.0"]
   [org.eclipse.aether/aether-transport-file "1.1.0"]
   [org.eclipse.aether/aether-transport-classpath "1.1.0"]
   [org.apache.maven/maven-aether-provider "3.3.9"]
   
   ;; lib.javaosc
   #_
   [com.illposed.osc/javaosc-core "0.8"]
   #_#_#_#_#_
   [org.clojars.technomancy/jmdns "3.2.1"]
   [commons-net "3.0.1"]
   [org.jmdns/jmdns "3.5.1"]
   [commons-net/commons-net "3.9.0"]
   [overtone/at-at "1.2.0"]
   
   ;; lib.lucene
   [org.apache.lucene/lucene-core "9.9.2"]
   [org.apache.lucene/lucene-queryparser "9.9.2"]
   [org.apache.lucene/lucene-analyzers-common "8.11.2"]
   [org.apache.lucene/lucene-suggest "9.9.2"]
   
   ;; lib.openpgp
   [org.bouncycastle/bcprov-jdk15on "1.65"]
   [org.bouncycastle/bcpg-jdk15on "1.65"]

   ;; lib.postgres
   [com.impossibl.pgjdbc-ng/pgjdbc-ng "0.8.9"]
   
   ;; lib.oshi
   [com.github.oshi/oshi-core "6.4.11"]
   
   ;; math.stat
   [net.sourceforge.jdistlib/jdistlib "0.4.5"]
   
   ;; math.infix
   [org.scijava/parsington "3.1.0"]
   
   ;; rt.basic
   [http-kit "2.6.0"]
   
   ;; rt.graal
   [org.graalvm.sdk/graal-sdk "21.2.0"]
   [org.graalvm.truffle/truffle-api "21.2.0"]
   [org.graalvm.js/js "21.2.0"]
   [org.graalvm.js/js-scriptengine "21.2.0"]
   [commons-io/commons-io "2.15.1"]
   
   ;; rt.jep
   [black.ninia/jep "4.2.0"]
   
   ;; rt.libpython
   [clj-python/libpython-clj "2.026"]
   
   
   ;; std.pretty
   [org.clojure/core.rrb-vector "0.1.2"]

   ;; script.css
   [garden "1.3.10"]
   [net.sourceforge.cssparser/cssparser "0.9.30"]
   
   ;; script.graphql
   [district0x/graphql-query "1.0.6"]

   ;; script.toml
   [com.moandjiezana.toml/toml4j "0.7.2"]
   
   ;; script.yaml
   [org.yaml/snakeyaml "1.33" #_"2.0" ;; needed by markdown-clj
    ]

   ;; std.fs.archive
   [org.apache.commons/commons-compress "1.25.0"]

   ;; std.config
   [borkdude/edamame "1.4.24"]

   ;; std.contract
   [metosin/malli "0.17.0"]
   
   ;; std.html
   [org.jsoup/jsoup "1.17.2"]

   ;; std.image
   [com.twelvemonkeys.imageio/imageio-bmp  "3.10.1"]
   [com.twelvemonkeys.imageio/imageio-tiff "3.10.1"]
   [com.twelvemonkeys.imageio/imageio-icns "3.10.1"]
   [com.twelvemonkeys.imageio/imageio-jpeg "3.10.1"]

   ;; std.json
   [com.fasterxml.jackson.core/jackson-core "2.16.1"]
   [com.fasterxml.jackson.core/jackson-databind "2.16.1"]
   [com.fasterxml.jackson.datatype/jackson-datatype-jsr310 "2.16.1"]

   ;; std.math
   [org.apache.commons/commons-math3 "3.6.1"]

   ;; std.text.diff
   [com.googlecode.java-diff-utils/diffutils "1.3.0"]
   

   ;; TESTS - std.object
   [org.eclipse.jgit/org.eclipse.jgit "5.13.0.202109080827-r"]]
  :global-vars {*warn-on-reflection* true}
  :cljfmt {:file-pattern #"^[^\.].*\.clj$"
           :indents {script [[:inner 0]]
                     template-vars [[:inner 0]]
                     fact [[:inner 0]]
                     comment [[:inner 0]]}}
  :profiles {:dev {:plugins [[lein-ancient "0.6.15"]
                             [lein-exec "0.3.7"]
                             [cider/cider-nrepl "0.45.0"]]}
             :repl {:injections [(try (require 'jvm.tool)
                                      (require '[std.lib :as h])
                                      (catch Throwable t (.printStackTrace t)))]}}
  :resource-paths    ["resources" "src-build" "src-doc" "test-data" "test-code"]
  :java-source-paths ["src-java" "test-java"]
  :java-output-path  "target/classes"
  :repl-options {:host "0.0.0.0"
                 :port #_10234 51311}
  :jvm-opts
  ["-Xms2048m"
   "-Xmx2048m"
   "-XX:MaxMetaspaceSize=1048m"
   "-XX:-OmitStackTraceInFastThrow"
   
   ;;
   ;; GC FLAGS
   ;;
   "-XX:+UseAdaptiveSizePolicy"
   "-XX:+AggressiveHeap"
   "-XX:+ExplicitGCInvokesConcurrent"
   ;;"-XX:+UseCMSInitiatingOccupancyOnly"
   ;;"-XX:+CMSClassUnloadingEnabled"
   ;;"-XX:+CMSParallelRemarkEnabled"

   ;;
   ;; GC TUNING
   ;;   
   "-XX:MaxNewSize=256m"
   "-XX:NewSize=256m"
   ;;"-XX:CMSInitiatingOccupancyFraction=60"
   "-XX:MaxTenuringThreshold=8"
   "-XX:SurvivorRatio=4"

   ;;
   ;; Truffle
   ;;
   "-Dpolyglot.engine.WarnInterpreterOnly=false"
   
   ;;
   ;; JVM
   ;;
   "-Djdk.tls.client.protocols=\"TLSv1,TLSv1.1,TLSv1.2\""
   "-Djdk.attach.allowAttachSelf=true"
   "--enable-native-access=ALL-UNNAMED"
   "--add-opens" "java.base/java.io=ALL-UNNAMED"
   "--add-opens" "java.base/java.lang=ALL-UNNAMED"
   "--add-opens" "java.base/java.lang.annotation=ALL-UNNAMED"
   "--add-opens" "java.base/java.lang.invoke=ALL-UNNAMED"
   "--add-opens" "java.base/java.lang.module=ALL-UNNAMED"
   "--add-opens" "java.base/java.lang.ref=ALL-UNNAMED"
   "--add-opens" "java.base/java.lang.reflect=ALL-UNNAMED"
   "--add-opens" "java.base/java.math=ALL-UNNAMED"
   "--add-opens" "java.base/java.net=ALL-UNNAMED"
   "--add-opens" "java.base/java.nio=ALL-UNNAMED"
   "--add-opens" "java.base/java.nio.channels=ALL-UNNAMED"
   "--add-opens" "java.base/java.nio.charset=ALL-UNNAMED"
   "--add-opens" "java.base/java.nio.file=ALL-UNNAMED"
   "--add-opens" "java.base/java.nio.file.attribute=ALL-UNNAMED"
   "--add-opens" "java.base/java.nio.file.spi=ALL-UNNAMED"
   "--add-opens" "java.base/java.security=ALL-UNNAMED"
   "--add-opens" "java.base/java.security.cert=ALL-UNNAMED"
   "--add-opens" "java.base/java.security.interfaces=ALL-UNNAMED"
   "--add-opens" "java.base/java.security.spec=ALL-UNNAMED"
   "--add-opens" "java.base/java.text=ALL-UNNAMED"
   "--add-opens" "java.base/java.time=ALL-UNNAMED"
   "--add-opens" "java.base/java.time.chrono=ALL-UNNAMED"
   "--add-opens" "java.base/java.time.format=ALL-UNNAMED"
   "--add-opens" "java.base/java.time.temporal=ALL-UNNAMED"
   "--add-opens" "java.base/java.time.zone=ALL-UNNAMED"
   "--add-opens" "java.base/java.util=ALL-UNNAMED"
   "--add-opens" "java.base/java.util.concurrent=ALL-UNNAMED"
   "--add-opens" "java.base/java.util.concurrent.atomic=ALL-UNNAMED"
   "--add-opens" "java.base/java.util.concurrent.locks=ALL-UNNAMED"
   "--add-opens" "java.base/java.util.function=ALL-UNNAMED"
   "--add-opens" "java.base/java.util.jar=ALL-UNNAMED"
   "--add-opens" "java.base/java.util.regex=ALL-UNNAMED"
   "--add-opens" "java.base/java.util.spi=ALL-UNNAMED"
   "--add-opens" "java.base/java.util.stream=ALL-UNNAMED"
   "--add-opens" "java.base/java.util.zip=ALL-UNNAMED"
   "--add-opens" "java.base/jdk.internal=ALL-UNNAMED"
   "--add-opens" "java.base/jdk.internal.loader=ALL-UNNAMED"
   "--add-opens" "java.base/jdk.internal.misc=ALL-UNNAMED"
   "--add-opens" "java.base/jdk.internal.module=ALL-UNNAMED"
   "--add-opens" "java.base/jdk.internal.org.objectweb.asm=ALL-UNNAMED"
   "--add-opens" "java.base/jdk.internal.org.xml.sax=ALL-UNNAMED"
   "--add-opens" "java.base/jdk.internal.perf=ALL-UNNAMED"
   "--add-opens" "java.base/jdk.internal.reflect=ALL-UNNAMED"
   "--add-opens" "java.base/jdk.internal.util=ALL-UNNAMED"
   "--add-opens" "java.base/jdk.internal.vm=ALL-UNNAMED"
   "--add-opens" "java.base/jdk.internal.vm.annotation=ALL-UNNAMED"

   "--add-opens" "java.net.http/java.net.http=ALL-UNNAMED"
   "--add-opens" "java.net.http/jdk.internal.net.http=ALL-UNNAMED"
   "--add-opens" "java.management/java.lang.management=ALL-UNNAMED"
   "--add-opens" "java.management/sun.management=ALL-UNNAMED"

   "--add-opens" "java.desktop/java.applet=ALL-UNNAMED"
   "--add-opens" "java.desktop/java.awt=ALL-UNNAMED"
   "--add-opens" "java.desktop/java.awt.color=ALL-UNNAMED"
   "--add-opens" "java.desktop/java.awt.dnd=ALL-UNNAMED"
   "--add-opens" "java.desktop/java.awt.event=ALL-UNNAMED"
   "--add-opens" "java.desktop/java.awt.font=ALL-UNNAMED"
   "--add-opens" "java.desktop/java.awt.geom=ALL-UNNAMED"
   "--add-opens" "java.desktop/java.awt.im=ALL-UNNAMED"
   "--add-opens" "java.desktop/java.awt.im.spi=ALL-UNNAMED"
   "--add-opens" "java.desktop/java.awt.image=ALL-UNNAMED"
   "--add-opens" "java.desktop/java.awt.image.renderable=ALL-UNNAMED"
   "--add-opens" "java.desktop/java.awt.print=ALL-UNNAMED"
   ])
