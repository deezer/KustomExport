package deezer.kustom

/**
 * As of today (2021/20/18) the KSP + KotlinJsIr compiler has some issues with JS sourceSets.
 * https://docs.google.com/spreadsheets/d/13lXyEHu1GzwgicWvTqnJf_qCcxOY2vwM04gSfx_f1fk/edit#gid=0
 * As a workaround, we're declaring a new annotation in each module and pass the annotation full name in args to ksp.
 * When KSP/Kotlin is more stable with source sets, this could be used to define once and for all the annotation.
 */

enum class ExportMode {
    ONLY_IMPORT, ONLY_EXPORT, IMPORT_EXPORT
}

//@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
public annotation class KustomExport(
    public val mode: ExportMode = ExportMode.IMPORT_EXPORT
)
