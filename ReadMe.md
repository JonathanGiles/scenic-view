Scenic View
===========

Scenic View is a JavaFX application designed to make it simple to understand the current state of your application scenegraph, 
and to also easily manipulate properties of the scenegraph without having to keep editing your code. 
This lets you find bugs, and get things pixel perfect without having to do the compile-check-compile dance. 
You can [learn more about Scenic View on its website](http://www.scenic-view.org).

## Java Version

Scenic View runs on Java 11 and JavaFX 11. 

For more information about JavaFX 11, see https://openjfx.io/openjfx-docs/.

## How to build

Install a valid Java 11 version, and set `JAVA_HOME` accordingly.

The project is managed by gradle, so is not necessary to download the JavaFX 11 SDK. 

To build the project, type:

	./gradlew build

To build a custom runtime image for your platform, type:

	./gradlew jlink

You can also create a zipped version of that image for distribution:

	./gradlew jlinkZip

## Use of ScenicView

### Stand-alone application

You can run ScenicView as stand-alone application from this project:

	./gradlew run

or if you have the custom image:

	cd build/image/bin
	./scenicView

Then run a JavaFX 11 application and it will be detected by ScenicView.

#### From the jar

You can also run the `scenicview.jar` in any platform, providing a JDK 11 and JavaFX SDK 11 are installed:

	java --module-path /path-to/javafx-11-sdk/lib --add-modules javafx.web,javafx.fxml,javafx.swing -jar scenicview.jar

### As a dependency

You can add `scenicview.jar` as a dependency to your JavaFX 11 application. Since this jar doesn't include 
the JavaFX dependencies, you should add them to your project, in case these weren't included yet.

For instance, if you are running a gradle project, add the jar to a `libs` folder, then add it to the build.gradle file:

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
