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
                scrollBeyondLastLine: false,
                fontSize: 14,
                automaticLayout: true,
                ...options,
                minimap: { enabled: false },
                stickyScroll: { enabled: false },
            }}
            {...props}
        />
    );
}
