import 'package:xtalk_db/system/impl-supabase.dart' as impl_supabase;
import 'package:xtalk_db/system/main-client.dart' as main_client;
import 'package:xtalk_db/system/impl-memory.dart' as impl_memory;
import 'package:xtalk_db/system/impl-sqlite.dart' as impl_sqlite;
import 'package:xtalk_db/system/impl-postgres.dart' as impl_postgres;
import 'dart:async';






create_impl(type, defaults, schema, lookup) {
  var client = main_client.create_client(type,defaults);
  if(type == "memory"){
    return impl_memory.impl_memory(schema,lookup);
  }
  else if(type == "sqlite"){
    return impl_sqlite.impl_sqlite(client,schema,lookup);
  }
  else if(type == "postgres"){
    return impl_postgres.impl_postgres(client,schema,lookup);
  }
  else if(type == "supabase"){
    return impl_supabase.impl_supabase(client,schema,lookup);
  }
}

create_impl_init(impl) {
  var lookup = impl["lookup"];
  var schema = impl["schema"];
  var type = impl["::"];
  if(type == "xt.db.system.impl_sqlite/ImplSqlite"){
    return impl_sqlite.impl_sqlite_init(impl);
  }
  else if(type == "xt.db.system.impl_postgres/ImplPostgres"){
    return impl_postgres.impl_postgres_init(impl);
  }
  else{
    return Future.sync(() {
      return impl;
    });
  }
}