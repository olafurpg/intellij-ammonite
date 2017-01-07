# intellij-ammonite

WIP

To manually test plugin on a fresh intellij installation, run `sbt pluginRun`.
I recommend you "Create new project", it doesn't have to be an sbt project, just a scala project.
Create an empty file `foo.amm`, the extension needs to be `.amm` in order to not conflict with `.sc`.
Hopefully, we can use `.sc` sometime later.

Most of build.sbt was adopted from here: https://github.com/psliwa/idea-composer-plugin.

