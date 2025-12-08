import React from 'react';
import Editor from '@monaco-editor/react';
import { useAppState } from '../../state';

export function MonacoEditor({ value, onChange, language = "javascript", readOnly = false, options = {}, ...props }) {
    const { theme } = useAppState();

    // Map our theme 'dark' / 'light' to Monaco's 'vs-dark' / 'light'
    const editorTheme = theme === 'dark' ? 'vs-dark' : 'light';

    const handleEditorChange = (value, event) => {
        if (onChange) onChange(value || "");
    };

    return (
        <Editor
            height="100%"
            language={language}
            value={value}
            theme={editorTheme}
            onChange={handleEditorChange}
            options={{
                readOnly: readOnly,
                minimap: { enabled: true },
                stickyScroll: { enabled: false },
                scrollBeyondLastLine: false,
                fontSize: 13,
                automaticLayout: true,
                ...options
            }}
            {...props}
        />
    );
}
