const impl_supabase = require("@xtalk/db/system/impl-supabase.js")

const main_client = require("@xtalk/db/system/main-client.js")

const impl_memory = require("@xtalk/db/system/impl-memory.js")

const impl_sqlite = require("@xtalk/db/system/impl-sqlite.js")

const impl_postgres = require("@xtalk/db/system/impl-postgres.js")

function create_impl(type,defaults,schema,lookup){
  let client = main_client.create_client(type,defaults);
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

function create_impl_init(impl){
  let {lookup,schema} = impl;
  let type = impl["::"];
  if(type == "xt.db.system.impl_sqlite/ImplSqlite"){
    return impl_sqlite.impl_sqlite_init(impl);
  }
  else if(type == "xt.db.system.impl_postgres/ImplPostgres"){
    return impl_postgres.impl_postgres_init(impl);
  }
  else{
    return Promise.resolve().then(function (){
      return impl;
    });
  }
}

module.exports = {
  ["create_impl"]:create_impl,
  ["create_impl_init"]:create_impl_init
}