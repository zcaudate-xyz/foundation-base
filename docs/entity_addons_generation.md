# Entity Addons And Generated Functions

## Goal

Move the old `statsdb.template.user-mixins.*` idea into a cleaner entity-level model.

The entity should declare what capabilities it has. Those capabilities should drive
generation of:

- standard table functions
- addon-specific functions
- companion tables where needed
- sync/event hints where needed

The entity declaration remains PG-first. The generation system should emit actual
output code rather than hide the logic inside a large macro expansion chain.

## Background

In `statstrade-core`, the `statsdb.template.user-mixins.*` namespaces already act as
a capability system around a base table.

Examples:

- `access`
- `starred`
- `join-request`
- `join-invite`

Those templates generate recurring patterns like:

- companion tables such as `TopicAccess`, `TopicStarred`, `TopicJoinRequest`
- helper assertions such as `topic-assert-is-owner`
- standard mutations such as `topic-add-member`, `topic-remove-star`
- list/query functions
- notify payloads that include `:db/sync`

So the old system should be treated as an important reference for what addon
generation needs to produce.

## Recommendation

Replace the old template-registry mental model with an entity-addon model.

Conceptually:

```clojure
(defentity.pg Topic
  {:addons [:track/default
            :record/default
            :ownership
            :access/member
            :starred
            :join-request
            :join-invite]}
  [...fields...])
```

The entity stays the source of truth. Addons describe repeated behavior families.

## Core Split

There should be two kinds of generated output.

### 1. Standard Functions

Generated for most entities by default.

Typical examples:

- `create`
- `create-raw`
- `modify`
- `remove` or `archive`
- `get`
- `list`

These functions are part of the normal CRUD surface of the table.

### 2. Addon Functions

Generated only when the entity declares that addon.

Typical examples:

- membership/access functions
- starred functions
- request/invite workflow functions
- ownership transfer helpers
- visibility/public toggles

These functions are not universal. They are capability-driven.

## Proposed Addon Catalog

The initial addon catalog should be derived from the proven patterns in
`statsdb.template.user-mixins.*`.

### `:track/default`

Purpose:

- add standard operation tracking fields/behavior

Typical generated output:

- tracking-related defaults
- operation metadata helpers

### `:record/default`

Purpose:

- add base record fields such as ids, timestamps, deleted flags

Typical generated output:

- common schema fragments
- standard record defaults

### `:ownership`

Purpose:

- declare that the entity has a canonical owner

Typical generated output:

- `assert-is-owner`
- ownership-aware `create`
- ownership-aware `remove`
- `transfer-ownership`

### `:access/member`

Purpose:

- entity supports member/admin roles and membership management

Typical generated output:

- companion access table
- `assert-is-member`
- `assert-is-admin`
- `assert-is-management`
- `add-member`
- `remove-member`
- `set-member-access`
- `leave-as-member`

### `:starred`

Purpose:

- users can star/favourite records of this entity

Typical generated output:

- companion starred table
- `add-star`
- `remove-star`

### `:join-request`

Purpose:

- users can request to join an entity

Typical generated output:

- companion join-request table
- `assert-can-send-join-request`
- `send-join-request`
- `accept-join-request`
- `reject-join-request`
- `cancel-join-request`
- `list-join-requests-*`

### `:join-invite`

Purpose:

- managers can invite users into an entity

Typical generated output:

- companion join-invite table
- `assert-can-send-join-invite`
- `send-join-invite`
- `accept-join-invite`
- `reject-join-invite`
- `cancel-join-invite`
- `list-join-invites-*`

### `:public-toggle`

Purpose:

- public/member visibility can be toggled for membership-style entities

Typical generated output:

- `set-public-as-member`

## Mapping From Old Templates

The old `statsdb.template.user-mixins.*` modules map cleanly to addons:

- `user-mixins.access` -> `:ownership`, `:access/member`, `:public-toggle`
- `user-mixins.starred` -> `:starred`
- `user-mixins.join-request` -> `:join-request`
- `user-mixins.join-invite` -> `:join-invite`

So the new system should not discard the old logic. It should reorganize it around
entity-declared capabilities.

## Generation Model

The macro should stay thin.

`defentity.pg` should do two things:

1. define or normalize the entity declaration
2. register an entity spec that generators can consume

Actual helper generation should happen in explicit codegen steps that emit normal
source files.

For example:

- entity declaration file
- generated standard function file
- generated addon function file
- generated policy file

## Why This Is Better

This model is better than the old template registry because:

- the capability model is explicit at the entity declaration
- generated behavior is grouped by entity instead of by macro module
- output code is inspectable and diffable
- addon behavior can later carry sync metadata in one place
- it matches the newer goal of PG-first authoring plus explicit code generation

## Relationship To Sync

This document is about entity capabilities first, not final sync generation.

However, addons are the right place to later attach sync expectations.

Examples:

- `:starred` can imply sync over the starred companion table
- `:access/member` can imply sync over the access table and base table
- `:join-request` can imply sync over request state transitions

That means addon generation and inferred `:db/sync` generation should fit together
naturally later.

## Example

Conceptual authoring:

```clojure
(defentity.pg Organisation
  {:addons [:track/default
            :record/default
            :ownership
            :access/member
            :join-request
            :join-invite]}
  [:name {:type :citext :required true}
   :title {:type :text}])
```

Conceptual generated outputs:

- `OrganisationAccess`
- `OrganisationJoinRequest`
- `OrganisationJoinInvite`
- `organisation-create`
- `organisation-modify`
- `organisation-remove`
- `organisation-assert-is-owner`
- `organisation-assert-is-member`
- `organisation-add-member`
- `organisation-remove-member`
- `organisation-send-join-request`
- `organisation-accept-join-invite`

## Immediate Design Rule

The system should answer this question clearly:

> Given one entity declaration, what standard functions and addon functions should
> exist for it?

That should be derivable from the entity spec without consulting a separate template
registry by hand.

## Suggested Next Step

Turn the old `user-mixins` family into a formal addon spec table.

For each addon:

- required entity fields or assumptions
- companion tables generated
- standard functions generated
- addon functions generated
- sync-sensitive tables affected

That would make the capability model concrete enough to drive real generation.
