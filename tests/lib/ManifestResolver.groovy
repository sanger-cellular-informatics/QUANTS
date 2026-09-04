import java.nio.file.Files
import java.nio.file.Path

class ManifestResolver {
    static File resolveSheet(def projectDir, String manifestFileName) {
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

        // Keep the temporary sheet below the project directory. Singularity
        // may start the pipeline process after this helper returns, so a
        // system temporary directory registered with deleteOnExit() can be
        // gone before it is mounted into the container.
        Path resolverDir = projectPath.resolve('.nf-test').resolve('manifest-resolver')
        Files.createDirectories(resolverDir)
        File tmpDir = Files.createTempDirectory(resolverDir, 'nf-test-').toFile()

        File resolvedSheet = new File(tmpDir, manifestFileName.replace(".csv", ".resolved.csv"))
        resolvedSheet.text = resolved

        return resolvedSheet
    }
}
