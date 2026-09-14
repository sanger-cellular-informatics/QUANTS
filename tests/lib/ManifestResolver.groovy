import java.nio.file.Files
import java.nio.file.Path

class ManifestResolver {
    static File resolveSheet(def projectDir, def launchDir, String manifestFileName) {
        Path projectPath = new File(projectDir.toString()).toPath().toAbsolutePath().normalize()
        Path testsPath = projectPath.resolve('tests')
        File manifest = testsPath.resolve('manifests').resolve(manifestFileName).toFile()
        String raw = manifest.text

        // Nextflow resolves paths in a samplesheet relative to the staged
        // samplesheet. Make repository-relative test data paths absolute
        // before passing the temporary samplesheet to the pipeline.
        String resolved = raw.replace(
            'tests/',
            "${testsPath}/"
        )

        // Keep the temporary sheet in this test's nf-test launch directory so
        // it remains available to the pipeline's container runtime until the
        // test cleanup block runs.
        // nf-test does not populate launchDir while listing tests in dry-run
        // mode. Use the run-level directory only for that parse-time fallback;
        // executed tests always use their own launchDir.
        Path resolverBase = launchDir
            ? new File(launchDir.toString()).toPath()
            : projectPath.resolve('.nf-test').resolve('tests')
        Path resolverDir = resolverBase.resolve('manifest-resolver')
        Files.createDirectories(resolverDir)

        File resolvedSheet = resolverDir.resolve(manifestFileName.replace(".csv", ".resolved.csv")).toFile()
        resolvedSheet.text = resolved

        return resolvedSheet
    }
}
