# SENG302 Template Project Overview

Welcome to the template project for SENG302 2024. In this README we have included some useful information to help you
get started. We advise you take some time reading through this entire document, as doing so may save you many headaches
down the line!

## Dependencies

This project requires Java version >=
21, [click here to get the latest stable OpenJDK release (as of time of writing)](https://jdk.java.net/21/)

## Technologies

This project makes use of several technologies that you will have to work with. Here are some helpful links to
documentation/resources for the big one:

- [Spring Boot](https://spring.io/projects/spring-boot) - Used to provide http server functionality
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa) - Used to implement JPA (Java Persistence API)
  repositories
- [h2](https://www.h2database.com/html/main.html) - Used as an SQL JDBC and embedded database
- [Thymeleaf](https://www.thymeleaf.org/) - A templating engine to render HTML on the server, as opposed to a separate
  client-side application (such as React)
- [Gradle](https://gradle.org/) - A build tool that greatly simplifies getting application up and running, even managing
  our dependencies (for those who did SENG202, you can think of Gradle as a Maven replacement)
- [Spring Boot Gradle Plugin](https://docs.spring.io/spring-boot/docs/3.0.2/gradle-plugin/reference/html/) - Allows us
  to more easily integrate our Spring Boot application with Gradle

## Quickstart Guide

### Building and running the project with Gradle

We'll give some steps here for building and running via the commandline, though IDEs such as IntelliJ will typically
have a 'gradle' tab somewhere that you can use to perform the same actions with as well.

#### 1 - Running the project

From the root directory ...

On Linux:

```
./gradlew bootRun
```

On Windows:

```
gradlew bootRun
```

By default, the application will run on local port 8080 ('http://localhost:8080')

#### 2 - Connecting to the UI from your browser

Everything should now be up and running, so you can load up your preferred browser and connect to the application at
[http://localhost:8080](http://localhost:8080). We have set up some example pages you can reach from the application
home '/', we suggest you have a play around with these to understand the basics of the new technologies involved.

#### 3 - What's included to play with

This template project includes basic demo functionality for handling forms and persisting the submissions at
`/demo/forms`
as well as a some basic Thymeleaf fragments at `/demo/fragments`.

For more information see these relevant resources:

- [More information about persistence using JPA](https://spring.io/guides/gs/accessing-data-jpa/)
- [Examples for using Thymeleaf fragments](https://www.baeldung.com/spring-thymeleaf-fragments)
- [Thymeleaf URL syntax](https://www.thymeleaf.org/doc/articles/standardurlsyntax.html)
- [More information about the Autowired annotation](https://www.baeldung.com/spring-autowire)

## Information about included build files/scripts

Whilst these scripts and files will not be of use until later in the course when you set up continuous integration (CI)
we have included files with default behaviour, and some for reference.

- `deployment-files-fyi/nginx/sites-available.conf`
    - Reference file showing the VMs NGINX config.
- `deployment-files-fyi/systemd-service/production.service`
    - Reference file showing the production environment service
      configuration. [See here for more information about .service files](https://www.shellhacks.com/systemd-service-file-example/)
- `deployment-files-fyi/systemd-service/staging.service`
    - Reference file showing the staging environment service configuration.
- `runner-scripts/production.sh`
    - Deployment shell script for running the production environment on a VM.
- `runner-scripts/staging.sh`
    - Deployment shell script for running the staging environment on a VM.
- `.gitlab-ci.yml`
    - The (GitLab specific) CI script for the project. This will cause your pipelines to fail/timeout until
      gitlab-runners are set up so feel free to ignore these for
      now. [For more information refer to GitLab's Documentation Here](https://docs.gitlab.com/ee/ci/yaml/gitlab_ci_yaml.html)