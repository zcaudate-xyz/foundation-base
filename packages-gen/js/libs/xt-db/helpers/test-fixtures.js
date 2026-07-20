var Schema = {
  "Task":{
    "id":{
      "type":"uuid",
      "cardinality":"one",
      "primary":true,
      "scope":"id",
      "order":0,
      "ident":"id"
    },
    "status":{
      "type":"enum",
      "cardinality":"one",
      "required":true,
      "scope":"info",
      "enum":{"ns":"postgres.sample.scratch-v1/EnumStatus"},
      "order":1,
      "ident":"status"
    },
    "name":{
      "type":"text",
      "cardinality":"one",
      "required":true,
      "scope":"data",
      "order":2,
      "ident":"name"
    }
  }
};

var Lookup = {"Task":{"position":0}};

var SchemaLookup = {
  "TaskCache":{
    "schema":"scratch",
    "schema_primary":{"type":"uuid","id":"id"},
    "public":true,
    "schema_update":false,
    "position":0
  },
  "Task":{
    "schema":"scratch",
    "schema_primary":{"type":"uuid","id":"id"},
    "public":true,
    "schema_update":false,
    "position":1
  },
  "Entry":{
    "schema":"scratch",
    "schema_primary":{"type":"uuid","id":"id"},
    "public":true,
    "schema_update":false,
    "position":2
  }
};

var Views = {
  "Task":{
    "select":{
      "by_status":{
        "input":[{"symbol":"i_status","type":"text"}],
        "return":"jsonb",
        "view":{
          "table":"Task",
          "type":"select",
          "tag":"by_status",
          "access":{"roles":{}},
          "guards":[],
          "query":{"status":"{{i_status}}"}
        }
      }
    },
    "return":{
      "default":{
        "input":[{"symbol":"i_task_id","type":"uuid"}],
        "return":"jsonb",
        "view":{
          "table":"Task",
          "type":"return",
          "tag":"default",
          "access":{"roles":{}},
          "guards":[],
          "query":["status"]
        }
      }
    }
  }
};

var InstallOpts = {"schema":Schema,"lookup":Lookup,"views":Views};

var EntrySeed = {
  "Entry":[
    {
    "id":"00000000-0000-0000-0000-0000000000c1",
    "name":"alpha",
    "tags":["guide","sql"],
    "__deleted__":false
  },
    {
    "id":"00000000-0000-0000-0000-0000000000c2",
    "name":"beta",
    "tags":["guide"],
    "__deleted__":false
  }
  ]
};

var ModelSpec = {
  "views":{
    "main":{
      "resolver":{
        "type":"db/query",
        "table":"Task",
        "return_entry":{
          "input":[{"symbol":"i_task_id","type":"uuid"}],
          "return":"jsonb",
          "view":{
            "table":"Task",
            "type":"return",
            "tag":"default",
            "access":{"roles":{}},
            "guards":[],
            "query":["status"]
          }
        },
        "return_id":"00000000-0000-0000-0000-0000000000a1"
      },
      "input":[]
    },
    "open":{
      "resolver":{
        "type":"db/query",
        "table":"Task",
        "select_entry":{
          "input":[{"symbol":"i_status","type":"text"}],
          "return":"jsonb",
          "view":{
            "table":"Task",
            "type":"select",
            "tag":"by_status",
            "access":{"roles":{}},
            "guards":[],
            "query":{"status":"{{i_status}}"}
          }
        }
      },
      "default_input":["open"]
    }
  }
};

var DependentModelSpec = {
  "views":{
    "main":{
      "resolver":{
        "type":"db/query",
        "table":"Task",
        "return_method":"default",
        "return_id":"00000000-0000-0000-0000-0000000000a1"
      },
      "input":[]
    },
    "open":{
      "resolver":{"type":"db/query","table":"Task","select_method":"by_status"},
      "default_input":["open"],
      "deps":["main"]
    }
  }
};

var Seed = {
  "Task":[
    {
    "id":"00000000-0000-0000-0000-0000000000a1",
    "status":"open",
    "name":"alpha-task"
  },
    {
    "id":"00000000-0000-0000-0000-0000000000a2",
    "status":"closed",
    "name":"beta-task"
  }
  ]
};

module.exports = {
  ["Schema"]:Schema,
  ["Lookup"]:Lookup,
  ["SchemaLookup"]:SchemaLookup,
  ["Views"]:Views,
  ["InstallOpts"]:InstallOpts,
  ["EntrySeed"]:EntrySeed,
  ["ModelSpec"]:ModelSpec,
  ["DependentModelSpec"]:DependentModelSpec,
  ["Seed"]:Seed
}