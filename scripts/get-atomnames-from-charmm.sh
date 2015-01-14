#! /bin/bash
cat top_all36_prot.rtf top_all36_na.rtf | awk '$1 == "ATOM" { printf("%s(AtomType.%s, \"%s\"),\n", $2, substr($3, 1, 1), $2) }' | sort -u
