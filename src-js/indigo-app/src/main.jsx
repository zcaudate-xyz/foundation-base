import React from 'react'
import '@/client/monaco-init'

import './index.css'
import * as r from '@/libs/js/react'

import * as repl from '@/client/repl-client'
import * as app from '@/client/app'

import { AppStateProvider } from '@/client/app/state'

// code.dev.index-main/AppIndex [35] 
export function AppIndex() {
  React.useEffect(function () {
    repl.connect("localhost", 1311, { path: "repl" });
  }, []);
  return (
    <AppStateProvider>
      <app.App></app.App>
    </AppStateProvider>);
}

// code.dev.index-main/main [77] 
export function main() {
  r.renderDOMRoot("root", AppIndex);
}

// code.dev.index-main/__main__ [81] 
main();