import React from "react";
import blessed from "blessed";
import { render } from "react-blessed";
import { listUsers, lookupUser, SAMPLE_USERS, type User } from "./index.js";

type KeypressKey = {
  name?: string;
  ctrl?: boolean;
  meta?: boolean;
};

function userLabel(user: User): string {
  return user.displayName ?? user.id;
}

function App({ screen }: { screen: blessed.Widgets.Screen }): React.ReactElement {
  const [query, setQuery] = React.useState("");
  const [selectedIndex, setSelectedIndex] = React.useState(0);
  const normalizedQuery = query.trim().toLowerCase();
  const matches = listUsers(SAMPLE_USERS).filter((user) => {
    const searchable = [user.id, user.displayName ?? "", ...user.roles]
      .join(" ")
      .toLowerCase();
    return searchable.includes(normalizedQuery);
  });
  const visibleIndex = Math.min(selectedIndex, Math.max(0, matches.length - 1));
  const selectedUser = matches[visibleIndex]
    ? lookupUser(SAMPLE_USERS, matches[visibleIndex].id)
    : null;

  React.useEffect(() => {
    setSelectedIndex(0);
  }, [query]);

  React.useEffect(() => {
    const onKeypress = (character: string | undefined, key: KeypressKey): void => {
      if (key.name === "C-c" || (key.ctrl && key.name === "c")) {
        screen.destroy();
        return;
      }

      if (key.name === "escape") {
        setQuery("");
        setSelectedIndex(0);
        return;
      }

      if (key.name === "backspace" || key.name === "delete") {
        setQuery((current) => current.slice(0, -1));
        return;
      }

      if (matches.length > 0 && key.name === "up") {
        setSelectedIndex((current) => (current === 0 ? matches.length - 1 : current - 1));
        return;
      }

      if (matches.length > 0 && key.name === "down") {
        setSelectedIndex((current) => (current + 1) % matches.length);
        return;
      }

      if (character && character.length === 1 && !key.ctrl && !key.meta) {
        setQuery((current) => current + character);
      }
    };

    screen.on("keypress", onKeypress);
    return () => {
      screen.removeListener("keypress", onKeypress);
    };
  }, [matches.length, screen]);

  return (
    <box width="100%" height="100%" style={{ bg: "black", fg: "white" }}>
      <box
        top={0}
        left={1}
        width="100%"
        height={1}
        content="USER DIRECTORY"
        style={{ fg: "cyan", bold: true }}
      />
      <box
        top={1}
        left={1}
        width="100%"
        height={1}
        content={`Search: ${query || "type to filter users"}`}
        style={{ fg: "yellow" }}
      />
      <box
        top={3}
        left={0}
        width="40%"
        height="100%-5"
        label=" Users "
        border={{ type: "line" }}
        style={{ border: { fg: "blue" } }}
      >
        {matches.length === 0 ? (
          <box top={0} left={1} content="No users match this search" />
        ) : (
          matches.map((user, index) => (
            <box
              key={user.id}
              top={index}
              left={1}
              width="100%-2"
              height={1}
              content={`${index === visibleIndex ? ">" : " "} ${userLabel(user)}`}
              style={{
                fg: index === visibleIndex ? "black" : "white",
                bg: index === visibleIndex ? "cyan" : "black",
              }}
            />
          ))
        )}
      </box>
      <box
        top={3}
        left="40%"
        width="60%"
        height="100%-5"
        label=" Selected User "
        border={{ type: "line" }}
        style={{ border: { fg: "blue" } }}
      >
        {selectedUser ? (
          <>
            <box top={1} left={2} content={userLabel(selectedUser)} style={{ fg: "green", bold: true }} />
            <box top={3} left={2} content={`ID: ${selectedUser.id}`} />
            <box top={5} left={2} content={`Roles: ${selectedUser.roles.join(", ")}`} />
          </>
        ) : (
          <box top={1} left={2} content="No user selected" />
        )}
      </box>
      <box
        bottom={0}
        left={1}
        width="100%"
        height={1}
        content="Type to filter · ↑/↓ navigate · Esc clear · Ctrl+C quit"
        style={{ fg: "gray" }}
      />
    </box>
  );
}

const screen = blessed.screen({
  autoPadding: true,
  smartCSR: true,
  title: "TypeScript User Directory",
});

render(<App screen={screen} />, screen);
