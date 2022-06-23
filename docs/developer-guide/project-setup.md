If you are interested in developing and building the project please read the following the instructions carefully.

## Version control

To get sources of the project, please execute:

```sh
git clone https://github.com/camunda-communit-hub/camunda-platform-7-rest-client-spring-boot.git
cd camunda-platform-7-rest-client-spring-boot
```

We are using gitflow in our git SCM for naming b ranches. That means that you should start from `master` branch,
create a `feature/<name>` out of it and once it is completed create a pull request containing
it. Please squash your commits before submitting and use semantic commit messages, if possible.

## Project Build

Perform the following steps to get a development setup up and running.

```sh
./mvnw clean install
```

## Integration Tests

By default, the build command will ignore the run of `failsafe` Maven plugin executing the integration tests
(usual JUnit tests with class names ending with ITest). In order to run integration tests, please
call from your command line:

```sh
./mvnw -Pitest failsafe:verify
```

## Project build modes and profiles

### Camunda Version

You can choose the used Camunda version by specifying the profile `camunda-ee` or `camunda-ce`. The default
version is a Community Edition. Specify `-Pcamunda-ee` to switch to Camunda Enterprise edition. This will
require a valid Camunda license. You can put it into a file `~/.camunda/license.txt` and it will be detected
automatically.

### Documentation

We are using MkDocs for generation of a static site documentation and rely on markdown as much as possible.

!!! note

    If you want to develop your docs in 'live' mode, run `mkdocs serve` and access
    the http://localhost:8000/ from your browser.

For creation of documentation, please run:

#### Generation of JavaDoc and Sources

By default, the sources and javadoc API documentation are not generated from the source code. To enable this:

```sh
./mvnw clean install -Pcommunity-action-maven-release -Dgpg.skip=true
```

#### Starting example applications

To start applications, either use your IDE and create
run configuration for the class:

* `org.camunda.community.rest.example.standalone.CamundaRestClientExampleApplication`
* `org.camunda.community.rest.example.processapplication.CamundaRestClientExampleApplicationWithEngineProvided`

Alternatively, you can run them from the command line:

```sh
./mvn spring-boot:run -f examples/example
./mvn spring-boot:run -f examples/example-provided
```

### Continuous Integration

GitHub Actions are building all branches on commit hook (for codecov).
In addition, a GitHub Actions are used to build PRs and all branches.

### Release Management

The release is produced by using the GitHub feature to "Publish a Release". To do so, please
"close a milestone" and a special action will generate automatically collected issues and
PRs to generate the Release notes. It will create a tag, which will be built if this
release get published. Now "Publish release" and the GH action will create a new
release and publish it into Camunda Artifactory and Maven Central Staging. To 
release it to the public, please create an issue and assign it to someone of
Camunda [@camunda-community-hub/devrel](https://github.com/orgs/camunda-community-hub/teams/devrel)

#### What modules get deployed to repository

Every Maven module is enabled by default. If you want to change this, please provide the property

```xml
<maven.deploy.skip>true</maven.deploy.skip>
```

inside the corresponding `pom.xml`. Currently, all `examples` are _EXCLUDED_ from publication into Maven Central.
