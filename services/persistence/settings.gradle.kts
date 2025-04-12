rootProject.name = "persistence"
include("shared-jpa")
include("shared-auth")
project(":shared-jpa").projectDir = file("../shared/shared-jpa")
project(":shared-auth").projectDir = file("../shared/shared-auth")