(defproject foundation/dupasupa "0.1.0"
  :description "Dupasupa application management example"
  :dependencies [[org.clojure/clojure "1.12.0"]
                 [org.clojure/data.json "2.4.0"]
                 [org.postgresql/postgresql "42.7.2"]
                 [http-kit "2.8.0"]
                 [xyz.zcaudate/code.test "4.1.8"]
                 [xyz.zcaudate/lang "4.1.8"]
                 [xyz.zcaudate/postgres "4.1.8"]
                 [xyz.zcaudate/js.core "4.1.8"]
                 [xyz.zcaudate/xtalk.substrate "4.1.8"]
                 [xyz.zcaudate/std.make "4.1.8"]]
  :source-paths ["src" "../../src" "../../src-lang" "../../src-refactor"]
  :resource-paths ["../../resources"]
  :aliases {"app" ["run" "-m" "app.manage" "dupasupa.build/PROJECT"]
            "manage" ["run" "-m" "app.manage.server" "dupasupa.build/PROJECT"]
            "test" ["run" "-m" "code.test"]})
