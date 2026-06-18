---
applyTo: '**/*.xml'
description: 'Android XML Resource Style Guidelines'
---

# Android XML Resource Style Guidelines

## Layout Files

1. IDs use camelCase with type prefixes: `btnSubmit`, `tvTitle`, `etUserName`
2. Each element is placed on a new line and indented with 4 spaces (no tabs)
3. Attributes are ordered alphabetically within each element
4. Use `@string/` resources for all text, avoid hardcoded strings
5. Use `@dimen/` resources for all dimensions, avoid hardcoded values
6. Use `@color/` resources for all colors, avoid hardcoded color values
7. Use self-closing tags for empty elements

## Resource Naming Conventions

1. Layout files: use prefixes like `activity_`, `fragment_`, `dialog_`, `item_`, `layout_`
2. Drawable resources: use prefixes like `btn_`, `ic_`, `bg_`, `divider_`, `selector_`
3. Color resources: name by semantic meaning, e.g., `colorPrimary`, `textColorSecondary`
4. Dimension resources: name by usage, e.g., `marginSmall`, `textSizeLarge`
5. String resources: group by feature or screen, e.g., `login_error`, `profile_title`

## Attribute Formatting

1. Place the first attribute on the same line as the element name if it fits within 100 characters
2. If not, place each attribute on a new line, indented by 4 spaces
3. Close the tag on a new line if attributes are multiline

## Supporting Multiple Screen Sizes

1. Provide alternative layouts for different screen sizes: `layout-sw340dp`, `layout-sw750dp`, etc.
2. Use `ConstraintLayout` for flexible layouts
3. Use `dp` for layout dimensions and `sp` for text size, avoid hardcoded pixel values

## Accessibility

1. All image elements must have a `contentDescription` attribute
2. Ensure sufficient color contrast for text and backgrounds
3. Touch targets should be at least 48dp x 48dp

## Comments

1. Use XML comments to explain complex or non-obvious sections:
    ```xml
    <!-- This section handles the login form -->
    ```
2. Mark TODOs with a clear comment:
    ```xml
    <!-- TODO(junpyo-kim): Refactor this layout for tablets -->
    ```

## Best Practices

1. Remove unused resources regularly
2. Avoid deep nesting of layout elements (prefer flatter hierarchies)
3. Use `tools:` namespace for design-time attributes only
4. Use `@style/` for consistent appearance and theming
5. Group related resources in separate files (e.g., `colors.xml`, `dimens.xml`, `strings.xml`)