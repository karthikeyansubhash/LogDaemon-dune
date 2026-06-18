# Pull Request Content Generator

This prompt helps you generate a comprehensive pull request description by analyzing code changes based on branch analysis results, creating diagrams, and structuring the PR content.

## Required Information
<!-- 
When using this prompt, please provide the following information:
1. PR Title (with Jira ticket number, e.g. [DUNE-123456] Add Feature X)
2. Source branch and target branch information
3. Branch analysis results (from branch-analysis-comprehensive.md)
4. Class file path(s) to analyze
5. Test file path(s) related to the changes
-->

## Instructions

1. Use the comprehensive branch analysis results as the foundation for PR content
2. Analyze and summarize code modifications by class and category
3. Analyze and summarize test code changes
4. Create PlantUML sequence diagrams for the modified classes
5. Generate comprehensive PR content in English and Korean

## Analysis Steps

### 1. Branch Analysis Integration

First, integrate the comprehensive branch analysis results:
- Commit history and timeline
- File change statistics and breakdown
- Category-based change classification
- Impact analysis and recommendations

### 2. Code Changes Analysis by Category

Categorize and analyze changes based on the branch analysis:
- **Dependency Changes**: Library version updates, new dependencies
- **New Features**: New classes, methods, functionality
- **Refactoring**: Code structure improvements, method renaming, package reorganization
- **Configuration Changes**: Manifest, build settings, resource files
- **Bug Fixes**: Error corrections, exception handling improvements
- **Documentation**: README, comments, documentation files

### 3. Detailed Code Analysis by Class

For each modified class, summarize and add to the PR markdown file:
- Purpose of the class
- Changes made (with line-by-line details)
- Impact on the system
- Dependencies affected
- Related configuration changes

### 4. Test Code Analysis

For test code changes, analyze and append to the PR markdown file:
- Test coverage achieved
- Testing approach (unit tests, instrumentation tests)
- Edge cases covered
- Any mocking or special test configurations
- Test statistics and coverage metrics

### 5. Build and Configuration Analysis

Analyze build and configuration file changes:
- Gradle file changes (dependencies, version updates)
- Android manifest changes
- Other configuration file changes
- Impact on build process

### 6. Create PlantUML Sequence Diagrams

Generate sequence diagrams showing the interaction flow in the modified classes and add references to them in the PR markdown file:
- Create PlantUML file : en, ko

## Output Format

The generated PR file will be created in `.github/pullrequest/{JIRA-ID}_{PR-Title}.md` with the following structure:

