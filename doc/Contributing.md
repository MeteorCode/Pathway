Pathway is open-source software licensed under the [MIT license](https://github.com/MeteorCode/Pathway/blob/master/LICENSE). Therefore, while Pathway is maintained by MeteorCode Laboratories, MeteorCode will accept contributions to Pathway from any individual interested in improving Pathway, provided that they follow the Pathway Best Practices and Coding Style guidelines.

## What You'll Need

In order to compile and test Pathway, you will need the following:
 + A recent Java JDK installation; Java 8 is required.
 + Git
 + The text editor or IDE of your choice

The remainder of Pathway's dependencies, including the SBT build system, will be downloaded automatically by the SBT wrapper script (`./sbt`).

## How to Contribute

If you wish to contribute to Pathway, please fork the Pathway repository, make any changes you wish to contribute, and then create a [pull request](https://github.com/MeteorCode/Pathway/pulls) to merge your changes.

Your pull request will be reviewed by the repository maintainers (@ArcticLight and @hawkw), who will determine whether or not it can be merged. In most cases, if your pull request is not approved, the maintainers will provide you with information on why your pull request was not merged and what you should do in order to make it merge-ready.

In order to ensure that this process is as efficient as possible, please ensure that your contributions conform to the Pathway Coding Style and Best Practices (as described in the next section), and run the Pathway test suite locally on your development machine to ensure that your contribution builds successfully. Furthermore, please try to provide a detailed explanation of what your pull request adds or fixes, how this is implemented, and why it is necessary. This will make the review process much easier for both the contributor (you) and the repository maintainers. Pull requests that are not adequately described will not be approved to merge.

If you are unsure how to create a pull request, please consult the [GitHub documentation](https://help.github.com/articles/using-pull-requests/) for more information.

#### Best Practices & Coding Style

All Scala code contributed to Pathway should conform to the [Effective Scala](http://twitter.github.io/effectivescala/) guidelines. We use the [Codacy](https://www.codacy.com/app/MeteorCode-Labs/Pathway/issues) automated code review service to ensure that all contributions are Effective Scala-compliant. Any pull requests will not be merged to master until they are fully compliant with Effective Scala.

We ask that the coding style, naming, and formatting conventions described in Effective Scala and the [Scala Style Guide](http://docs.scala-lang.org/style/) be followed whenever possible. In order to assist Pathway contributors in following these conventions, we have provided an [Editor Config](http://editorconfig.org/) file in the Pathway repository. Compatible text editors and IDEs will automatically use this file to set your editor defaults to the Pathway style guide while working on Pathway files.

Ideally, all pull requests should add complete unit tests for all code added. We use [codecov.io](https://codecov.io/github/MeteorCode/Pathway) to track test test coverage for all commits and pull requests. For a pull request to be merged, we require that the _patch coverage_ (the number of added lines covered by tests) of that pull request to be greater than 90%.


## Communicating with Pathway's Maintainers

#### Contacting the Maintainers

If you have any questions, either about how to contribute to Pathway or about Pathway itself, please feel free to contact the Pathway maintainers (@hawkw and @ArcticLight). We are happy to answer any questions. If you're interested in joining a discussion concerning Pathway and receiving updates on Pathway development, you may wish to consider joining the Pathway [Gitter chat channel](https://gitter.im/MeteorCode/Pathway?utm_source=share-link&utm_medium=link&utm_campaign=share-link).

#### Bug Reports and Feature Requests

If you have found any bugs in Pathway or would like to request a feature or improvement, please feel free to open an issue on the [Pathway issue tracker](https://github.com/MeteorCode/Pathway/issues). If you are reporting a bug, please provide a complete and detailed description of the issue, a description of the environment on which Pathway is running (i.e. your computer and operating system, Java Runtime Environment (JRE) version, Pathway version, et cetera), and any relevant stack traces or error logs. This will assist the maintainers in diagnosing and fixing the issue.

Before opening an issue, please ensure that there is not already an open issue for the problem or feature in question. If there is an open issue already, you may wish to confirm that bug. If you do so, please include a detailed bug report, as discussed above.
