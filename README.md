## XiaomiToolV2

This is the source code of the Xiaomi modding tool XiaomiTool V2 (www.xiaomitool.com)

### State of the project
The project is currently semi-abandoned because of a bunch of reasons. If you want to fix bugs or do stuff, just fork the repo.
This project started as a student-level project: many bad practices has been used and bad choices have been made, making this project costly to maintain. For example:
1. Use of Java (1.8) instead of other, less verbose, languages (eg Kotlin, Python)
2. No CI or CD, releasing a build for all the OSes is a hassle, and it's easy to introduce bugs.
3. No documentation
4. No testing
5. Bad architecture (no separated modules, circular dependencies, not well-designed classes, ...)
6. Limited to Xiaomi devices

Even if some of these points could be fixed fairly easily, the base project is still not good enough to make it worth for me. The required change is so radical that it would be nearly as costly as rewriting the entire thing from scratch.

### Building and Running 

This project uses gradle, therefore you can just clone the repo and use:

``` gradlew build ``` and ``` gradlew run ```

Building is going to create the jar file only, which is not enough to make XiaoMiTool work: you will also need to bundle it with the resources needed (the `res` directory in the repo).
Make sure to select the repository branch corresponding to your target platform OS (Windows, Mac, Linux), as the resources files are different.

#### Bundling and distributing

The relative directory `res/tools` must contain the right tools (`adb`, `fastboot` for each platform, also driver related files for Windows) for the target OS. 
You can get the tools from the different branches of this repository (Windows, Linux, Mac).
Please keep in mind that `adb` and `fastboot` are NOT the generic ones that you can download from the Internet, but custom ones compiled specifically for the MIUI. If you don't use the ones from this repo, you will lose the possibility of unlocking the bootloader and flashing MIUI roms via stock recovery.

For the Windows repo, it's also advisable to keep the `res/driver` directory as it contains the driver that XiaoMiTool will install on Windows to be able to connect to the devices. Also, it's advisable to start the program with administrative priviledges (you can use the launch4j launcher to do that) to be able to install the drivers.

The java version used for this gradle configuration is java 11, however the source code is compatible with java 1.8, making it possible to compile a version for 32bit jre 1.8.

XiaoMiTool V2 uses JavaFX for the gui, therefore if you want to create a bundle, you have to create a JRE image with the JavaFX module. You can get more details on how to do that on the [official JavaFX guide](https://openjfx.io/openjfx-docs/). If you compile the project for JRE 1.8, JavaFX is already bundled in the standard JRE.

If you want to modify the code and create a distributable bundle, the easiest way is probably to take a previous bundle release, extract the files, replace the jar file and repack it. 

### Issues

As stated in the `State of the project` section, this project is semi-abandoned. Don't spend too much time on them, they might just be ignored.

