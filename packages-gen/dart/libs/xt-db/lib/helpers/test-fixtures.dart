var Schema = <dynamic, dynamic>{
  "Task":<dynamic, dynamic>{
    "id":<dynamic, dynamic>{
      "type":"uuid",
      "cardinality":"one",
      "primary":true,
      "scope":"id",
      "order":0,
      "ident":"id"
    },
    "status":<dynamic, dynamic>{
      "type":"enum",
      "cardinality":"one",
      "required":true,
      "scope":"info",
      "enum":<dynamic, dynamic>{"ns":"postgres.sample.scratch-v1/EnumStatus"},
      "order":1,
      "ident":"status"
    },
    "name":<dynamic, dynamic>{
      "type":"text",
      "cardinality":"one",
      "required":true,
      "scope":"data",
      "order":2,
      "ident":"name"
    }
  }
};

var Lookup = <dynamic, dynamic>{"Task":<dynamic, dynamic>{"position":0}};

var SchemaLookup = <dynamic, dynamic>{
  "TaskCache":<dynamic, dynamic>{
    "schema":"scratch",
    "schema_primary":<dynamic, dynamic>{"type":"uuid","id":"id"},
    "public":true,
    "schema_update":false,
    "position":0
  },
  "Task":<dynamic, dynamic>{
    "schema":"scratch",
    "schema_primary":<dynamic, dynamic>{"type":"uuid","id":"id"},
    "public":true,
    "schema_update":false,
    "position":1
  },
  "Entry":<dynamic, dynamic>{
    "schema":"scratch",
    "schema_primary":<dynamic, dynamic>{"type":"uuid","id":"id"},
    "public":true,
    "schema_update":false,
    "position":2
  }
};

var Views = <dynamic, dynamic>{
  "Task":<dynamic, dynamic>{
    "select":<dynamic, dynamic>{
      "by_status":<dynamic, dynamic>{
        "input":<dynamic>[<dynamic, dynamic>{"symbol":"i_status","type":"text"}],
        "return":"jsonb",
        "view":<dynamic, dynamic>{
          "table":"Task",
          "type":"select",
          "tag":"by_status",
          "access":<dynamic, dynamic>{"roles":<dynamic, dynamic>{}},
          "guards":<dynamic>[],
          "query":<dynamic, dynamic>{"status":"{{i_status}}"}
        }
      }
    },
    "return":<dynamic, dynamic>{
      "default":<dynamic, dynamic>{
        "input":<dynamic>[<dynamic, dynamic>{"symbol":"i_task_id","type":"uuid"}],
        "return":"jsonb",
        "view":<dynamic, dynamic>{
          "table":"Task",
          "type":"return",
          "tag":"default",
          "access":<dynamic, dynamic>{"roles":<dynamic, dynamic>{}},
          "guards":<dynamic>[],
          "query":<dynamic>["status"]
        }
      }
    }
  }
};

var InstallOpts = <dynamic, dynamic>{"schema":Schema,"lookup":Lookup,"views":Views};

var EntrySeed = <dynamic, dynamic>{
  "Entry":<dynamic>[
    <dynamic, dynamic>{
    "id":"00000000-0000-0000-0000-0000000000c1",
    "name":"alpha",
    "tags":<dynamic>["guide","sql"],
    "__deleted__":false
  },
    <dynamic, dynamic>{
    "id":"00000000-0000-0000-0000-0000000000c2",
    "name":"beta",
    "tags":<dynamic>["guide"],
    "__deleted__":false
  }
  ]
};

var ModelSpec = <dynamic, dynamic>{
  "views":<dynamic, dynamic>{
    "main":<dynamic, dynamic>{
      "resolver":<dynamic, dynamic>{
        "type":"db/query",
        "table":"Task",
        "return_entry":<dynamic, dynamic>{
          "input":<dynamic>[<dynamic, dynamic>{"symbol":"i_task_id","type":"uuid"}],
          "return":"jsonb",
          "view":<dynamic, dynamic>{
            "table":"Task",
            "type":"return",
            "tag":"default",
            "access":<dynamic, dynamic>{"roles":<dynamic, dynamic>{}},
            "guards":<dynamic>[],
            "query":<dynamic>["status"]
          }
        },
        "return_id":"00000000-0000-0000-0000-0000000000a1"
      },
      "input":<dynamic>[]
    },
    "open":<dynamic, dynamic>{
      "resolver":<dynamic, dynamic>{
        "type":"db/query",
        "table":"Task",
        "select_entry":<dynamic, dynamic>{
          "input":<dynamic>[<dynamic, dynamic>{"symbol":"i_status","type":"text"}],
          "return":"jsonb",
          "view":<dynamic, dynamic>{
            "table":"Task",
            "type":"select",
            "tag":"by_status",
            "access":<dynamic, dynamic>{"roles":<dynamic, dynamic>{}},
            "guards":<dynamic>[],
            "query":<dynamic, dynamic>{"status":"{{i_status}}"}
          }
        }
      },
      "default_input":<dynamic>["open"]
    }
  }
};

var DependentModelSpec = <dynamic, dynamic>{
  "views":<dynamic, dynamic>{
    "main":<dynamic, dynamic>{
      "resolver":<dynamic, dynamic>{
        "type":"db/query",
        "table":"Task",
        "return_method":"default",
        "return_id":"00000000-0000-0000-0000-0000000000a1"
      },
      "input":<dynamic>[]
    },
    "open":<dynamic, dynamic>{
      "resolver":<dynamic, dynamic>{"type":"db/query","table":"Task","select_method":"by_status"},
      "default_input":<dynamic>["open"],
      "deps":<dynamic>["main"]
    }
  }
};

var Seed = <dynamic, dynamic>{
  "Task":<dynamic>[
    <dynamic, dynamic>{
    "id":"00000000-0000-0000-0000-0000000000a1",
    "status":"open",
    "name":"alpha-task"
  },
    <dynamic, dynamic>{
    "id":"00000000-0000-0000-0000-0000000000a2",
    "status":"closed",
    "name":"beta-task"
  }
  ]
};