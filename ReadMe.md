Scenic View
===========

Scenic View is a JavaFX application designed to make it simple to understand the current state of your application scenegraph, and to also easily manipulate properties of the scenegraph without having to keep editing your code. 
This lets you find bugs, and get things pixel perfect without having to do the compile-check-compile dance.

[![Scenic View Screenshot](http://fxexperience.com/wp-content/uploads/2014/08/scenicView1.png)]()

## Build status

Builds for JDK 11 for Windows, Linux, and MacOS are built by Azure Pipelines. The status of these builds is shown below:

| Platform | Status | Download for JDK 11 |
|----------|--------|---------------------|
| Windows  | [![Build Status](https://jonathangiles.visualstudio.com/Scenic%20View/_apis/build/status/Scenic%20View%20-%20JDK%2011%20-%20Windows)](https://jonathangiles.visualstudio.com/Scenic%20View/_build/latest?definitionId=5) | [Download](https://jogiles.blob.core.windows.net/scenic-view/scenicview-win.zip) 
| MacOS  | [![Build Status](https://jonathangiles.visualstudio.com/Scenic%20View/_apis/build/status/Scenic%20View%20-%20JDK%2011%20-%20macOS)](https://jonathangiles.visualstudio.com/Scenic%20View/_build/latest?definitionId=7) | [Download](https://jogiles.blob.core.windows.net/scenic-view/scenicview-mac.zip) |
| Linux  | [![Build Status](https://jonathangiles.visualstudio.com/Scenic%20View/_apis/build/status/Scenic%20View%20-%20JDK%2011%20-%20Linux)](https://jonathangiles.visualstudio.com/Scenic%20View/_build/latest?definitionId=6) | [Download](https://jogiles.blob.core.windows.net/scenic-view/scenicview-linux.zip) |

You can also download platform-independent releases [JDK 8](http://fxexperience.com/downloads/scenic-view-8-7-0/) and [JDK 9](http://fxexperience.com/downloads/scenic-view-9-0-0/).

## Java Version

Scenic View has releases for JDK 8, JDK 9, and JDK 11:

- The JDK 8 release is in maintenance mode. No active development is ongoing, and the code exists in the jdk8 branch.
- The JDK 9 release is deprecated, and developers are encouraged to use either the JDK 8 release or the JDK 11 release.
- The JDK 11 release is the actively developed branch, and the code exists in the master branch.

For more information about JavaFX 11, see https://openjfx.io/openjfx-docs/.

## Scenic View for JDK 11+

### How to build

Install a valid Java 11 version, and set `JAVA_HOME` accordingly.

The project is managed by gradle, so is not necessary to download the JavaFX 11 SDK. 

To build the project, type:

	./gradlew build

To build a custom runtime image for your platform, type:

	./gradlew jlink

You can also create a zipped version of that image for distribution:

	./gradlew jlinkZip

### Using Scenic View

#### Stand-alone application

Download the Scenic View custom image for your platform from the above links. Unzip and then run: 

	cd scenicview/bin
	./scenicView

Also, you can clone or download this project, and run Scenic View as stand-alone application:

	./gradlew run

or if you build a custom image:

	cd build/scenicview/bin
	./scenicView

Then run a JavaFX application and it will be detected by Scenic View.

Alternatively, you can also run the `scenicview.jar` in any platform, providing that JDK 11 and JavaFX SDK 11 are installed:

	cd build/libs/
	java --module-path /path-to/javafx-11-sdk/lib --add-modules javafx.web,javafx.fxml,javafx.swing -jar scenicview.jar

##### Notes

- Scenic View will detect JavaFX applications running on Java 9, 10 or 11. 

- If the JavaFX application runs from a custom image (created via `link` or `jpackage`), it won't
have access to some required tools that are available when it runs from a regular JDK, and Scenic View won't be
able to find it.

#### As a dependency

You can add `scenicview.jar` as a dependency to your JavaFX application. Since this jar doesn't include 
the JavaFX dependencies, you should add them to your project, in case these weren't included yet.

For instance, if you are running a gradle project, add the jar to a `libs` folder, then add it to the `build.gradle` file, like:

        plugins {
            id 'application'
            id 'org.openjfx.javafxplugin' version '0.0.5'
        }

        repositories {
            mavenCentral()
        }

        dependencies {
            compile files('libs/scenicview.jar')
        }

        javafx {
            modules = ['javafx.web', 'javafx.fxml', 'javafx.swing']
        }

and also add it to the `module-info.java` file requirements:

        requires javafx.controls;
        requires javafx.fxml;
        requires transitive javafx.web;
        requires transitive javafx.swing;

        requires org.scenicview.scenicview;

Finally, you can run it from the application class:

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
        
        ScenicView.show(scene);

## License

GNU General Public License v3.0-or-later

## Contributing

This project welcomes all types of contributions and suggestions. 
We encourage you to report issues, create suggestions and submit pull requests.

Please go through the [list of issues](https://github.com/JonathanGiles/scenic-view/issues) 
to make sure that you are not duplicating an issue.
