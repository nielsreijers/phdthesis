set term postscript eps enhanced color font "Helvetica,20"
set datafile separator ","

set grid ytics lc rgb "#bbbbbb" lw 1 lt 1
set size 1.25, 1
set key outside




set ylabel "Overhead (% of native C run time)"
set xtics ("simple \n peeph." 0, "impr. \n peeph." 1, "stack \n caching" 2, "pop.val. \n caching" 3, "mark \n loops" 4, "const \n shift" 5, "16-bit \n index" 6, "SIMUL" 7)
set xtics rotate by 45 right

set key width -2.8
set output "../performance-per-benchmark.eps"
plot "performance-per-benchmark.dat" using 2 title 'Bubble sort'   with linespoints dashtype 1 lw 3 ps 1.5, \
     "performance-per-benchmark.dat" using 3 title 'Heap sort'     with linespoints dashtype 2 lw 3 ps 1.5, \
     "performance-per-benchmark.dat" using 4 title 'Binary search' with linespoints dashtype 3 lw 3 ps 1.5, \
     "performance-per-benchmark.dat" using 5 title 'XXTEA'         with linespoints dashtype 4 lw 3 ps 1.5, \
     "performance-per-benchmark.dat" using 6 title 'MD5'           with linespoints dashtype 5 lw 3 ps 1.5, \
     "performance-per-benchmark.dat" using 7 title 'RC5'           with linespoints dashtype 6 lw 3 ps 1.5, \
     "performance-per-benchmark.dat" using 8 title 'FFT'           with linespoints dashtype 7 lw 3 ps 1.5, \
     "performance-per-benchmark.dat" using 9 title 'Outlier'       with linespoints dashtype 8 lw 3 ps 1.5, \
     "performance-per-benchmark.dat" using 10 title 'LEC'           with linespoints dashtype 9 lw 3 ps 1.5, \
     "performance-per-benchmark.dat" using 11 title 'CoreMark'      with linespoints dashtype 10 lw 3 ps 1.5, \
     "performance-per-benchmark.dat" using 12 title 'MoteTrack'     with linespoints dashtype 11 lw 3 ps 1.5, \
     "performance-per-benchmark.dat" using 13 title 'HeatCalib'     with linespoints dashtype 12 lw 3 ps 1.5, \
     "performance-per-benchmark.dat" using 14 title 'HeatDetect'    with linespoints dashtype 13 lw 3 ps 1.5

set key width 0
set output "../performance-per-opcode-category.eps"
plot "performance-per-opcode-category.dat" using 2 title 'total'      with linespoints dashtype 1 lw 3 ps 1.5, \
     "performance-per-opcode-category.dat" using 3 title 'push/pop'   with linespoints dashtype 2 lw 3 ps 1.5, \
     "performance-per-opcode-category.dat" using 4 title 'load/store' with linespoints dashtype 3 lw 3 ps 1.5, \
     "performance-per-opcode-category.dat" using 5 title 'mov(w)'     with linespoints dashtype 4 lw 3 ps 1.5, \
     "performance-per-opcode-category.dat" using 6 title 'vm+other'   with linespoints dashtype 5 lw 3 ps 1.5




set ylabel "Overhead (% of native C size)"
set xtics ("simple \n peeph." 0, "impr. \n peeph." 1, "stack \n caching" 2, "pop.val. \n caching" 3, "mark \n loops" 4, "const \n shift" 5, "16-bit \n index" 6, "SIMUL" 7)

