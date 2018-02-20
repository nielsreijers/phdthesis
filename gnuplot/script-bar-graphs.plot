set term postscript eps enhanced color font "Helvetica,22"
set datafile separator ","

set style data histogram
set style histogram cluster gap 2
set style fill solid border -1
set xtics rotate by 45 right
set ylabel "Overhead (% of native C run time)"

set yrange [0:500]
set output "../safety-cost.eps"
plot 'safety-cost.dat' using 2:xtic(1) ti col linecolor "#e7a529", '' u 3 ti col linecolor "#039f74", '' u 4 ti col linecolor "#56b5e8"

set yrange [-70:350]
set output "../safety-cost-diff-using-regs.eps"
plot 'safety-cost-diff-using-regs.dat' using 2:xtic(1) ti col linecolor "#6666ee", '' u 3 ti col linecolor "#55bb55", '' u 4 ti col linecolor "#cc00000"


set yrange [0:250]
set output "../8_16_32_bit.eps"
plot '8_16_32_bit.dat' using 2:xtic(1) ti col linecolor "#e7a529", '' u 3 ti col linecolor "#039f74", '' u 4 ti col linecolor "#56b5e8"


