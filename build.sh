sbt ++2.12.4 "^^1.0.0"   compile publishSigned
sbt ++2.10.6 "^^0.13.16" compile publishSigned
sbt sonatypeReleaseAll