```markdown
# [JIRA-ID] PR Title

## Description
<!--- Replace 'xxxxx' with the Jira issue ID that is related to this change -->
[DUNE-xxxxx] Jira subject or summary
<!--- Then describe your changes in detail -->

### Total Summary
- **One-line Summary**: Core purpose and main changes in one sentence
- **Background**: Context and motivation for the changes
- **Key Technical Decisions**: Important architectural or design choices
- **Expected Impact**: Anticipated effects and benefits

### Branch Analysis Summary
- **Branch**: `source-branch` → `target-branch`
- **Commits**: X commits over Y days
- **Files Changed**: X files (A additions, D deletions)
- **Change Categories**: Primary focus areas

### Detailed Changes Analysis

#### File Statistics
- **Total Files**: X modified, Y added, Z deleted
- **By Type**: 
  - Java files: X files (Y lines changed)
  - Gradle files: X files (Y lines changed)
  - XML files: X files (Y lines changed)
  - Other: X files (Y lines changed)

#### Changes by Category

##### Dependency Changes
- **Library Updates**: List of updated dependencies with version changes
- **New Dependencies**: Newly added libraries and their purposes
- **Removed Dependencies**: Deprecated or removed libraries

##### New Features
- **New Classes**: List of new classes and their purposes
- **New Methods**: Key new methods and their functionality
- **Enhanced Functionality**: Improvements to existing features

##### Refactoring
- **Code Structure**: Structural improvements and reorganization
- **Method Renaming**: Renamed methods and their reasons
- **Package Changes**: Package structure modifications

##### Configuration Changes
- **Build Settings**: Gradle configuration changes
- **Manifest Changes**: Android manifest modifications
- **Resource Changes**: Resource file updates

##### Bug Fixes
- **Fixed Issues**: Specific bugs or issues resolved
- **Exception Handling**: Improved error handling
- **Edge Cases**: Addressed edge cases

##### Documentation
- **Code Comments**: Added or updated code documentation
- **README Updates**: Documentation file changes
- **API Documentation**: Interface and method documentation

### Code Changes by Class

#### Class Name
- **Purpose**: Brief description of the class purpose
- **Changes**: Summary of changes made
- **Impact**: Impact on the system
- **Dependencies**: External libraries or dependencies
- **Configuration Impact**: Related configuration changes

### Test Coverage

#### Unit Tests
- **Unit Test File**: Description of unit test coverage
- **New Tests**: Newly added test cases
- **Modified Tests**: Updated test cases
- **Coverage Metrics**: Test coverage statistics

#### Instrumented Tests
- **Instrumented Test File**: Description of instrumented test coverage
- **Integration Tests**: Integration test scenarios
- **UI Tests**: User interface test coverage

#### Edge Cases
- **Edge Cases Covered**: Specific edge cases addressed by tests
- **Error Scenarios**: Error handling test cases
- **Performance Tests**: Performance-related test scenarios

#### Coverage Assessment
- **Overall Coverage**: Comprehensive test coverage evaluation
- **Coverage Gaps**: Areas that may need additional testing
- **Quality Metrics**: Code quality and test effectiveness

### Build and Configuration Impact

#### Gradle Changes
- **Build Script**: Changes to build.gradle files
- **Dependencies**: Version updates and new dependencies
- **Build Process**: Impact on build and compilation

#### Android Manifest
- **Permissions**: Permission changes
- **Components**: Service, receiver, or activity declarations
- **Configuration**: App configuration changes

#### Resource Files
- **Layout Changes**: UI layout modifications
- **String Resources**: Text and localization updates
- **Drawable Resources**: Image and icon changes

### Sequence Diagrams
- [Class Operations](.github/diagrams/{JIRA-ID}_{PR-Title}.puml)
- [New Feature Flow](.github/diagrams/{JIRA-ID}_{PR-Title}_feature.puml)
- [Error Handling](.github/diagrams/{JIRA-ID}_{PR-Title}_error.puml)

### Impact Analysis

#### Functional Impact
- **Core Features**: Impact on main application functionality
- **New Capabilities**: Added features and capabilities
- **Behavioral Changes**: Modified application behavior

#### Technical Impact
- **Performance**: Performance implications
- **Security**: Security considerations
- **Compatibility**: Backward compatibility assessment
- **Dependencies**: Impact on external dependencies

#### Quality Impact
- **Code Quality**: Code quality improvements
- **Maintainability**: Code maintainability enhancements
- **Testing**: Test quality and coverage improvements

## Types of changes

- [ ] Bug fix
- [ ] New feature (non-breaking change which adds functionality)
- [ ] Breaking change (fix or feature that would cause existing functionality to
  not work as expected)
- [ ] Improvement/refactoring (non-breaking change that make things better)

## Testing - Describe how this was qualified

<!--- Help give the reviewer(s) an understanding of how this change set was tested -->
<!--- Reviewers, if this isn't filled out it might be a good conversation to start and document -->

- [ ] unit tests (describe)
- [ ] instrumented test (describe)
- [ ] API test (describe)
- [ ] simulator tests (make sure the tests are merged with latest code and executed)
- [ ] hardware tests (make sure the tests are merged with latest code and executed)

## Diagram Structure

The PlantUML diagram will be created in `.github/diagrams/{JIRA-ID}_{PR-Title}.puml`.
- Include all sequence diagrams in a single file
- Create clear section headers for different workflows
- Add appropriate comments to explain the diagram flow

## Usage Example

```
Please generate a PR file for:

PR Title: [DUNE-248535] Store Clientinfo Securely

Class files:
- app/src/main/java/com/hp/jetadvantage/link/system/data/SharedPreference.java

Test files:
- app/src/test/java/com/hp/jetadvantage/link/system/data/SharedPreferenceTest.java
- app/src/androidTest/java/com/hp/jetadvantage/link/system/data/SharedPreferenceInstrumentedTest.java
```

