import 'xtalk_ui/ui/view/polyfill.dart' as polyfill;
import 'xtalk_ui/ui/view/runtime.dart' as runtime;
import 'xtalk_ui/ui/view/backend.dart' as backend;




var runtime_create = runtime.runtime_create;

var snapshot = runtime.snapshot;

var local_set = runtime.local_set;

var prepare = runtime.prepare;

var open = runtime.open;

var close = runtime.close;

var native_registry = backend.native_registry;

var polyfill_registry = polyfill.registry;