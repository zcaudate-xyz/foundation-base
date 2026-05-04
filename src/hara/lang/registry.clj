(ns hara.lang.registry)

(def +registry+
  (atom {[:postgres :default]          'hara.runtime.postgres.base.grammar
         [:postgres :jdbc]             'hara.runtime.postgres.base.client
         [:postgres :jdbc.client]      'hara.runtime.postgres.base.client
         
         [:solidity :default]          'hara.runtime.solidity
	 
         [:bash   :oneshot]            'hara.runtime.basic.impl.process-bash
         [:bash   :basic]              'hara.runtime.shell
         [:bash   :remote]             'hara.runtime.shell
         
         [:lua    :oneshot]            'hara.runtime.basic.impl.process-lua
         [:lua    :basic]              'hara.runtime.basic.impl.process-lua
         [:lua    :interactive]        'hara.runtime.basic.impl.process-lua
         [:lua    :websocket]          'hara.runtime.basic.impl.process-lua
         [:lua    :nginx]              'hara.runtime.nginx
         [:lua    :nginx.instance]     'hara.runtime.nginx
         [:lua    :redis]              'hara.runtime.redis
         [:lua.redis :default]         'hara.runtime.redis
         [:lua.redis :redis]           'hara.runtime.redis
         [:lua.nginx :oneshot]         'hara.runtime.basic.impl.process-lua
         [:lua.nginx :basic]           'hara.runtime.basic.impl.process-lua
         [:lua.nginx :interactive]     'hara.runtime.basic.impl.process-lua
         [:lua.nginx :websocket]       'hara.runtime.basic.impl.process-lua
         [:lua.nginx :nginx]           'hara.runtime.nginx
         [:lua.nginx :nginx.instance]  'hara.runtime.nginx
         [:lua.nginx :remote-port]     'hara.runtime.basic.impl.process-lua
         [:lua.nginx :remote-ws]       'hara.runtime.basic.impl.process-lua
         [:lua    :remote-port]        'hara.runtime.basic.impl.process-lua
         [:lua    :remote-ws]          'hara.runtime.basic.impl.process-lua
         
         [:js     :oneshot]            'hara.runtime.basic.impl.process-js
         [:js     :basic]              'hara.runtime.basic.impl.process-js
         [:js     :interactive]        'hara.runtime.basic.impl.process-js
         [:js     :websocket]          'hara.runtime.basic.impl.process-js
         [:js     :javafx]             'hara.runtime.javafx
         [:js     :graal]              'hara.runtime.graal
         [:js     :browser]            'hara.runtime.browser
         [:js     :remote-port]        'hara.runtime.basic.impl.process-js
         [:js     :remote-ws]          'hara.runtime.basic.impl.process-js
         
         [:python :oneshot]            'hara.runtime.basic.impl.process-python
         [:python :basic]              'hara.runtime.basic.impl.process-python
         [:python :interactive]        'hara.runtime.basic.impl.process-python
         [:python :websocket]          'hara.runtime.basic.impl.process-python
         [:python :graal]              'hara.runtime.graal
         [:python :jep]                'hara.runtime.jep
         [:python :libpython]          'hara.runtime.libpython
         [:python :remote-port]        'hara.runtime.basic.impl.process-python
         [:python :remote-ws]          'hara.runtime.basic.impl.process-python

         [:scheme :oneshot]            'hara.runtime.basic.impl.process-scheme
         [:scheme :basic]              'hara.runtime.basic.impl.process-scheme

         [:elisp  :oneshot]            'hara.runtime.basic.impl.process-elisp
         [:elisp  :basic]              'hara.runtime.basic.impl.process-elisp

         [:ruby   :oneshot]            'hara.runtime.basic.impl.process-ruby
         [:ruby   :basic]              'hara.runtime.basic.impl.process-ruby

         [:perl   :oneshot]            'hara.runtime.basic.impl-annex.process-perl
         [:perl   :basic]              'hara.runtime.basic.impl-annex.process-perl

         [:php    :oneshot]            'hara.runtime.basic.impl-annex.process-php
         [:php    :basic]              'hara.runtime.basic.impl-annex.process-php
         
         [:r      :oneshot]            'hara.runtime.basic.impl-annex.process-r
         [:r      :basic]              'hara.runtime.basic.impl-annex.process-r

         [:julia  :oneshot]            'hara.runtime.basic.impl-annex.process-julia
         [:julia  :basic]              'hara.runtime.basic.impl-annex.process-julia

         [:erlang :oneshot]            'hara.runtime.basic.impl-annex.process-erlang
         [:erlang :basic]              'hara.runtime.basic.impl-annex.process-erlang
         
         [:haskell :twostep]           'hara.runtime.basic.impl-annex.process-haskell
         [:lean    :twostep]           'hara.runtime.basic.impl-annex.process-lean
         [:ocaml   :twostep]           'hara.runtime.basic.impl-annex.process-ocaml
         
         [:rust   :twostep]            'hara.runtime.basic.impl-annex.process-rust
         
         [:c      :jocl]               'hara.runtime.jocl
         [:c      :oneshot]            'hara.runtime.basic.impl.process-c
         [:c      :twostep]            'hara.runtime.basic.impl.process-c

         [:dart   :twostep]            'hara.runtime.basic.impl.process-dart
         [:go     :twostep]            'hara.runtime.basic.impl.process-go
	 
         [:xtalk  :oneshot]            'hara.runtime.basic.impl.process-xtalk}))

(def +book-registry+
  (atom {[:xtalk    :default]          {:ns 'hara.model.spec-xtalk
                                        :book '+book+}

         [:bash     :default]          {:ns 'hara.model.spec-bash
                                        :book '+book+
                                        :parent :xtalk}
         [:c        :default]          {:ns 'hara.model.spec-c
                                        :book '+book+
                                        :parent :xtalk}
         [:dart     :default]          {:ns 'hara.model.spec-dart
                                        :book '+book+
                                        :parent :xtalk}
         [:glsl     :default]          {:ns 'hara.model.spec-glsl
                                        :book '+book+
                                        :parent :xtalk}
         [:go       :default]          {:ns 'hara.model.spec-go
                                        :book '+book+
                                        :parent :xtalk}
         [:js       :default]          {:ns 'hara.model.spec-js
                                        :book '+book+
                                        :parent :xtalk}
         [:llvm     :default]          {:ns 'hara.model.spec-llvm
                                        :book '+book+
                                        :parent :xtalk}
         [:lua      :default]          {:ns 'hara.model.spec-lua
                                        :book '+book+
                                        :parent :xtalk}
         [:lua.redis :default]         {:ns 'hara.model.spec-lua.variant-redis
                                        :book '+book+
                                        :parent :lua}
         [:lua.nginx :default]         {:ns 'hara.model.spec-lua.variant-nginx
                                        :book '+book+
                                        :parent :lua}
         [:python   :default]          {:ns 'hara.model.spec-python
                                        :book '+book+
                                        :parent :xtalk}
         [:elisp    :default]          {:ns 'hara.model.spec-elisp
                                        :book '+book+
                                        :parent :xtalk}
         [:scheme   :default]          {:ns 'hara.model.spec-scheme
                                        :book '+book+
                                        :parent :xtalk}
         
         [:postgres :default]          {:ns 'hara.runtime.postgres.base.grammar
                                        :book '+book+}
         [:solidity :default]          {:ns 'hara.model.spec-solidity
                                        :book '+book+}

         [:circom   :default]          {:ns 'hara.model.annex.spec-circom
                                        :book '+book+
                                        :parent :xtalk}
         [:erlang   :default]          {:ns 'hara.model.annex.spec-erlang
                                        :book '+book+
                                        :parent :xtalk}
         [:fortran  :default]          {:ns 'hara.model.annex.spec-fortran
                                        :book '+book+
                                        :parent :xtalk}
         [:haskell  :default]          {:ns 'hara.model.annex.spec-haskell
                                        :book '+book+
                                        :parent :xtalk}
         [:lean     :default]          {:ns 'hara.model.annex.spec-lean
                                        :book '+book+
                                        :parent :xtalk}
         [:ocaml    :default]          {:ns 'hara.model.annex.spec-ocaml
                                        :book '+book+
                                        :parent :xtalk}
         [:jq       :default]          {:ns 'hara.model.annex.spec-jq
                                        :book '+book+
                                        :parent :xtalk}
         [:julia    :default]          {:ns 'hara.model.annex.spec-julia
                                        :book '+book+
                                        :parent :xtalk}
         [:perl     :default]          {:ns 'hara.model.annex.spec-perl
                                        :book '+book+
                                        :parent :xtalk}
         [:php      :default]          {:ns 'hara.model.annex.spec-php
                                        :book '+book+
                                        :parent :xtalk}
         [:r        :default]          {:ns 'hara.model.annex.spec-r
                                        :book '+book+
                                        :parent :xtalk}
         [:ruby     :default]          {:ns 'hara.model.spec-ruby
                                        :book '+book+
                                        :parent :xtalk}
         [:rust     :default]          {:ns 'hara.model.annex.spec-rust
                                        :book '+book+
                                        :parent :xtalk}
         [:verilog  :default]          {:ns 'hara.model.annex.spec-verilog
                                        :book '+book+
                                        :parent :xtalk}}))

(defn registry-book-list
  "lists all registered books"
  {:added "4.1"}
  ([] (keys @+book-registry+)))

(defn registry-book-ns
  "gets the namespace for a book registry entry"
  {:added "4.1"}
  ([lang] (registry-book-ns lang :default))
  ([lang key]
   (:ns (get @+book-registry+ [lang key]))))

(defn registry-book-info
  "gets the full book registry entry"
  {:added "4.1"}
  ([lang] (registry-book-info lang :default))
  ([lang key]
   (get @+book-registry+ [lang key])))

(defn registry-book
  "loads the book namespace and returns the book"
  {:added "4.1"}
  ([lang] (registry-book lang :default))
  ([lang key]
   (when-let [{:keys [ns book]} (registry-book-info lang key)]
     (clojure.core/require ns)
     (some-> (ns-resolve (the-ns ns) book)
             var-get))))
