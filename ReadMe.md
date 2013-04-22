Scenic View
===========



Building
--------

This project depends on the fxconnector project.  Either build that project
first (via the `install` task) or use an existing build from jcenter.

Releasing
---------

To upload release quality builds to bintray, first you must place a real
`bintray_username` and `bintray_api_key` in your ~/.gradle/gradle.properties`
file.  The values in the local gradle.properties exist only to keep the build
running when not deploying.  Next, call `./gradlew uploadArchives`.

Finally, you will need to go onto the Bintray website for the
(scenic-view)[https://bintray.com/repo/browse/scenic-view/scenic-view] repo
and either release or discard the files.

The single jar suitable for the non-repo release is located under
`build/libs/scenic-view-<version>.jar` which for 8.0-dp3 would be
`build/libs/scenic-view-8.0-dp3.jar`.  The exact name isn't required to stay
the same for non-repo releases, and can easily be canged to someting shorter
like `scenic-view.jar`.

To build application bundles first do a bintray release.  This is done to
insure the platform bundles all use the same library jar bits.  The next step
is to change into the `app` directory, and run `gradle clean assemble`.  The
resulting distribution packages will be in `build/distributions/bundles`.




