import android.Keys._

android.Plugin.androidBuild

name := "ARPiBot-Android"

version := "0.1"

scalaVersion := "2.10.3"

proguardCache in Android ++= Seq(
  ProguardCache("org.scaloid") % "org.scaloid"
)

proguardOptions in Android ++= Seq(
	"-dontobfuscate",
	"-dontoptimize"
)

libraryDependencies ++= Seq(
	"org.scaloid" %% "scaloid" % "3.1-8-RC1"
)

scalacOptions in Compile += "-feature"

run <<= run in Android

install <<= install in Android
