import java.nio.file.Files
import java.nio.file.Path

class ManifestResolver {
    static File resolveSheet(def projectDir, def launchDir, String manifestFileName) {
        if (!manifestFileName?.trim()) {
            throw new IllegalArgumentException('manifestFileName must be a filename or an absolute path')
        }

        Path projectPath = new File(projectDir.toString()).toPath().toAbsolutePath().normalize()
        Path testsPath = projectPath.resolve('tests')
        Path manifestPath = new File(manifestFileName).toPath()
        if (!manifestPath.isAbsolute() && manifestPath.nameCount != 1) {
            throw new IllegalArgumentException(
                "manifestFileName must be a filename or an absolute path: ${manifestFileName}"
            )
        }

        File manifest = manifestPath.isAbsolute()
            ? manifestPath.toFile()
            : testsPath.resolve('manifests').resolve(manifestPath).toFile()
        if (!manifest.isFile()) {
            throw new FileNotFoundException("Manifest file does not exist: ${manifest}")
        }
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
            ? new File(launchDir.toString()).toPath().toAbsolutePath().normalize()
            : projectPath.resolve('.nf-test').resolve('tests').toAbsolutePath().normalize()
        Path resolverDir = resolverBase.resolve('manifest-resolver').normalize()
        if (!resolverDir.startsWith(resolverBase)) {
            throw new IllegalArgumentException(
                "Resolver directory must be inside the test launch directory: ${resolverDir}"
            )
        }

        Path resolvedSheetPath = resolverDir
            .resolve(manifest.name.replace(".csv", ".resolved.csv"))
            .normalize()
        if (!resolvedSheetPath.startsWith(resolverDir.normalize())) {
            throw new IllegalArgumentException(
                "Resolved samplesheet must be written inside the resolver directory: ${resolvedSheetPath}"
            )
        }

        Files.createDirectories(resolverDir)
        File resolvedSheet = resolvedSheetPath.toFile()
        resolvedSheet.text = resolved

        return resolvedSheet
    }
}
