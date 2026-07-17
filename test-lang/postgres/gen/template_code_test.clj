(ns postgres.gen.template-code-test
  (:require [clojure.string :as str]
            [code.project :as project]
            [hara.lang :as l]
            [postgres.gen.template-code :as gen]
            [postgres.sample.scratch-v0]
            [std.fs :as fs])
  (:use code.test))

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

(def +ping-lines+
  ["(def.xt ping"
   "  {:input []"
   "   :return \"text\""
   "   :schema \"scratch_v0\""
   "   :id \"ping\""
   "   :flags {}"
   "   :url \"api/scratch-v0/ping\"})"])

(def +routes-lines+
  ["(def.xt ping"
   "  {:input []"
   "   :return \"text\""
   "   :schema \"scratch_v0\""
   "   :id \"ping\""
   "   :flags {}"
   "   :url \"api/scratch-v0/ping\"})"
   ""
   "(def.xt log-append"
    "  {:input [{:symbol \"i_message\" :type \"text\"}]"
   "   :return \"jsonb\""
   "   :schema \"scratch_v0\""
   "   :id \"log_append\""
   "   :flags {}"
   "   :url \"api/scratch-v0/log-append\"})"])

^{:refer postgres.gen.template-code/namespace-url-root :added "4.1"}
(fact "creates a default route root from a namespace"
  (gen/namespace-url-root 'postgres.sample.scratch-v0)
  => "api/scratch-v0")

^{:refer postgres.gen.template-code/emit-route-entry :added "4.1"}
(fact "emits a single `def.xt` route entry"
  (str/split-lines
   (gen/emit-route-entry 'postgres.sample.scratch-v0/ping))
  => +ping-lines+)

^{:refer postgres.gen.template-code/emit-route-entries :added "4.1"}
(fact "emits all `def.xt` route entries for a namespace"
  (str/split-lines
   (gen/emit-route-entries 'postgres.sample.scratch-v0))
  => +routes-lines+)

^{:refer postgres.gen.template-code/target-path :added "4.1"}
(fact "resolves the generated namespace path from the source namespace root"
  (with-redefs [project/get-path (fn [_] "src-lang/postgres/sample/scratch_v0.clj")
                project/project  (fn [] {:source-paths ["src" "src-lang"]})]
    (gen/target-path 'postgres.sample.scratch-v0.route-entries
                     'postgres.sample.scratch-v0))
  => "src-lang/postgres/sample/scratch_v0/route_entries.clj")

^{:refer postgres.gen.template-code/namespace-body :added "4.1"}
(fact "creates the namespace body for generated route entries"
  (->> (gen/namespace-body 'postgres.sample.scratch-v0.route-entries
                           'postgres.sample.scratch-v0)
       str/split-lines
       (take 6)
       vec)
  => ["(ns postgres.sample.scratch-v0.route-entries"
      "  (:require [hara.lang :as l]))"
      ""
      "(l/script :xtalk)"
      ""
      "(def.xt ping"])

^{:refer postgres.gen.template-code/generate-ns :added "4.1"}
(fact "writes the generated namespace file"
  (let [tmpdir    (str (fs/create-tmpdir "template-code-"))
        out-path  (str tmpdir "/src-lang/postgres/sample/scratch_v0/route_entries.clj")]
    (try
      (with-redefs [project/get-path (fn [_] (str tmpdir "/src-lang/postgres/sample/scratch_v0.clj"))
                    project/project  (fn [] {:source-paths ["src" "src-lang"]})]
        (gen/generate-ns 'postgres.sample.scratch-v0.route-entries
                         'postgres.sample.scratch-v0))
      => out-path

      (slurp out-path)
      => #(and (str/includes? % "(def.xt ping")
               (str/includes? % ":url \"api/scratch-v0/ping\"")
               (str/includes? % "(def.xt log-append")
               (str/includes? % ":url \"api/scratch-v0/log-append\""))
      (finally
        (fs/delete tmpdir)))))


^{:refer postgres.gen.template-code/route-entry-input :added "4.1"}
(fact "creates template input for a simple route"
  (gen/route-entry-input {:src 'postgres.sample.scratch-v0/ping
                          :route-sym 'ping
                          :root "api/scratch-v0"})
  => {'route-sym 'ping
      'input []
      'return "text"
      'schema "scratch_v0"
      'id "ping"
      'flags {}
      'url "api/scratch-v0/ping"})

^{:refer postgres.gen.template-code/route-entry-input :added "4.1"}
(fact "creates template input using the root to build the url"
  (gen/route-entry-input {:src 'postgres.sample.scratch-v0/log-append
                          :route-sym 'log-append
                          :root "api/scratch-v0"})
  => {'route-sym 'log-append
      'input [{:symbol "i_message" :type "text"}]
      'return "jsonb"
      'schema "scratch_v0"
      'id "log_append"
      'flags {}
      'url "api/scratch-v0/log-append"})

^{:refer postgres.gen.template-code/route-entry-input :added "4.1"}
(fact "uses a custom root when provided"
  (get (gen/route-entry-input {:src 'postgres.sample.scratch-v0/ping
                               :route-sym 'ping
                               :root "custom/root"})
       'url)
  => "custom/root/ping")

^{:refer postgres.gen.template-code/render-module :added "4.1"}
(fact "renders descriptor data as a templated XTalk entry"
  (gen/render-module
   'sample.generated
   :view
   [['sample/source
     {:id "sample_source"
      :table "Sample"
      :return-entry {:input [] :view {:type "return"}}
      :select-args []
      :return-args []}]])
  => #(and (str/includes? % "(def.xt ^{:api/type :view} source")
           (str/includes? % ":return-entry {:input []")
           (not (str/includes? % "\"{:id"))))