set key width -2.8
set output "../codesizeoverhead-per-benchmark.eps"
plot "codesizeoverhead-per-benchmark.dat" using 2 title 'Bubble sort'   with linespoints dashtype 1 lw 3 ps 1.5, \
     "codesizeoverhead-per-benchmark.dat" using 3 title 'Heap sort'     with linespoints dashtype 2 lw 3 ps 1.5, \
     "codesizeoverhead-per-benchmark.dat" using 4 title 'Binary search' with linespoints dashtype 3 lw 3 ps 1.5, \
     "codesizeoverhead-per-benchmark.dat" using 5 title 'XXTEA'         with linespoints dashtype 4 lw 3 ps 1.5, \
     "codesizeoverhead-per-benchmark.dat" using 6 title 'MD5'           with linespoints dashtype 5 lw 3 ps 1.5, \
     "codesizeoverhead-per-benchmark.dat" using 7 title 'RC5'           with linespoints dashtype 6 lw 3 ps 1.5, \
     "codesizeoverhead-per-benchmark.dat" using 8 title 'FFT'           with linespoints dashtype 7 lw 3 ps 1.5, \
     "codesizeoverhead-per-benchmark.dat" using 9 title 'Outlier'       with linespoints dashtype 8 lw 3 ps 1.5, \
     "codesizeoverhead-per-benchmark.dat" using 10 title 'LEC'           with linespoints dashtype 9 lw 3 ps 1.5, \
     "codesizeoverhead-per-benchmark.dat" using 11 title 'CoreMark'      with linespoints dashtype 10 lw 3 ps 1.5, \
     "codesizeoverhead-per-benchmark.dat" using 12 title 'MoteTrack'     with linespoints dashtype 11 lw 3 ps 1.5, \
     "codesizeoverhead-per-benchmark.dat" using 13 title 'HeatCalib'     with linespoints dashtype 12 lw 3 ps 1.5, \
     "codesizeoverhead-per-benchmark.dat" using 14 title 'HeatDetect'    with linespoints dashtype 13 lw 3 ps 1.5

set key width 0
set output "../codesizeoverhead-per-opcode-category.eps"
plot "codesizeoverhead-per-opcode-category.dat" using 2 title 'total'      with linespoints dashtype 1 lw 3 ps 1.5, \
     "codesizeoverhead-per-opcode-category.dat" using 3 title 'push/pop'   with linespoints dashtype 2 lw 3 ps 1.5, \
     "codesizeoverhead-per-opcode-category.dat" using 4 title 'load/store' with linespoints dashtype 3 lw 3 ps 1.5, \
     "codesizeoverhead-per-opcode-category.dat" using 5 title 'mov(w)'     with linespoints dashtype 4 lw 3 ps 1.5, \
     "codesizeoverhead-per-opcode-category.dat" using 6 title 'vm+other'   with linespoints dashtype 5 lw 3 ps 1.5




set ylabel "Overhead (% of native C run time)"
set xtics ("4" 0, "5" 1, "6" 2, "7" 3, "8" 4, "9" 5, "10" 6, "11" 7)
set xtics rotate by 0 right

set output "../cachesize-performance-per-benchmark.eps"

set key width -2.8
plot "cachesize-performance-per-benchmark.dat" using 2 title 'Bubble sort'   with linespoints dashtype 1 lw 3 ps 1.5, \
     "cachesize-performance-per-benchmark.dat" using 3 title 'Heap sort'     with linespoints dashtype 2 lw 3 ps 1.5, \
     "cachesize-performance-per-benchmark.dat" using 4 title 'Binary search' with linespoints dashtype 3 lw 3 ps 1.5, \
     "cachesize-performance-per-benchmark.dat" using 5 title 'XXTEA'         with linespoints dashtype 4 lw 3 ps 1.5, \
     "cachesize-performance-per-benchmark.dat" using 6 title 'MD5'           with linespoints dashtype 5 lw 3 ps 1.5, \
     "cachesize-performance-per-benchmark.dat" using 7 title 'RC5'           with linespoints dashtype 6 lw 3 ps 1.5, \
     "cachesize-performance-per-benchmark.dat" using 8 title 'FFT'           with linespoints dashtype 7 lw 3 ps 1.5, \
     "cachesize-performance-per-benchmark.dat" using 9 title 'Outlier'       with linespoints dashtype 8 lw 3 ps 1.5, \
     "cachesize-performance-per-benchmark.dat" using 10 title 'LEC'           with linespoints dashtype 9 lw 3 ps 1.5, \
     "cachesize-performance-per-benchmark.dat" using 11 title 'CoreMark'      with linespoints dashtype 10 lw 3 ps 1.5, \
     "cachesize-performance-per-benchmark.dat" using 12 title 'MoteTrack'     with linespoints dashtype 11 lw 3 ps 1.5, \
     "cachesize-performance-per-benchmark.dat" using 13 title 'HeatCalib'     with linespoints dashtype 12 lw 3 ps 1.5, \
     "cachesize-performance-per-benchmark.dat" using 14 title 'HeatDetect'    with linespoints dashtype 13 lw 3 ps 1.5

