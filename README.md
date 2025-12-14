# SENG302 HomeHelper Project

Basic project using ```gradle```, ```Spring Boot```, ```Thymeleaf```, and ```GitLab CI```.

HomeHelper is an application that supports users in their renovation goals, allowing them to create renovation records
with rooms and tasks.
Users can also view their profile and change their profile picture.

## Dependencies

### Core Dependencies

- Java version >=
  21, [click here to get the latest stable OpenJDK release (as of time of writing)](https://jdk.java.net/21/)
- Spring Boot Starter
- Spring Boot Starter Thymeleaf
- Spring Boot Starter Web
- Spring Boot Starter Data JPA
- Spring Boot Starter Mail
- Spring Boot Starter Security
- H2 database
- Apache Tika

### Development and Testing

- Spring Boot DevTools
- Spring Boot Starter Test
- Spring Security Test
- Jacoco
- Cucumber
- JUnit

### Web Assets

- WebJars Locator
- Bootstrap

## How to run

#### 1.1 - Environment variables

In IntelliJ (or your terminal) set the following environment variables as follows:

```
MAIL_USERNAME=donotreplyhomehelper@gmail.com
MAIL_PASSWORD=iywyeqimdxutoarq
```

If working in IntelliJ, go to Run -> Edit Configurations -> bootRun -> Environment variables and paste in this line:

```
MAIL_USERNAME=donotreplyhomehelper@gmail.com;MAIL_PASSWORD=iywyeqimdxutoarq
```
### 1.2 - Setting up API access tokens when running locally

To use address autocomplete, you will need to obtain an API access token by signing up for Map box [here](https://account.mapbox.com/auth/signup/?route-to=https%3A%2F%2Fconsole.mapbox.com%2F%3Fauth%3D1)

Once you obtain your token set the environment variable in Intellij or in the terminal as follows

```
MAPBOX_ACCESS_TOKEN=insert-your-api-token-here
```

### 1.3 - Setting up API key when running locally

To use the AI feature, you will need to obtain and API key by signing up for Google AI Studio [here](https://aistudio.google.com/api-keys)

Once you obtain your key set the environment variable in Intellij or in the terminal as follows

```
GEMINI_API_KEY=insert-your-api-key-here
```

### 1.4 - Running the project

From the root directory ...

On Linux:

```
./gradlew bootRun
```

On Windows:

```
gradlew bootRun
```

By default, the application will run on local port 8080 [http://localhost:8080](http://localhost:8080)

### 2 - Using the application

#### Default user credentials to sign in with:

> User with enough tasks and records for pagination, "Luxury Bathroom Remodel" and "Modern Kitchen Remodel" has enough tasks for testing
> - Email: jane.doe@gmail.com
> - Password: Abc123!!

> User with all public renovation records
> - Email: bob.smith@gmail.com
> - Password: Abc123!!



Everything should now be up and running, so you can load up your preferred browser and connect to the application at
[http://localhost:8080](http://localhost:8080)

You can also check the [Deployed Production Server](https://csse-seng302-team900.canterbury.ac.nz/prod/home.)  or the
[Deployed Staging Server](https://csse-seng302-team900.canterbury.ac.nz/test/)

To register an account, you will need to use an email that you can access because the application will email
confirm the account.

Once signed in, you can create and edit renovation records with rooms and tasks. You can also view your profile page and
change your profile picture.
You can update your account's password and reset it if need be.

## How to run tests

### Unit tests

From the root directory ...

On Linux:

```
./gradlew test
```

On Windows:

```
gradlew test
```

### Integration tests

From the root directory ...

Ensure that the following environment variables are set:

```
MAIL_USERNAME=donotreplyhomehelper@gmail.com
MAIL_PASSWORD=iywyeqimdxutoarq
```

On Linux:

```
./gradlew integration
```

On Windows:

```
gradlew integration
```

### Code Coverage

On Linux:

```
./gradlew jacocoTestReport
```

On Windows:

```
gradlew jacocoTestReport
```

## Contributors

- Luke Armstrong
- Sophia Copley
- Corey Hines
- James Li
- Heather Peter
- Hayden Topperwien
- David Williamson
- SENG302 teaching team

## References

- [Spring Boot Docs](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)
- [Spring JPA docs](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [Thymeleaf Docs](https://www.thymeleaf.org/documentation.html)
- [Learn resources](https://learn.canterbury.ac.nz/course/view.php?id=17797&section=8)
