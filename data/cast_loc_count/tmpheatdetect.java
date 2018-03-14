public class HeatDetect {
    public static final byte THRESHOLD_LEVEL1 = 2;
    public static final byte THRESHOLD_LEVEL2 = 3;
    public static final byte RED              = 4;
    public static final byte ORANGE           = 3;
    public static final byte YELLOW           = 2;
    public static final byte WHITE            = 1;
    public static final byte ARRAY_SIZE       = 8;
    public static final byte WAITTOCHECK      = 90;
    public static final byte CHECKED          = 91;
    public static final byte NEIGHBOR         = 92;
    public static final byte BOUNDARY         = 99;
    public static final byte LEFT_UP          = 7;
    public static final byte RIGHT_UP         = 63;
    public static final byte LEFT_DOWN        = 0;
    public static final byte RIGHT_DOWN       = 56;
    public static int x_weight_coordinate = 0;
    public static int y_weight_coordinate = 0;
    public static int xh_weight_coordinate = 0;
    public static int yh_weight_coordinate = 0;
    public static int yellowGroupH = 0;
    public static int yellowGroupL = 0;
    public static int orangeGroupH = 0;
    public static int orangeGroupL = 0;
    public static int redGroupH = 0;
    public static int redGroupL = 0;
    public static boolean[] zscoreWeight = null;
    private static short[] neighbor = new short[8];
    private static final short zscore_threshold_high = 1000;
    private static final short zscore_threshold_low = 500;
    private static final short zscore_threshold_hot = 5000;
    private static final short zscore_threshold_recal = 1000;
    public static void heat_detect(short[] frame_buffer, byte[] color, byte[] rColor, int[] largestSubset, int[] testset, int[] result) {
        HeatCalib.zscoreCalculation(frame_buffer);
        ShortWrapper maxSubsetLen = new ShortWrapper();
        maxSubsetLen.value = 0;
        get_largest_subset(largestSubset, maxSubsetLen, testset, result);
        reset_log_variable(color);
        if (maxSubsetLen.value > 1) {
            if (HeatCalib.z_max > zscore_threshold_hot) {
                get_filtered_xy(largestSubset, maxSubsetLen.value);
            } else if (HeatCalib.z_max > zscore_threshold_low) {
                get_xy(largestSubset, maxSubsetLen.value);
            }
            labelPixel(largestSubset, maxSubsetLen.value, color);
            rotateColor(color, rColor);
            findGroup(rColor);
        } else {
            RTC.beep(42);
            RTC.avroraBreak();        }    }
    private static void get_largest_subset(int[] largestSubset, ShortWrapper maxSubsetLen, int[] testset, int[] result) {
        int pixelCount=0;
        for(short i=0; i<64; i++){
            testset[i]=0;        }
        for(short i=0; i<64; i++){
            if (HeatCalib.zscore[i] > zscore_threshold_high) {
                testset[pixelCount]=i;
                pixelCount++;
                zscoreWeight[i]=true;
            } else if (HeatCalib.zscore[i] > zscore_threshold_low) {
                if (zscoreWeight[i] || check_neighbor_zscore_weight(i)) {
                    testset[pixelCount]=i;
                    pixelCount++;
                    zscoreWeight[i]=true;                }
            } else {
                zscoreWeight[i]=false;            }        }
        testset[pixelCount] = BOUNDARY;
        find_largestSubset(testset, pixelCount, maxSubsetLen, largestSubset, result);
        if (maxSubsetLen.value == 1) {
          maxSubsetLen.value = 0;
          largestSubset[0] = -1;         }    }
    private static void find_largestSubset(int[] testset, int testsetLen, ShortWrapper maxSubsetLen, int[] largestSubset, int[] result){
        for(short i=0; i<64;i++){result[i]=WAITTOCHECK;}
        int subsetNumber = 0;
        ShortWrapper startIndex = new ShortWrapper();
        startIndex.value = 0;
        while(get_startIndex(testset, testsetLen, result, startIndex)){
            result[testset[startIndex.value]]=subsetNumber;
            label_subset(testset, testsetLen, result, subsetNumber);
            subsetNumber++;        }
        select_largest_subset(testset, testsetLen, result, subsetNumber, maxSubsetLen, largestSubset);    }
    private static void select_largest_subset(int[] testset, int testsetLen, int[] result, int subsetNumber, ShortWrapper maxSubsetLen, int[] largestSubset){
        int maxSubsetNumber = 0;
        for(short i=0; i<subsetNumber; i++){
            short lengthCount = 0;
            for(short j=0; j<testsetLen; j++){
                if(result[testset[j]] == i){
                    lengthCount++;                }            }
            if(lengthCount > maxSubsetLen.value){ // if equal, no solution currently
                maxSubsetLen.value = lengthCount;
                maxSubsetNumber = i;            }        }
        short index=0;
        for(short i=0; i<64; i++){
            if(result[i]==maxSubsetNumber){
                largestSubset[index]=i;
                index++;            }        }    }
    private static void reset_log_variable(byte[] color)    {
        for(short i=0; i<64; i++) {
            color[i] = WHITE;        }
        x_weight_coordinate = -1;
        y_weight_coordinate = -1;
        xh_weight_coordinate = -1;
        yh_weight_coordinate = -1;
        yellowGroupH = 0;
        yellowGroupL = 0;
        orangeGroupH = 0;
        orangeGroupL = 0;
        redGroupH = 0;
        redGroupL = 0;    }
    private static void get_filtered_xy(int[] largestSubset, int maxSubsetLen) {
        short x_weight_zscore_sum = 0;
        short y_weight_zscore_sum = 0;
        int zscore_sum = 0;
        byte low_zscore_length = 0;
        short xh_weight_zscore_sum = 0;
        short yh_weight_zscore_sum = 0;
        int zscore_sum_h = 0;
        byte hot_zscore_length = 0;
        for(short i=0; i<maxSubsetLen; i++) {
            short _zscore = HeatCalib.zscore[largestSubset[i]];
            if (_zscore > zscore_threshold_hot) {
                zscore_sum_h += _zscore;
                hot_zscore_length += 1;
            } else if (_zscore > zscore_threshold_low) {
                zscore_sum += _zscore;
                low_zscore_length += 1;            }        }
        for(short i=0; i<maxSubsetLen; i++) {
            int _x = largestSubset[i] % 8;
            int _y = largestSubset[i] >>> 3;
            short _zscore = HeatCalib.zscore[largestSubset[i]];
            if (_zscore > zscore_threshold_hot) {
                xh_weight_zscore_sum += _x * _zscore / zscore_sum_h;
                yh_weight_zscore_sum += _y * _zscore / zscore_sum_h;
            } else if (_zscore > zscore_threshold_low) {
                x_weight_zscore_sum += _x * _zscore / zscore_sum;
                y_weight_zscore_sum += _y * _zscore / zscore_sum;            }        }
        if (hot_zscore_length > 0) {
            xh_weight_coordinate = xh_weight_zscore_sum;
            yh_weight_coordinate = yh_weight_zscore_sum;        }
        if (low_zscore_length > 0) {
            x_weight_coordinate = x_weight_zscore_sum;
            y_weight_coordinate = y_weight_zscore_sum;        }    }
    private static void get_xy(int[] largestSubset, int maxSubsetLen) {
        short x_weight_zscore_sum = 0;
        short y_weight_zscore_sum = 0;
        int zscore_sum = 0;
        for(short i=0; i<maxSubsetLen; i++) {
            short _zscore = HeatCalib.zscore[largestSubset[i]];
            zscore_sum += _zscore;        }
        for(short i=0; i<maxSubsetLen; i++) {
            int _x = largestSubset[i] % 8;
            int _y = largestSubset[i] >>> 3;
            short _zscore = HeatCalib.zscore[largestSubset[i]];
            x_weight_zscore_sum += 100 * _x * _zscore / zscore_sum;
            y_weight_zscore_sum += 100 * _y * _zscore / zscore_sum;        }
        x_weight_coordinate = x_weight_zscore_sum;
        y_weight_coordinate = y_weight_zscore_sum;    }
    private static void labelPixel(int[] largestSubset, int maxSubsetLen, byte[] color)    {
        for(short i=0; i<maxSubsetLen; i++) {
            int pixelIndex = largestSubset[i];
            if (HeatCalib.zscore[pixelIndex] > zscore_threshold_hot) {
                color[pixelIndex] = RED;
            } else if (HeatCalib.zscore[pixelIndex] > zscore_threshold_high) {
                color[pixelIndex] = ORANGE;
            } else if (HeatCalib.zscore[pixelIndex] > zscore_threshold_low) {
                color[pixelIndex] = YELLOW;            }        }    }
    private static void rotateColor(byte[] color, byte[] rColor)    {
        for(short i=0; i<8; i++) {
            for (short j=0; j<8; j++) {
                short i_shift3 = (short)(i<<3);
                rColor[(i_shift3)+j] = color[LEFT_UP + (j<<3) - i];            }        }    }
    private static void findGroup(byte[] color)    {
        for(short i=0; i<32; i++){
            byte cl = color[i];
            int one_shift_i = 1<<i;
            if(cl == YELLOW){
                yellowGroupL |= one_shift_i;
            }else if(cl == ORANGE){
                orangeGroupL |= one_shift_i;
            }else if(cl == RED){
                redGroupL |= one_shift_i;            }
            cl = color[i+32];
            if(cl == YELLOW){
                yellowGroupH |= one_shift_i;
            }else if(cl == ORANGE){
                orangeGroupH |= one_shift_i;
            }else if(cl == RED){
                redGroupH |= one_shift_i;            }        }    }
    private static boolean get_startIndex(int[] testset, int testsetLen, int[] result, ShortWrapper startIndex) {
        boolean rv = false; // done this way to avoid values on the stack at a brtarget
        for(short i=0; i<testsetLen; i++){
            if(result[testset[i]] == WAITTOCHECK){
                startIndex.value = i;
                rv = true;
                break;            }        }
        return rv;    }
    private static void label_subset(int[] testset, int testsetLen, int[] result, int subsetNumber) {
        while(label_neighbor(result, subsetNumber)){
            for(short i=0; i< testsetLen; i++){
                short testset_i = (short)testset[i];
                if(result[testset_i] == NEIGHBOR){
                    result[testset_i]=subsetNumber;                }            }
            for(short i=0; i<64; i++){
                if(result[i] == NEIGHBOR){
                    result[i]=CHECKED;                }            }        }    }
    private static void get_eight_neighbor(short loc, short[] neighbor)     {
        for (short i=0; i<8; i++) {
            neighbor[i] = -1;        }
        short loc_div_array_size = (short)(loc >>> 3);
        short loc_mod_array_size = (short)(loc % ARRAY_SIZE);
        if((loc_div_array_size)==0 && (loc_mod_array_size)==0){
            neighbor[0]=(short)(loc+1); neighbor[1]=(short)(loc+ARRAY_SIZE); neighbor[2]=(short)(loc+ARRAY_SIZE+1);
        }else if((loc_div_array_size)==0 && (loc_mod_array_size)>0 && (loc_mod_array_size)<(ARRAY_SIZE-1)){
            neighbor[0]=(short)(loc-1); neighbor[1]=(short)(loc+1); neighbor[2]=(short)(loc+ARRAY_SIZE-1); neighbor[3]=(short)(loc+ARRAY_SIZE); neighbor[4]=(short)(loc+ARRAY_SIZE+1);
        }else if((loc_div_array_size)==0 && (loc_mod_array_size)==(ARRAY_SIZE-1)){
            neighbor[0]=(short)(loc-1); neighbor[1]=(short)(loc+ARRAY_SIZE-1); neighbor[2]=(short)(loc+ARRAY_SIZE);
        }else if((loc_div_array_size)> 0 && (loc_div_array_size)<(ARRAY_SIZE-1) && (loc_mod_array_size)==0){
            neighbor[0]=(short)(loc-ARRAY_SIZE); neighbor[1]=(short)(loc-ARRAY_SIZE+1); neighbor[2]=(short)(loc+1); neighbor[3]=(short)(loc+ARRAY_SIZE); neighbor[4]=(short)(loc+ARRAY_SIZE+1);
        }else if((loc_div_array_size)>0 && (loc_div_array_size)<(ARRAY_SIZE-1) && (loc_mod_array_size)>0 && (loc_mod_array_size)<(ARRAY_SIZE-1)){
            neighbor[0]=(short)(loc-ARRAY_SIZE-1); neighbor[1]=(short)(loc-ARRAY_SIZE); neighbor[2]=(short)(loc-ARRAY_SIZE+1); neighbor[3]=(short)(loc-1); neighbor[4]=(short)(loc+1); neighbor[5]=(short)(loc+ARRAY_SIZE-1); neighbor[6]=(short)(loc+ARRAY_SIZE); neighbor[7]=(short)(loc+ARRAY_SIZE+1);
        }else if((loc_div_array_size)>0 && (loc_div_array_size)<(ARRAY_SIZE-1) && (loc_mod_array_size)==(ARRAY_SIZE-1)){
            neighbor[0]=(short)(loc-ARRAY_SIZE-1); neighbor[1]=(short)(loc-ARRAY_SIZE); neighbor[2]=(short)(loc-1); neighbor[3]=(short)(loc+ARRAY_SIZE-1); neighbor[4]=(short)(loc+ARRAY_SIZE);
        }else if((loc_div_array_size)==(ARRAY_SIZE-1) && (loc_mod_array_size)==0){
            neighbor[0]=(short)(loc-ARRAY_SIZE); neighbor[1]=(short)(loc-ARRAY_SIZE+1); neighbor[2]=(short)(loc+1);
        }else if((loc_div_array_size)==(ARRAY_SIZE-1) && (loc_mod_array_size)>0 && (loc_mod_array_size)<(ARRAY_SIZE-1)){
            neighbor[0]=(short)(loc-ARRAY_SIZE-1); neighbor[1]=(short)(loc-ARRAY_SIZE); neighbor[2]=(short)(loc-ARRAY_SIZE+1); neighbor[3]=(short)(loc-1); neighbor[4]=(short)(loc+1);
        }else if((loc_div_array_size)==(ARRAY_SIZE-1) && (loc_mod_array_size)==(ARRAY_SIZE-1)){
            neighbor[0]=(short)(loc-ARRAY_SIZE-1); neighbor[1]=(short)(loc-ARRAY_SIZE); neighbor[2]=(short)(loc-1);        }    }
    private static void get_four_neighbor(short loc, short[] neighbor)     {
        for (short i=0; i<4; i++) {
            neighbor[i] = -1;        }
        short loc_div_array_size = (short)(loc >>> 3);
        short loc_mod_array_size = (short)(loc % ARRAY_SIZE);
        if((loc_div_array_size)==0 && (loc_mod_array_size)==0){
            neighbor[0]=(short)(loc+1); neighbor[1]=(short)(loc+ARRAY_SIZE);
        }else if((loc_div_array_size)==0 && (loc_mod_array_size)>0 && (loc_mod_array_size)<(ARRAY_SIZE-1)){
            neighbor[0]=(short)(loc-1); neighbor[1]=(short)(loc+1); neighbor[2]=(short)(loc+ARRAY_SIZE);
        }else if((loc_div_array_size)==0 && (loc_mod_array_size)==(ARRAY_SIZE-1)){
            neighbor[0]=(short)(loc-1); neighbor[1]=(short)(loc+ARRAY_SIZE);
        }else if((loc_div_array_size)> 0 && (loc_div_array_size)<(ARRAY_SIZE-1) && (loc_mod_array_size)==0){
            neighbor[0]=(short)(loc-ARRAY_SIZE); neighbor[1]=(short)(loc+1); neighbor[2]=(short)(loc+ARRAY_SIZE);
        }else if((loc_div_array_size)>0 && (loc_div_array_size)<(ARRAY_SIZE-1) && (loc_mod_array_size)>0 && (loc_mod_array_size)<(ARRAY_SIZE-1)){
            neighbor[0]=(short)(loc-ARRAY_SIZE); neighbor[1]=(short)(loc-1); neighbor[2]=(short)(loc+1); neighbor[3]=(short)(loc+ARRAY_SIZE);
        }else if((loc_div_array_size)>0 && (loc_div_array_size)<(ARRAY_SIZE-1) && (loc_mod_array_size)==(ARRAY_SIZE-1)){
            neighbor[0]=(short)(loc-ARRAY_SIZE); neighbor[1]=(short)(loc-1); neighbor[2]=(short)(loc+ARRAY_SIZE);
        }else if((loc_div_array_size)==(ARRAY_SIZE-1) && (loc_mod_array_size)==0){
            neighbor[0]=(short)(loc-ARRAY_SIZE); neighbor[1]=(short)(loc+1);
        }else if((loc_div_array_size)==(ARRAY_SIZE-1) && (loc_mod_array_size)>0 && (loc_mod_array_size)<(ARRAY_SIZE-1)){
            neighbor[0]=(short)(loc-ARRAY_SIZE); neighbor[1]=(short)(loc-1); neighbor[2]=(short)(loc+1);
        }else if((loc_div_array_size)==(ARRAY_SIZE-1) && (loc_mod_array_size)==(ARRAY_SIZE-1)){
            neighbor[0]=(short)(loc-ARRAY_SIZE); neighbor[1]=(short)(loc-1);        }    }
    private static boolean label_neighbor(int[] result, int subsetNumber){
        boolean hasNeighbor = false;
        for(short i=0; i<64; i++){
            if(result[i]==subsetNumber){
                get_eight_neighbor(i, neighbor);
                for(short j=0; j<8;j++){
                    short neighbor_j = neighbor[j];
                    if(neighbor_j != -1 && result[neighbor_j] == WAITTOCHECK){
                        result[neighbor_j]=NEIGHBOR;
                        hasNeighbor = true;                    }                }            }        }
        return hasNeighbor;    }
    private static boolean check_neighbor_zscore_weight(short index) {
        boolean rv = false;  // done this way to avoid values on the stack at a brtarget
        get_four_neighbor(index, neighbor);
        for(short i=0; i<4;i++){
            short neighbor_i = neighbor[i];
            if (neighbor_i != -1) {
                if (zscoreWeight[neighbor_i]) {
                    rv = true;
                    break;                }            }        }
        return rv;    }}