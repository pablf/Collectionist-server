ThisBuild / scalaVersion     := "2.13.10"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

lazy val root = (project in file("."))
  .settings(
    name := "Collectionist2",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % "2.0.10",
      "dev.zio" %% "zio-test" % "2.0.10" % Test,
      "dev.zio" %% "zio-http" % "3.0.0-RC1",

      "com.typesafe.slick" %% "slick" % "3.5.0-M2",
      "com.typesafe.slick" %% "slick-hikaricp" % "3.4.1",
      "org.slf4j" % "slf4j-nop" % "2.0.6",
      "org.postgresql" % "postgresql" % "42.5.4",

      "org.mongodb.scala" % "mongo-scala-driver" % "2.2.2",

      "org.apache.spark" %% "spark-core" % "3.3.2",
      "org.apache.spark" %% "spark-mllib" % "3.3.2",
      "org.apache.spark" %% "spark-sql" % "3.3.2",
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")


  )
