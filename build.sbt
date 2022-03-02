/*
 * Copyright (c) 2020 Jobial OÜ. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance with
 * the License. A copy of the License is located at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
name := "condense"

ThisBuild / organization := "io.jobial"
ThisBuild / scalaVersion := "2.12.13"
ThisBuild / crossScalaVersions := Seq("2.11.12", "2.12.13", "2.13.6")
ThisBuild / version := "0.4.0"
ThisBuild / scalacOptions += "-target:jvm-1.8"
ThisBuild / publishArtifact in(Test, packageBin) := true
ThisBuild / publishArtifact in(Test, packageSrc) := true
ThisBuild / publishArtifact in(Test, packageDoc) := true

import sbt.Defaults.sbtPluginExtra
import sbt.Keys.{description, libraryDependencies, publishConfiguration}
import sbt.addCompilerPlugin
import xerial.sbt.Sonatype._

lazy val commonSettings = Seq(
  publishConfiguration := publishConfiguration.value.withOverwrite(true),
  publishLocalConfiguration := publishLocalConfiguration.value.withOverwrite(true),
  publishTo := publishTo.value.orElse(sonatypePublishToBundle.value),
  sonatypeProjectHosting := Some(GitHubHosting("jobial-io", "condense", "orbang@jobial.io")),
  organizationName := "Jobial OÜ",
  licenses := Seq("APL2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt")),
  description := "Run functional Scala code as a portable serverless function or microservice",
  addCompilerPlugin("org.typelevel" % "kind-projector" % "0.13.2" cross CrossVersion.full),
  addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),
  scalacOptions ++= (if (scalaBinaryVersion.value != "2.13") Seq("-Ypartial-unification") else Seq())
)

lazy val CloudformationTemplateGeneratorVersion = "3.10.4"
lazy val SclapVersion = "1.1.7"
lazy val ScaseVersion = "0.4.0"

lazy val root: Project = project
  .in(file("."))
  .settings(commonSettings)
  .settings(
    publishArtifact := false,
    makePom / publishArtifact := true
  )
  .aggregate(`sbt-condense`, `condense-core`)
  .dependsOn(`sbt-condense`, `condense-core`)

lazy val `condense-core` = project
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "io.jobial" %% "cloud-formation-template-generator" % CloudformationTemplateGeneratorVersion,
      "io.jobial" %% "sclap" % SclapVersion,
      "io.jobial" %% "scase-core" % ScaseVersion % "compile->compile;test->test",
      "io.jobial" %% "scase-aws" % ScaseVersion % "compile->compile;test->test"
    )
  )


// check https://stackoverflow.com/questions/37525980/sbt-exclude-module-from-aggregates-or-compilation-based-on-scala-version
lazy val `sbt-condense` = project
  .settings(commonSettings)
  //.enablePlugins(SbtPlugin)
  .settings(
    name := "sbt-condense",
    publish := scalaBinaryVersion.value == "2.12",
    //      unmanagedSources / excludeFilter := AllPassFilter,
    //      managedSources / excludeFilter := AllPassFilter,
    Compile / unmanagedSourceDirectories := (if (scalaBinaryVersion.value == "2.12") Seq(baseDirectory.value / "src" / "main" / "scala") else Nil),
    //      Compile / managedSourceDirectories := Nil,
    publishMavenStyle := scalaBinaryVersion.value == "2.12",
    sbtPlugin := scalaBinaryVersion.value == "2.12",
    pluginCrossBuild / sbtVersion := "1.2.8", // set minimum sbt version
    libraryDependencies ++= {
      val sbtV = (sbtBinaryVersion in pluginCrossBuild).value
      val scalaV = (scalaBinaryVersion in update).value

      if (scalaBinaryVersion.value == "2.12")
        Seq(sbtPluginExtra("com.github.sbt" % "sbt-proguard" % "0.5.0", sbtV, scalaV))
      else
        Seq()
    }
  )