set key width 0
set output "../cachesize-performance-per-opcode-category.eps"
plot "cachesize-performance-per-opcode-category.dat" using 2 title 'total'      with linespoints dashtype 1 lw 3 ps 1.5, \
     "cachesize-performance-per-opcode-category.dat" using 3 title 'push/pop'   with linespoints dashtype 2 lw 3 ps 1.5, \
     "cachesize-performance-per-opcode-category.dat" using 4 title 'load/store' with linespoints dashtype 3 lw 3 ps 1.5, \
     "cachesize-performance-per-opcode-category.dat" using 5 title 'mov(w)'     with linespoints dashtype 4 lw 3 ps 1.5, \
     "cachesize-performance-per-opcode-category.dat" using 6 title 'vm+other'   with linespoints dashtype 5 lw 3 ps 1.5




set ylabel "Overhead (% of native C run time)"
set xtics ("1" 0, "2" 1, "3" 2, "4" 3, "5" 4, "6" 5, "7" 6)

set key width 0
set output "../pinnedregs-performance-xxtea.eps"
plot "pinnedregs-performance-xxtea.dat" using 2 title 'total'      with linespoints dashtype 1 lw 3 ps 1.5, \
     "pinnedregs-performance-xxtea.dat" using 3 title 'push/pop'   with linespoints dashtype 2 lw 3 ps 1.5, \
     "pinnedregs-performance-xxtea.dat" using 4 title 'load/store' with linespoints dashtype 3 lw 3 ps 1.5, \
     "pinnedregs-performance-xxtea.dat" using 5 title 'mov(w)'     with linespoints dashtype 4 lw 3 ps 1.5, \
     "pinnedregs-performance-xxtea.dat" using 6 title 'vm+other'   with linespoints dashtype 5 lw 3 ps 1.5
     

set key width -2.8
set output "../pinnedregs-performance-all-benchmarks.eps"
plot "pinnedregs-performance-all-benchmarks.dat" using 2 title 'Bubble sort'   with linespoints dashtype 1 lw 3 ps 1.5, \
     "pinnedregs-performance-all-benchmarks.dat" using 3 title 'Heap sort'     with linespoints dashtype 2 lw 3 ps 1.5, \
     "pinnedregs-performance-all-benchmarks.dat" using 4 title 'Binary search' with linespoints dashtype 3 lw 3 ps 1.5, \
     "pinnedregs-performance-all-benchmarks.dat" using 5 title 'XXTEA'         with linespoints dashtype 4 lw 3 ps 1.5, \
     "pinnedregs-performance-all-benchmarks.dat" using 6 title 'MD5'           with linespoints dashtype 5 lw 3 ps 1.5, \
     "pinnedregs-performance-all-benchmarks.dat" using 7 title 'RC5'           with linespoints dashtype 6 lw 3 ps 1.5, \
     "pinnedregs-performance-all-benchmarks.dat" using 8 title 'FFT'           with linespoints dashtype 7 lw 3 ps 1.5, \
     "pinnedregs-performance-all-benchmarks.dat" using 9 title 'Outlier'       with linespoints dashtype 8 lw 3 ps 1.5, \
     "pinnedregs-performance-all-benchmarks.dat" using 10 title 'LEC'           with linespoints dashtype 9 lw 3 ps 1.5, \
     "pinnedregs-performance-all-benchmarks.dat" using 11 title 'CoreMark'      with linespoints dashtype 10 lw 3 ps 1.5, \
     "pinnedregs-performance-all-benchmarks.dat" using 12 title 'MoteTrack'     with linespoints dashtype 11 lw 3 ps 1.5, \
     "pinnedregs-performance-all-benchmarks.dat" using 13 title 'HeatCalib'     with linespoints dashtype 12 lw 3 ps 1.5, \
     "pinnedregs-performance-all-benchmarks.dat" using 14 title 'HeatDetect'    with linespoints dashtype 13 lw 3 ps 1.5, \
     "pinnedregs-performance-all-benchmarks.dat" using 15 title 'average'       with linespoints dashtype 13 lw 6 ps 1.5
