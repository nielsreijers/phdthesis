set term postscript eps enhanced color font "Helvetica,20"
set datafile separator ","

#set offsets 0.5, 0.5, 0, 0
#set grid ytics lc rgb "#bbbbbb" lw 1 lt 1
#set key spacing 1
#set bmargin at screen 0.08
#set xtics offset 0,0.5 font ",18"
#set ytics offset -0.6,0


set xlabel "Percentage of executed array/object access instructions"
set ylabel "Overhead (% unsafe VM)"
set xrange [0:20]
set yrange [0:100]

set output "../safety-ld-st-percentage-vs-overhead.eps"
plot "safety-ld-st-percentage-vs-overhead.dat" using 3:4 title 'Loads vs. read safety overhead'      with points pointtype 31 , \
     "safety-ld-st-percentage-vs-overhead.dat" using 1:2 title 'Stores vs. write safety overhead'    with points pointtype 31 lc rgb "red"


set autoscale x
set autoscale y
set logscale x
set logscale y
set xlabel "Number of calls"
set ylabel "Duration in cycles"
set key off
set term postscript eps enhanced color font "Helvetica,15"

set output "../method-calls-vs-duration.eps"
#plot "method-calls-vs-duration.dat" using ($1):($2):3 with labels offset 0,1 , "method-calls-vs-duration.dat" using 1:2 with points pt 7 ps 2
plot "method-calls-vs-duration.dat" using 3:4:5 with labels offset 0,0.5, "method-calls-vs-duration.dat" using 1:2 with points pt 7 ps 2, "method-calls-vs-duration.dat" using 1:2:($3-$1):($4-$2) with vectors lt -1 lw 0.5 nohead
