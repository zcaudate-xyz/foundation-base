(ns documentation.xt-substrate-walkthrough
  (:use code.test))

[[:hero {:title "xt.substrate walkthrough"
         :subtitle "Memory, websocket, fanout, multiplex, and page examples."
         :lead "The substrate walkthroughs demonstrate how frames, spaces, transports, proxy utilities, and pages compose into running systems."}]]

[[:chapter {:title "Walkthrough sequence" :link "sequence"}]]

"Use `test-lang/xt/substrate/walkthrough/s01_basic_test.clj` through `s07_wsserver_test.clj` for the core sequence. Use `test-lang/xt/substrate/walkthrough_js/s30_workers_test.clj` through `s36_playmin_test.clj` for browser, worker, proxy, React, and playground flows."

[[:chapter {:title "What to learn" :link "learn"}]]

"The examples should explain how a frame is shaped, how a space routes work, how memory transport differs from websocket transport, how fanout and multiplexing are introduced, and where page/proxy utilities enter the stack."
