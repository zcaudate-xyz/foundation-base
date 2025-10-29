{
  "file_path": "/path/to/your/test/file.clj",
  "task": "Refactor Clojure Test Docstrings Using Source Context",
  "instructions": [
    "**Scan** the file at `file_path` for all Clojure forms.",
    "**Identify** every form that starts with the symbol `fact`.",
    "For each `fact` form, **extract the function symbol** from the preceding metadata (look for the `:refer` key, e.g., `script.css/generate-style`).",
    "**Locate and analyze the source code** of this function in the corresponding `src` directory (e.g., `src/script/css.clj`) to understand the function's full contract (inputs, outputs, purpose, and existing docstring).",
    "The **docstring** to be modified is the first argument to the `fact` form (a string literal).",
    "**Modify** the existing docstring to be more descriptive and informative, synthesizing information from the test body and the source function's context.",
    "The new docstring must clarify *what* the test is verifying about the function's behavior, especially in relation to the specific input and expected output of the test case.",
    "**Example Transformation for `generate-css` (Source-Aware):**",
    "  * If the source docstring says the function 'creates a stylesheet from a vector of rules', the test docstring should be refined to: `\"generates a complete stylesheet by correctly processing a vector containing multiple [selector map] rules into a formatted CSS string.\"`",
    "**Preserve** all surrounding metadata (e.g., `^{:refer ...}`), all test code within the `fact` body, and file structure (`ns`, `use`, `require`).",
    "**Output** the complete, modified file content."
  ],
  "context": "The test file verifies functions in the `script.css` namespace, covering both CSS generation and parsing. The agent should use the source code to provide the most accurate and specific descriptions possible for each test."
}
