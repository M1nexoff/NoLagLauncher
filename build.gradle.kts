// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    id("com.google.devtools.ksp") version "2.3.4" apply false
    id("com.google.dagger.hilt.android") version "2.60" apply false

}

tasks.register("Printart"){
    doFirst {
        println("""
         _   _         _                   _                            _               
        | \ | |       | |                 | |                          | |              
        |  \| | ___   | |     __ _  __ _  | |     __ _ _   _ _ __   ___| |__   ___ _ __ 
        | . ` |/ _ \  | |    / _` |/ _` | | |    / _` | | | | '_ \ / __| '_ \ / _ \ '__|
        | |\  | (_) | | |___| (_| | (_| | | |___| (_| | |_| | | | | (__| | | |  __/ |   
        \_| \_/\___/  \_____/\__,_|\__, | \_____/\__,_|\__,_|_| |_|\___|_| |_|\___|_|   
                                    __/ |                                               
                                   |___/                                                
""".trimIndent())
    }
}