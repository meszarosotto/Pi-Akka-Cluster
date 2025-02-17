/**
  * Copyright © 2018 Lightbend, Inc
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *
  * NO COMMERCIAL SUPPORT OR ANY OTHER FORM OF SUPPORT IS OFFERED ON
  * THIS SOFTWARE BY LIGHTBEND, Inc.
  *
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */

//import com.lightbend.cinnamon.sbt.Cinnamon
//import com.lightbend.sbt.javaagent.JavaAgent.JavaAgentKeys
import sbt.Keys._
import sbt._
import sbtassembly._
import sbtstudent.AdditionalSettings
import AssemblyKeys.{assembly, assemblyMergeStrategy}
import com.typesafe.sbt.packager.archetypes.{JavaAppPackaging, JavaServerAppPackaging}
import com.typesafe.sbt.packager.docker.DockerChmodType.UserGroupWriteExecute
import com.typesafe.sbt.packager.docker.DockerPlugin.autoImport.{dockerAdditionalPermissions, dockerBaseImage, dockerChmodType, dockerCommands, dockerEnvVars, dockerExposedPorts, dockerRepository}
import com.typesafe.sbt.packager.docker.{Cmd, DockerChmodType, DockerPlugin}
import com.typesafe.sbt.packager.universal.UniversalPlugin, UniversalPlugin.autoImport._

object CommonSettings {
  lazy val commonSettings = Seq(
    organization := "com.lightbend.training",
    version := "1.3.0",
    scalaVersion := Version.scalaVer,
    scalacOptions ++= CompileOptions.compileOptions,
    unmanagedSourceDirectories in Compile := List((scalaSource in Compile).value, (javaSource in Compile).value),
    unmanagedSourceDirectories in Test := List((scalaSource in Test).value, (javaSource in Test).value),
    testOptions += Tests.Argument(TestFrameworks.JUnit, "-v"),
    logBuffered in Test := false,
    parallelExecution in Test := false,
    parallelExecution in GlobalScope := false,
    parallelExecution in ThisBuild := false,
    fork in Test := false,
    test in assembly := {},
    libraryDependencies ++= Dependencies.dependencies,
//    credentials += Credentials(Path.userHome / ".lightbend" / "commercial.credentials"),
//    resolvers += "com-mvn" at "https://repo.lightbend.com/commercial-releases/",
//    resolvers += Resolver.url("com-ivy", url("https://repo.lightbend.com/commercial-releases/"))(Resolver.ivyStylePatterns)
  ) ++
    AdditionalSettings.initialCmdsConsole ++
    AdditionalSettings.initialCmdsTestConsole ++
    AdditionalSettings.cmdAliases

  lazy val configure: Project => Project = (proj: Project) => {
    proj
    //.enablePlugins(Cinnamon)
    .settings(CommonSettings.commonSettings: _*)
//    .settings(
//      libraryDependencies += Cinnamon.library.cinnamonPrometheus,
//      libraryDependencies += Cinnamon.library.cinnamonPrometheusHttpServer,
//      libraryDependencies += Cinnamon.library.cinnamonAkkaHttp,
//      libraryDependencies += Cinnamon.library.cinnamonOpenTracingZipkin,
//      libraryDependencies += Cinnamon.library.cinnamonCHMetricsElasticsearchReporter,
//      AssemblyKeys.assembly := Def.task {
//        JavaAgentKeys.resolvedJavaAgents.value.filter(_.agent.name == "Cinnamon").foreach { agent =>
//          sbt.IO.copyFile(agent.artifact, target.value / "cinnamon-agent.jar")
//        }
//        AssemblyKeys.assembly.value
//      }.value,
//      assemblyMergeStrategy in assembly := {
//        case "cinnamon-reference.conf" => MergeStrategy.concat
//        case x =>
//          val oldStrategy = (assemblyMergeStrategy in assembly).value
//          oldStrategy(x)
//      }
//    )
      .enablePlugins(DockerPlugin, JavaAppPackaging)
      .settings(
      mappings in Universal += file("librpi_ws281x.so") -> "lib/librpi_ws281x.so",
      javaOptions in Universal ++= Seq("-Djava.library.path=lib",
        "-Dcluster-node-configuration.cluster-id=cluster-0"),
      //dockerBaseImage := "arm32v7/openjdk",
      dockerBaseImage := "arm32v7/adoptopenjdk",
      dockerCommands ++= Seq( Cmd("USER", "root"),
        Cmd("RUN", "mkdir -p","/dev/mem")  ),
      dockerChmodType := UserGroupWriteExecute,
      dockerRepository := Some("docker-registry-default.gsa2.lightbend.com/lightbend"),
      dockerExposedPorts := Seq(8080, 8558, 2550, 9001),
      dockerAdditionalPermissions ++= Seq((DockerChmodType.UserGroupPlusExecute, "/tmp")),
    )
  }
}
