ideaExcludeFolders ++= Seq(".idea", ".idea_modules")

EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource

EclipseKeys.withSource := true

ScctPlugin.instrumentSettings

