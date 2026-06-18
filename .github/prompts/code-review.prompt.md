---
mode: 'agent'
description: 'Perform code quality and security review'
tools: ['semantic_search', 'read_file', 'file_search', 'grep_search', 'list_code_usages', 'think']
---

# Code Review

Please review the current code and suggest improvements based on the following aspects:

1. **SonarQube Analysis**:
    - Use the installed SonarQube for IDE plugin to identify and summarize code issues and improvement suggestions.
    - For Java SonarQube rules, reference the official documentation at https://rules.sonarsource.com/java/RSPEC-{rule_number} for detailed explanations and examples (e.g., S1201 -> https://rules.sonarsource.com/java/RSPEC-1201).
    - Also refer to the project's SonarQube rules configuration file at [sonarqube-java-jules.xml](../rules/sonarqube-java-jules.xml) for project-specific rule settings and priorities.

2. **Code Quality**:
    - Check for code duplication
    - Identify complex methods (suggest refactoring)
    - Verify naming convention compliance
    - Evaluate the code based on the following Android coding guidelines: [android-coding-style.instructions.md](../instructions/android-coding-style.instructions.md)