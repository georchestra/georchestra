# Contributing

First of all: thanks for contributing!  
We welcome all contributions.

## Code conventions

* Javascript:
  * Four space indents.
  * No tabs.
  * Always use brackets after a test even if there's a single line of code.
* Java:
  * Tab indents.
  * No spaces.
  * No trailing spaces or tabs.

## Commits policy

* Commits must be atomic: just one consistent change per commit.
* When possible, refer to an issue in the commit message, for example adding
  `(see #xx)` at the end of the commit message, or `fixes ##` to close the issue.
* Never commit environnement related changes.

## Submitting a pull request

* Keep pull requests as simple as possible. Remember: one PR targets one and only one feature or fix.
* A bugfix PR should target the oldest supported branch where the bug appears. Releases get bugfixes during one year.
* If a PR impacts the deployment procedure, it should also include documentation.
* If a PR requires the admin to update any database or file, it should also update the [release notes](RELEASE_NOTES.md).

Failure to do so will result in longer acceptance time.  
In addition, PRs which have not been updated by their author 1 year after the latest comment might get closed.
