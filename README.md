# ![sila-orchestrator logo](doc/pictures/sila-orchestrator-logo-128px.png) sila-orchestrator


A simple, dynamic [SiLA 2](https://sila-standard.com/) compliant client for coordinating various 
services. The main goal of this client is to easily link different SiLA 2 commands together, to 
create an automated, continuous workflow between distinct devices. For a short function overview and 
a description on how to use this software, take a look into the [User Guide](doc/UserGuide.md). For 
the more advanced usage inside the console or a script, see the [CLI Guide](doc/CliGuide.md).

![The sila-orchestrator client](doc/pictures/sila-orchestrator-gui.png)


### Build the Project

First, clone the repository.
```bash
git clone --recurse-submodules https://github.com/FlorianBauer/sila-orchestrator.git
```

To build this project, a JavaSE/JDK in version >= 11, as well as a moderately current 
Maven installation is required. Enter the project directory and use the following command to start 
the build process:

```bash
cd path/to/sila-orchestrator/
mvn clean install -DskipTests
```

After that, the resulting `*.jar`-file is located in the `orchestrator-client/target`-directory as 
`sila-orchestrator.jar`.


### Usage

Starting the sila-orchestrator GUI client: `java -jar sila-orchestrator.jar`.

To use the client within the command-line, take a look into the [CLI Guide](doc/CliGuide.md).


### Menu Item Installer for Linux

For Ubuntu/Debian based Linux distributions, a menu-item can be created. Simply execute the 
`add-menuitem.bash` script in the `etc` directory of the project. To uninstall the system entry, run
the corresponding `remove-menuitem.bash` script.
