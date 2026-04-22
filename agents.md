# Minecraft 26.1 Source Code

This repository contains an extracted copy of the decompiled Minecraft 26.1 source code under `fabric/sources/26.1/`.

## What is this?
When updating mods to newer versions of Minecraft (e.g., migrating to 26.1), the method signatures, fields, and class hierarchies in the underlying game code frequently change. This directory acts as a local, fully searchable repository of the vanilla Minecraft codebase (both common and client-side logic).

## How to use it
As an AI Agent or developer, you can use these sources to investigate API changes and resolve compilation errors.

**Best Practices for Agents:**
1. **Search for references:** If you encounter an `InvalidInjectionException` or a `NoSuchMethodError`, use the `grep_search` tool within `fabric/sources/26.1/` to find the target class and see how the method signature has changed.
   * Example: `grep_search(SearchPath="fabric/sources/26.1", Query="slotClicked", MatchPerLine=true)`
2. **Examine classes:** Once you find the target file, use the `view_file` tool to inspect the exact parameters, fields, and implementations to properly configure your Mixins or API calls.
3. **Trace logic:** You can trace how vanilla Minecraft implements specific mechanics by searching interfaces or base classes within this directory to understand the intended usage in the current version.

*Note: The `fabric/sources/` directory is ignored by Git to prevent bloating the system repository.*
