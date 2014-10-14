import sbt._
import Keys._
import play.Project._

object Resolvers {
  lazy val sprayIoReleases       = "Spray IO Release Repo" at "http://repo.spray.io"
  lazy val typesafeResolvers     = Seq(sprayIoReleases) ++ Seq("snapshots", "releases").map(Resolver.typesafeRepo) ++Seq("snapshots", "releases").map(Resolver.sonatypeRepo)

  lazy val sunRepo               = "Sun Maven2 Repo"               at "http://download.java.net/maven/2"
  lazy val glassfishRepo         = "Sun GF Maven2 Repo"            at "http://download.java.net/maven/glassfish"
  lazy val oracleRepo            = "Oracle Maven2 Repo"            at "http://download.oracle.com/maven"
  lazy val oracleResolvers       = Seq(sunRepo, oracleRepo)

  lazy val springRelease         = "EBR Spring Release Repository" at "http://repository.springsource.com/maven/bundles/release"
  lazy val springExternalRelease = "EBR Spring External Release"   at "http://repository.springsource.com/maven/bundles/external"
  lazy val springMilestoneRepo   = "Spring Milestone Repository"   at "https://repo.springsource.org/libs-milestone"
  lazy val springAppResolvers    = Seq(springRelease, springExternalRelease, springMilestoneRepo)

  lazy val jBossRepo             = "JBoss Public Maven Repo"       at "https://repository.jboss.org/nexus/content/groups/public-jboss/"

  lazy val allResolvers          = typesafeResolvers ++ springAppResolvers ++ oracleResolvers ++ Seq(jBossRepo)
}

object Versions {
  lazy val scalaVer  = "2.10.0"
  lazy val akkaVer   = "2.3.4"
  lazy val springVer = "3.2.2.RELEASE"
  lazy val playVer   = play.core.PlayVersion.current

}

// Add your project dependencies here,
object Dependencies {
  import Versions._
  
  lazy val provided = "provided"
  lazy val test     = "test"
  lazy val runtime  = "runtime"

  lazy val junit                = "junit"            %  "junit"           % "4.10"   % test
  lazy val mockito              = "org.mockito"      %  "mockito-core"    % "1.9.5"  % test
  lazy val scalaCheck           = "org.scalacheck"   %% "scalacheck"      % "1.10.1" % test
  lazy val scalaTest            = "org.scalatest"    %% "scalatest"       % "1.9.1"  % test
  lazy val testDependencies     = Seq(junit, scalaTest, scalaCheck, mockito)

  lazy val akkaOrg              = "com.typesafe.akka"
  lazy val akkaPersistArt       = "akka-persistence-experimental"

  lazy val akkaActor            = akkaOrg            %% "akka-actor"      % akkaVer
  lazy val akkaTestkit          = akkaOrg            %% "akka-testkit"    % akkaVer  % test
  lazy val akkaRemote           = akkaOrg            %% "akka-remote"     % akkaVer
  lazy val akkaSlf4j            = akkaOrg            %% "akka-slf4j"      % akkaVer
  lazy val akkaCamel            = akkaOrg            %% "akka-camel"      % akkaVer
  lazy val akkaPersistence      = akkaOrg            %% akkaPersistArt    % akkaVer
  
  lazy val akkaDependencies     = Seq(akkaActor, akkaTestkit, akkaRemote, akkaSlf4j, akkaCamel)

  lazy val springOrg            = "org.springframework"
  lazy val springScalaOrg       = springOrg + ".scala"
  lazy val javaxInject          = "javax.inject"      % "javax.inject"                    % "1"
  lazy val springAsm            = springOrg           % "org.springframework.asm"         % springVer
  lazy val springAop            = springOrg           % "org.springframework.aop"         % springVer
  lazy val springBeans          = springOrg           % "org.springframework.beans"       % springVer
  lazy val springCore           = springOrg           % "org.springframework.core"        % springVer
  lazy val springContext        = springOrg           % "org.springframework.context"     % springVer
  lazy val springExpression     = springOrg           % "org.springframework.expression"  % springVer
  lazy val springTxn            = springOrg           % "org.springframework.transaction" % springVer
  lazy val springOrm            = springOrg           % "org.springframework.orm"         % springVer
  lazy val springScala          = springScalaOrg      % "spring-scala"                    % "1.0.0.M2"
  
  lazy val springDependencies   = Seq(javaxInject, springBeans, springCore, springContext, springScala)

  lazy val rxScala        = "com.netflix.rxjava"     %  "rxjava-scala"   % "0.14.6"
  lazy val scalaAsync     = "org.scala-lang.modules" %% "scala-async"    % "0.9.0-M2"
  lazy val barcode4j      = "net.sf.barcode4j"       %  "barcode4j"      % "2.0"
  lazy val squeryl        = "org.squeryl"            %% "squeryl"        % "0.9.5-6"
  lazy val utilDependencies = Seq(rxScala, scalaAsync, barcode4j, squeryl)

  lazy val basicDependencies    = testDependencies ++ utilDependencies ++ springDependencies
}

object ApplicationBuild extends Build {
  import Resolvers._
  import Dependencies._
  import Versions._
  
  lazy val appName         = "play-crud"
  lazy val appVersion      = "1.0-SNAPSHOT"

  lazy val appDependencies = Seq(jdbc, anorm) ++ basicDependencies


  lazy val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here
    scalaVersion := scalaVer,
    resolvers ++= allResolvers
  )

}
