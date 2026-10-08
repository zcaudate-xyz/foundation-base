const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const {JSDOM, VirtualConsole} = require('jsdom');

async function run() {
  const errors = [];
  const virtualConsole = new VirtualConsole();
  virtualConsole.on('jsdomError', error => errors.push(error.message));
  virtualConsole.on('error', (...args) => errors.push(args.join(' ')));
  const dom = new JSDOM('<!doctype html><html><body><main id="app"></main></body></html>', {
    url: 'http://localhost', runScripts: 'outside-only', pretendToBeVisual: true, virtualConsole
  });
  const {window} = dom;
  window.ResizeObserver = class { observe() {} unobserve() {} disconnect() {} };
  const assets = path.join(__dirname, 'dist/assets');
  const bundle = fs.readdirSync(assets).find(name => name.endsWith('.js'));
  window.eval(fs.readFileSync(path.join(assets, bundle), 'utf8'));
  const document = window.document;
  const wait = async predicate => {
    const until = Date.now() + 5000;
    while (!predicate()) {
      assert.ok(Date.now() < until, `Timed out; screen: ${document.body.textContent}; errors: ${errors}`);
      await new Promise(resolve => setTimeout(resolve, 20));
    }
  };
  const input = () => document.querySelector('input[aria-label="title"]');
  const button = label => document.querySelector(`[aria-label="${label}"]`);
  const edit = async value => {
    Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set.call(input(), value);
    input().dispatchEvent(new window.Event('input', {bubbles: true}));
    await new Promise(resolve => setTimeout(resolve, 20));
  };
  await wait(() => input()?.value === 'Will turnout increase?');
  assert.ok(document.body.textContent.includes('Will the bill pass?'));
  button('Select topic-2').click();
  await wait(() => input()?.value === 'Will the bill pass?');
  await edit('Updated in the UI?');
  button('Save title').click();
  await wait(() => (document.body.textContent.match(/title: Updated in the UI\?/g) || []).length === 2);
  await wait(() => document.body.textContent.includes('revision: 2') && document.body.textContent.includes('Saved;'));
  await wait(() => button('Save title') && !button('Save title').closest('[aria-disabled="true"]'));
  await edit(' ');
  button('Save title').click();
  await wait(() => document.body.textContent.includes('Title is required'));
  assert.ok(document.body.textContent.includes('revision: 2'));
  assert.equal((document.body.textContent.match(/title: Updated in the UI\?/g) || []).length, 2);
  assert.deepEqual(errors, []);
  dom.window.close();
  console.log('UI smoke passed: list, selection, edit/event refresh, and validation.');
}
run().catch(error => { process.stderr.write(`${error.stack || error}\n`); process.exitCode = 1; });
