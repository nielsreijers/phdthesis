public class OutlierDetection {
    public static void benchmark_main(short NUMNUMBERS,
                                 short[] buffer,
                                 short[] distance_matrix,
                                 short distance_threshold,
                                 boolean[] outliers) {
        // Calculate distance matrix
        short sub_start=0;
        for (short i=0; i<NUMNUMBERS; i++) {
            short hor = sub_start;
            short ver = sub_start;
            for (short j=i; j<NUMNUMBERS; j++) {
                short buffer_i = buffer[i];
                short buffer_j = buffer[j];
                if (buffer_i > buffer_j) {
                    short diff = (short)(buffer_i - buffer_j);
                    distance_matrix[hor] = diff;
                    distance_matrix[ver] = diff;
                } else {
                    short diff = (short)(buffer_j - buffer_i);
                    distance_matrix[hor] = diff;
                    distance_matrix[ver] = diff;
                }

                hor ++;
                ver += NUMNUMBERS;
            }
            sub_start+=NUMNUMBERS+1;
        }

        // Determine outliers
        // Since we scan one line at a time, we don't need to calculate
        // a matrix index. The first NUMNUMBERS distances correspond to
        // measurement 1, the second NUMNUMBERS distances to measurement 2, etc.
        short k=0;
        short half_NUMNUMBERS = (short)(NUMNUMBERS >> 1);
        // This is necessary because Java doesn't have unsigned types
        if (distance_threshold > 0) {
            for (short i=0; i<NUMNUMBERS; i++) {
                short exceed_threshold_count = 0;
                for (short j=0; j<NUMNUMBERS; j++) {
                    short diff = distance_matrix[k++];
                    if (diff < 0 || diff > distance_threshold) {
                        exceed_threshold_count++;
                    }
                }

                if (exceed_threshold_count > half_NUMNUMBERS) {
                    outliers[i] = true;
                } else {
                    outliers[i] = false;
                }
            }
        } else {
            for (short i=0; i<NUMNUMBERS; i++) {
                short exceed_threshold_count = 0;
                for (short j=0; j<NUMNUMBERS; j++) {
                    short diff = distance_matrix[k++];
                    if (diff < 0 && diff > distance_threshold) {
                        exceed_threshold_count++;
                    }
                }

                if (exceed_threshold_count > half_NUMNUMBERS) {
                    outliers[i] = true;
                } else {
                    outliers[i] = false;
                }
            }
        }
    }
}
