process IRODS_IGET {
    tag "${meta.id}"
    label 'process_low'

    input:
        tuple val(meta), val(irods_paths)

    output:
        tuple val(meta), path(reads), emit: local_files
        tuple( 
            val("${task.process}"),
            val('irods_client'),
            eval("""iget -h 2>&1 | grep "Version" | awk '{print \\\$3}'"""),
            emit: versions_irods_client,
            topic: versions
        )

    when:
        task.ext.when == null || task.ext.when

    script:
        def args = task.ext.args ?: ''
        
        // part of the template, but currently unused:
        def prefix = task.ext.prefix ?: "${meta.id}"

        reads = irods_paths.collect { item -> item.tokenize('/').last() }

        def download_commands = irods_paths.withIndex().collect { irods_path, i ->
          def local_filename = reads[i]

          """
          echo "IRODS path: ${irods_path}"
          echo "Downloading as: ${local_filename}"

          iget ${args} -K -f -I -v "${irods_path}" "${local_filename}"
          """
        }.join('\n')

        """
        set -euo pipefail

        command -v iget >/dev/null 2>&1 || { echo "ERROR: iget not found (iRODS iCommands required)"; exit 1; }

        ${download_commands}
        """
    
    stub:
        reads = irods_paths.collect { item -> item.tokenize('/').last() }

        def stub_commands = reads.collect { local_filename ->
            """
            touch "${local_filename}"
            """
        }.join('\n')

        """
        set -euo pipefail

        ${stub_commands}
        """
}
