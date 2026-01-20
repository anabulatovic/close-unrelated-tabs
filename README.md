# Close Unrelated Tabs

[![JetBrains Marketplace](https://img.shields.io/badge/Marketplace-Close-Unrelated-Tabs-blue)](https://plugins.jetbrains.com/plugin/29784-close-unrelated-tabs)
![Version](https://img.shields.io/jetbrains/plugin/v/29784-close-unrelated-tabs)
![Downloads](https://img.shields.io/jetbrains/plugin/d/29784-close-unrelated-tabs)

An IntelliJ‑based IDE plugin that helps you focus by automatically closing editor tabs that are not related to the current file.

When working in large codebases, it’s easy to end up with dozens of open files. **Close Unrelated Tabs** keeps only the files that actually matter for what you’re working on right now.

---

## What It Does

Right‑click on any editor tab and select **“Close Unrelated Tabs”** to close all open files except those that:

* **Reference the selected file** (imports, usages)
* **Are referenced by the selected file**

This allows you to instantly narrow your context to just the relevant files.

---

## Features

* **Configurable reference depth**
  Choose whether to keep only direct references or also transitive ones.

* **Tab protection**
  Never close:

    * Pinned tabs
    * Modified (unsaved) files
    * Test files

* **Preview before closing**
  See which tabs will be closed before confirming the action.

* **Exclude files by pattern**
  Define file patterns that should always stay open.

---

## How to Use

1. Open multiple files in the editor
2. Right‑click on the tab of the file you care about
3. Select **Close Unrelated Tabs**
4. Review the preview (if enabled) and confirm

---

## Configuration

You can customize the plugin behavior under:

**Settings / Preferences → Editor → Close Unrelated Tabs**

Available options include:

* Reference depth (direct vs transitive)
* Protect pinned tabs
* Protect modified files
* Protect test files
* Enable/disable preview
* Exclusion patterns

---

## IDE Compatibility

* IntelliJ IDEA (Community & Ultimate)
* Other JetBrains IDEs based on the IntelliJ Platform

---

## License

This project is licensed under the Apache 2.0 License.

---

If you have feedback, feature requests, or bug reports, feel free to open an issue or contribute!
