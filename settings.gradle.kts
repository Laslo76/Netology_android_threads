pluginManagement {
    repositories {
        google()
        mavenCentral()
        mavenLocal()
        gradlePluginPortal()

        maven("https://dl.google.com/dl/android/maven2/")
        maven("https://mirrors.huaweicloud.com/repository/maven/")
        // Добавляем репозитории Huawei
        maven("https://developer.huawei.com/repo/") // Основной репозиторий с библиотеками
        maven("https://repo.huaweimobileservices.com/") // Важен для Gradle-плагинов
    }
}
dependencyResolutionManagement {
//    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
//    repositories {

//    }
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Здесь также можно указать репозитории для зависимостей проекта,
        // но обычно достаточно тех, что указаны выше
        maven("https://mirrors.huaweicloud.com/repository/maven/")
        maven("https://developer.huawei.com/repo/")
        maven("https://repo.huaweimobileservices.com/")
    }

}

rootProject.name = "NMedia"
include(":app")