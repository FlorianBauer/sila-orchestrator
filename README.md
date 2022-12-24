# ![sila-orchestrator logo](doc/pictures/sila-orchestrator-logo-128px.png) sila-orchestrator


A simple, dynamic [SiLA 2](https://sila-standard.com/) compliant client for coordinating various 
services. The main goal of this client is to easily link different SiLA 2 commands together, to 
create an automated, continuous workflow between distinct devices. For a short function overview and 
a description on how to use this software, take a look into the [User Guide](doc/UserGuide.md). For 
the more advanced usage inside a terminal, console or script, see the [CLI Guide](doc/CliGuide.md).

![The sila-orchestrator client](doc/pictures/sila-orchestrator-gui.png)


## Installation

To run this software, a installation of [JDK](https://jdk.java.net/) or [OpenJDK](https://adoptopenjdk.net/) in version >= 11 or higher is required.

Downloads for the latest versions are available at the [Release Page](https://github.com/FlorianBauer/sila-orchestrator/releases).

Windows user can download the `sila-orchestrator.exe` and start the application directly.

Alternatively, download the `sila-orchestrator.jar` package and start the client by typing the following into a Terminal/Shell: `java -jar sila-orchestrator.jar`.

To use the client in command-line mode (e.g. within a script), take a look into the [CLI Guide](doc/CliGuide.md).


### Build the Project

First, clone the repository.
```bash
git clone https://github.com/FlorianBauer/sila-orchestrator.git
```

To build this project, a [JDK](https://jdk.java.net/) or [OpenJDK](https://adoptopenjdk.net/) in version >= 11, as well as a moderately current Maven installation is required. Enter the project directory and use the following command to start the build process:

```bash
cd path/to/sila-orchestrator/
mvn clean install -DskipTests
```

After that, the resulting executable files are located in the `orchestrator-client/target`-directory as 
`sila-orchestrator.jar` and `sila-orchestrator.exe`.


### Menu Item Installer for Linux

For Ubuntu/Debian based Linux distributions, a menu-item can be created. Simply execute the 
`add-menuitem.bash` script in the `etc` directory of the project. To uninstall the system entry, run
the corresponding `remove-menuitem.bash` script.
