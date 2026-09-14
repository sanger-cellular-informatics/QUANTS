# QUANTS: Input

## Samplesheet

You will need to create a samplesheet with information about the samples you would like to analyse before running the pipeline.

Use the `input` parameter to specify its location.

```console
--input '[path to samplesheet file]'
```

The samplesheet has to be a **comma-separated** file. 
If working with FASTQ files, you will need a minimum of three columns with headers of `sample,fastq_1,fastq_2`. If working with CRAM files, you will need a minimum of two columns with headers of `sample,cram_path`. The samplesheet can also contain sample-specific parameters (see example below).

### Minimum samplesheet example

Example of a samplesheet with FASTQ files (note `fastq_2` column header required even if data is single-end):

```csv
sample,fastq_1,fastq_2
S01_D4_R1,S01_D4_R1.fastq.gz,
S02_D4_R2,S02_D4_R2.fastq.gz,
S03_D7_R1,S03_D7_R1.fastq.gz,
S04_D7_R2,S04_D7_R2.fastq.gz,
```

Example of a samplesheet with CRAM files:

```csv
sample,cram_path
S01_D4_R1,S01_D4_R1_merged.cram
S02_D4_R2,S02_D4_R2_merged.cram
S03_D7_R1,S03_D7_R1_merged.cram
S04_D7_R2,S04_D7_R2_merged.cram
```

### Recognised samplesheet fields

The samplesheet can contain any columns, but the columns with headers as below are recognised by QUANTS and used as described in the table below.

| Column | Description | Optional/Required |
| --- | --- | --- |
| `sample` | Custom sample name. Spaces in sample names are automatically converted to underscores (`_`). | Required |
| `fastq_1` | Full path to a FASTQ file. File has to be gzipped and have the extension ".fastq.gz" or ".fq.gz". | Required if `input_type` is set as `"fastq"` |
| `fastq_2` | Full path to a FASTQ file (for paired-end sequencing). File has to be gzipped and have the extension ".fastq.gz" or ".fq.gz". | Required if `input_type` is set as `"fastq"` and data is paired-end (column header is required for single-end data, but field can be left empty) |
| `cram_path` | Full path to a CRAM file. File has to have the extension ".cram". | Required if `input_type` is set as `"cram"` |
| `group_id` | ID to group samples together in the output directory | Optional |
| `oligo_library` | Path to an oligo library file | Required if either `quantification` or `infer_library_orientations` is enabled in global parameters |
| `append_start` | Sequence to append to the start of reads before alignment | If `read_modification` is enabled in global parameters and `infer_library_orientations` is set to `False`, at least `append_start` or `append_end` is required. If `append_start` is provided and `infer_library_orientations` is enabled, the provided value will be ignored (as `infer_library_orientations` will infer what the sequence to append should be). |
| `append_end` | Sequence to append to the end of reads before alignment | If `read_modification` is enabled in global parameters and `infer_library_orientations` is set to `False`, at least `append_start` or `append_end` is required. If `append_end` is provided and `infer_library_orientations` is enabled, the provided value will be ignored (as `infer_library_orientations` will infer what the sequence to append should be). |
| `read_transform` | Set this to `reverse`, `complement` or `reverse_complement` if transformation is required, else leave empty | Optional. If provided and `infer_library_orientations` is enabled, the provided value will be ignored (as `infer_library_orientations` will infer what, if any, transformation is required). |
| `adapter_path` | Path to a FASTA file containing adapter sequences to trim from reads | Required if `adapter_trimming` is set in global parameters |
| `expt_forward_primer` | Sequence of the forward primer used in the experiment. If `infer_library_orientations` is set to `False` this is the sequence trimmed from the start of reads. If `infer_library_orientations` is `True`, the actual sequence trimmed won't necessarily match this input. | Required if `infer_library_orientations` is set to `True`, or if `infer_library_orientations` is set to `False` but `primer_trimming` is set. |
| `expt_reverse_primer` | Sequence of the reverse primer used in the experiment. If `infer_library_orientations` is set to `False` this is the sequence trimmed from the end of reads. If `infer_library_orientations` is `True`, the actual sequence trimmed won't necessarily match this input. | Required if `infer_library_orientations` is set to `True`, or if `infer_library_orientations` is set to `False` but `primer_trimming` is set. |

As outlined above, samplesheet fields must be consistent with the global parameters. See [configuration](configuration.md#quants-configuration) for details of these.

### Deprecated samplesheet fields

With the release of QUANTS version 5.0.0.0, the following fields are no longer recognised in the samplesheet:

| Column         | Description                                       | Update |
|----------------|---------------------------------------------------|--------------------------------------------------|
| `primer_start` | Primer sequence to trim from the start of reads   | Now set via `expt_forward_primer` in samplesheet |
| `primer_end`   | Primer sequence to trim from the end of reads     | Now set via `expt_reverse_primer` in samplesheet |

### Example of samplesheet (compatible with QUANTS release 5.0.0.0)

Note the example below contains FASTQ files, but sample-specific parameters can be added to CRAM samplesheets in the same way.

```csv
sample,group_id,fastq_1,fastq_2,oligo_library,adapter_path,expt_forward_primer,expt_reverse_primer
S01_D4_R1,AAAA,S01_D4_R1_1.fastq.gz,S01_D4_R1_2.fastq.gz,/path/to/meta1.csv,path/to/adaptors.fa,GCTG,CTTGC
S02_D4_R2,AAAA,S02_D4_R2_1.fastq.gz,S02_D4_R2_2.fastq.gz,/path/to/meta1.csv,path/to/adaptors.fa,GCTG,CTTGC
S03_D7_R1,AAAA,S03_D7_R1_1.fastq.gz,S03_D7_R1_2.fastq.gz,/path/to/meta1.csv,path/to/adaptors.fa,GCTG,CTTGC
S04_D7_R2,AAAA,S04_D7_R2_1.fastq.gz,S04_D7_R2_2.fastq.gz,/path/to/meta1.csv,path/to/adaptors.fa,GCTG,CTTGC
S05_D4_R1,BBBB,S05_D4_R1_1.fastq.gz,S05_D4_R1_2.fastq.gz,/path/to/meta2.csv,path/to/adaptors.fa,AACG,ATACG
S06_D7_R1,BBBB,S06_D7_R1_1.fastq.gz,S06_D7_R1_2.fastq.gz,/path/to/meta2.csv,path/to/adaptors.fa,AACG,ATACG

```

## Other inputs

Other input files also needed to run QUANTS are:
- Sequencing files (FASTQ or CRAM) specified in the samplesheet.
- FASTA file with adapters, if `adapter_path` set in samplesheet.
- Oligo library file if `oligo_library` set in samplesheet. Note that information on the required library format for [pyQUEST](https://github.com/cancerit/pyQUEST) can be found [here](https://github.com/cancerit/pyQUEST#library) along with other usage details.
