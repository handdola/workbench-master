enablePlugins(ScalaJSPlugin)

// dynamic page reloading
enablePlugins(WorkbenchPlugin)

// (experimental feature) in-place code update with state preservation
// enablePlugins(WorkbenchSplicePlugin) // disable WorkbenchPlugin when activating

name := "Example"

scalaVersion := "2.11.8"

//<<-----------move to nginx.conf -------------------
localUrl := ("localpc.com", 12345)

domainName := "story.miridas.com:12345"
//domainName := "localpc.com:12345"

//previewDir := "Z:/StoryPreview/"
previewDir := "D:/ScalaIDE/StoryPreview"
//-----------move to nginx.conf ------------------->>

version := "0.1-SNAPSHOT"

libraryDependencies ++= Seq(
  "org.scala-js" %%% "scalajs-dom" % "0.9.1",
  "com.lihaoyi" %%% "scalatags" % "0.6.3",
  "com.lihaoyi" %%% "upickle" % "0.4.3"
)
