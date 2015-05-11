#! /bin/sh
awk '/BONDS/,/ANGLES/' par_all36_na.prm |\
    grep -v '^!' |\
    grep -v ANGLES |\
    grep -v BONDS |\
    awk -vLEFT=$1 -vRIGHT=$2 '
        BEGIN { min = 100 }
        {
            if ((substr($1, 1, 1) == LEFT && substr($2, 1, 1) == RIGHT) || (substr($1, 1, 1) == RIGHT && substr($2, 1, 1) == LEFT)) {
                if ($4 < min) min = $4
                if ($4 > max) max = $4
                total += $4
                i += 1
            }
        }
        END { if (min == 100) { print LEFT " " RIGHT " no data" } else { print LEFT " " RIGHT " " min " " max " " (total/i) } }
    '
