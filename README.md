# gradle-tools

This plugin applies shared configuration between all Kord projects

It is currently not designed to be used in other projects

# Features

## Versioning

This plugin reads the current version from git, according to the following rules

1. Use the current tag name if possible
2. If the current branch is `main` use `{nextPlannedVersion}-SNAPSHOT` from `gradle.properties`
3. Otherwise, use `{branch}-SNAPSHOT` whilst replacing `/` with `-`

## CI task grouping

The plugin creates meta-tasks that allow to publish artifacts from the desired OS

### Task list

| Name                              | Description                                                                                     | Runs on (when using specific OS task) |
|-----------------------------------|-------------------------------------------------------------------------------------------------|---------------------------------------|
| `publishCommon`                   | Publishes the KMP, JVM & JS publication                                                         | Specified common host (usually Linux) |
| `publishCommonToMavenLocal`       | Publishes the KMP, JVM & JS publication to maven local                                          | Specified common host (usually Linux) |
| `testCommon`                      | Runs JVM & JS tests and the Kotlin Binary Compatibility validator                               | Specified common host (usually Linux) |
| `publishLinux`                    | Publishes the Kotlin/Native publications targeting Linux                                        | Linux                                 |
| `publishLinuxToMavenLocal`        | Publishes the Kotlin/Native publications targeting Linux to maven local                         | Linux                                 |
| `testLinux`                       | Runs Kotlin/Native tests targeting Linux                                                        | Linux                                 |
| `publishWindows`                  | Publishes the Kotlin/Native publications targeting Windows                                      | Windows                               |
| `publishWindowsToMavenLocal`      | Publishes the Kotlin/Native publications targeting Windows to maven local                       | Windows                               |
| `testWindows`                     | Runs Kotlin/Native tests targeting Linux                                                        | Windows                               |
| `publishApple`                    | Publishes the Kotlin/Native publications targeting Apple platforms                              | MacOS                                 |
| `publishAppleToMavenLocal`        | Publishes the Kotlin/Native publications targeting Apple platforms to maven local               | MacOS                                 |
| `testApple`                       | Runs Kotlin/Native tests targeting Apple platforms                                              | MacOS                                 |
| `publishForCurrentOs`             | Publishes the Kotlin/Native publications targeting the current OS's platform                    | Any                                   |
| `publishForCurrentOsToMavenLocal` | Publishes the Kotlin/Native publications targeting the current OS's platform     to maven local | Any                                   |
| `testOnCurrentOS`                 | Tests all platforms targeting the current OS's platform                                         | Any                                   |

