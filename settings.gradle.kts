rootProject.name = "visi-sort"
val includer = file(".includer.gradle")
if (includer.exists()) {
    apply(from = includer)
}
