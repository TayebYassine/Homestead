# Contribution Guidelines

Thank you for your interest in contributing to Homestead! Whether you are fixing a bug, improving the documentation, or adding a feature, your help keeps the project moving. To keep the codebase consistent and reviews smooth, please follow these guidelines when contributing.

---

## Code Style & Conventions

Homestead follows a small, consistent coding style. Matching it keeps diffs readable and lets reviewers focus on behavior instead of formatting.

### Java Coding Standards

- Indent with **4 spaces** per level.
- Use the **[K&R (Kernighan & Ritchie)](https://en.wikipedia.org/wiki/Indentation_style#K&R) brace style**: open the brace on the same line as the statement, and close it on its own line.

### Spacing & Formatting

- Put spaces around operators (`a = b + c`, not `a=b+c`).
- Put a space after keywords such as `if`, `for`, and `while` (`if (condition)`).
- Keep one blank line between methods and between logical blocks of code.

---

## Pull Request (PR) Guidelines

Every pull request goes through the same process: prepare a branch, check your work, then describe it clearly.

### Before Submitting a PR

1. **Fork & Sync**
   - Fork the repository and sync your fork with the latest `main` branch. If a development branch exists (for example, `dev-5.1.0.0` or `dev-4.3.0`), fork that branch instead of `main` and base your changes on it.

2. **Code Quality Checks**
   - Ensure your code follows the style guide above.
   - Run your changes on a Minecraft testing server to confirm they behave as expected.

3. **Commit Messages**
   - Follow **[Conventional Commits](https://www.conventionalcommits.org/)** so the history stays readable.
   - Keep commits **small and logical** (avoid vague messages such as "fixed stuff").

### PR Submission

- Provide a **clear title** that summarizes the change.
- Add a **detailed description** covering:
  - What changes were made.
  - Why they were needed.
  - Related issues (optional links).

Questions about the process? Ask on the [Discord server](https://discord.gg/uh7gqDY6sz) or open a [GitHub issue](https://github.com/TayebYassine/Homestead/issues).
