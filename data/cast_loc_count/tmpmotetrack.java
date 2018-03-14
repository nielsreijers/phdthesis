public class DB {
    public final static short REFSIGNATUREDB_SIZE = 257;
    public final static short SIGNATUREDB_SIZE = 1; // commented out the other signature since we only use the first one
    @Lightweight
    public static void signature_get(RefSignature s, short index) {
        Point location = s.location;
        Signature sig = s.sig;
        location.x = SignatureDB.X.data[index];
        location.y = SignatureDB.Y.data[index];
        location.z = SignatureDB.Z.data[index];
        sig.id = SignatureDB.SigID.data[index];
        short rfSignalIndex = (short)(index * Signature.NBR_RFSIGNALS_IN_SIGNATURE); // sourceID, rssi0 and rssi1 arrays contain the Signature.NBR_RFSIGNALS_IN_SIGNATURE elements for each RefSignature in a row.
        RFSignal[] rfSignals = sig.rfSignals;
        for (short i=0; i<Signature.NBR_RFSIGNALS_IN_SIGNATURE; i++) {
            RFSignal rfSignal = rfSignals[i];
            rfSignal.sourceID = SignatureDB.SourceID.data[rfSignalIndex];
            rfSignal.rssi_0 = SignatureDB.Rssi0.data[rfSignalIndex];
            rfSignal.rssi_1 = SignatureDB.Rssi1.data[rfSignalIndex];
            rfSignalIndex++;        }    }
    @Lightweight
    public static void refSignature_get(RefSignature s, short index) {
        Point location = s.location;
        Signature sig = s.sig;
        location.x = RefSignatureDB.X.data[index];
        location.y = RefSignatureDB.Y.data[index];
        location.z = RefSignatureDB.Z.data[index];
        sig.id = RefSignatureDB.SigID.data[index];
        short rfSignalIndex = (short)(index * Signature.NBR_RFSIGNALS_IN_SIGNATURE); // sourceID, rssi0 and rssi1 arrays contain the Signature.NBR_RFSIGNALS_IN_SIGNATURE elements for each RefSignature in a row.
	    RFSignal[] rfSignals = sig.rfSignals;
        for (short i=0; i<Signature.NBR_RFSIGNALS_IN_SIGNATURE; i++) {
        	RFSignal rfSignal = rfSignals[i];
        	rfSignal.sourceID = RefSignatureDB.SourceID.data[rfSignalIndex];
        	rfSignal.rssi_0 = RefSignatureDB.Rssi0.data[rfSignalIndex];
        	rfSignal.rssi_1 = RefSignatureDB.Rssi1.data[rfSignalIndex];
        	rfSignalIndex++;        }    }}
public class Debug {
    public static void printSignature(Signature s) {
        RTC.avroraPrintShort(s.id);
        for (int i=0; i<Signature.NBR_RFSIGNALS_IN_SIGNATURE; i++) {
            RTC.beep(i);
            RTC.avroraPrintShort(s.rfSignals[i].sourceID);
            RTC.avroraPrintShort(s.rfSignals[i].rssi_0);
            RTC.avroraPrintShort(s.rfSignals[i].rssi_1);        }
        RTC.avroraBreak();    }
    public static void printRefSignature(RefSignature r) {
        RTC.avroraPrintShort(r.location.x);
        RTC.avroraPrintShort(r.location.y);
        RTC.avroraPrintShort(r.location.z);
        RTC.avroraPrintShort(r.sig.id);
        for (int i=0; i<Signature.NBR_RFSIGNALS_IN_SIGNATURE; i++) {
            RTC.beep(i);
            RTC.avroraPrintShort(r.sig.rfSignals[i].sourceID);
            RTC.avroraPrintShort(r.sig.rfSignals[i].rssi_0);
            RTC.avroraPrintShort(r.sig.rfSignals[i].rssi_1);        }
        RTC.avroraBreak();    }
    public static void printRFSignalAvgHT(RFSignalAvgHT h) {
        RTC.avroraPrintShort(h.size);
        RTC.avroraPrintShort(h.capacity);
        for (int i=0; i<h.capacity; i++) {
            RTC.beep(i);
            RTC.avroraPrintShort(h.htData[i].sourceID);
            RTC.avroraPrintShort(h.htData[i].rssiSum_0);
            RTC.avroraPrintShort(h.htData[i].nbrSamples_0);
            RTC.avroraPrintShort(h.htData[i].rssiSum_1);
            RTC.avroraPrintShort(h.htData[i].nbrSamples_1);        }
        RTC.avroraBreak();    }}
public class EstimateLoc {
    private static final byte KNEAREST_SIZE = 2;
    private static final byte MAX_REFSIGS_CONS = 2;
    private static final byte BIDIRECTIONAL_ALG  = 0;
    private static final byte UNIDIRECTIONAL_ALG = 1;
    private static final byte signatureDiffAlg = BIDIRECTIONAL_ALG;
    public static void nearestRefSigs(Object[] retSSDiffs, Signature sigPtr, RefSignature currRefSig)    {
        short i=0, f=0; //, p=0;
        ShortResults currSigDiffs = new ShortResults();
        for (f = 0; f < MoteTrackParams.NBR_FREQCHANNELS; ++f) {
            SignalSpaceDiff[] tmp = (SignalSpaceDiff[])retSSDiffs[f]; // To avoid CHECKCAST on each iteration
            for (i = 0; i < MAX_REFSIGS_CONS; ++i) {
                SignalSpaceDiff.init(tmp[i]);            }        }
        ShortResults currSigDiffsForSignatureDiffBidirectional = new ShortResults();
        SignalSpaceDiff[] retSSDiffs_0 = (SignalSpaceDiff[])retSSDiffs[0]; // To avoid CHECKCAST on each iteration
        SignalSpaceDiff[] retSSDiffs_1 = (SignalSpaceDiff[])retSSDiffs[1]; // To avoid CHECKCAST on each iteration
            DB.refSignature_get(currRefSig, i);
            if (signatureDiffAlg == BIDIRECTIONAL_ALG)
                RefSignature.signatureDiffBidirectional(currSigDiffs, currRefSig, sigPtr, currSigDiffsForSignatureDiffBidirectional);
            else if (signatureDiffAlg == UNIDIRECTIONAL_ALG)
                RefSignature.signatureDiffUnidirectional(currSigDiffs, currRefSig, sigPtr);
            else {
                RTC.avroraPrintHex32(0xBEEFBEEF);
                RTC.avroraPrintHex32(0x1);
                RTC.avroraBreak();           }
            for (f = 0; f < MoteTrackParams.NBR_FREQCHANNELS; ++f)
                SignalSpaceDiff.put(retSSDiffs_0, retSSDiffs_1, (byte)f, currSigDiffs, i);        }    }
    public static void estimateLoc(Point retLocPtr, Signature sigPtr, RefSignature refSigPtr)    {
        short f=0; //, p=0; //, r=0;
        Object[] ssDiffs = new Object[MoteTrackParams.NBR_FREQCHANNELS];
        for (f=0; f<MoteTrackParams.NBR_FREQCHANNELS; f++) {
            SignalSpaceDiff[] ssDiffsarray = new SignalSpaceDiff[MAX_REFSIGS_CONS];
            for (short i=0; i<MAX_REFSIGS_CONS; i++) {
                ssDiffsarray[i] = new SignalSpaceDiff();            }
            ssDiffs[f] = ssDiffsarray;        }
        Point[] locEstEachFreqPower = new Point[MoteTrackParams.NBR_FREQCHANNELS];       // centroid for each txPower
        Point[] locCombFreqPower = new Point[MoteTrackParams.NBR_FREQCHANNELS];
        for (f=0; f<MoteTrackParams.NBR_FREQCHANNELS; f++) {
            locEstEachFreqPower[f] = new Point();
            locCombFreqPower[f] = new Point();        }
        EstimateLoc.nearestRefSigs(ssDiffs, sigPtr, refSigPtr);
        for (f = 0; f < MoteTrackParams.NBR_FREQCHANNELS; ++f) {
                SignalSpaceDiff.centroidLoc(locEstEachFreqPower[f], (SignalSpaceDiff[])(ssDiffs[f]), KNEAREST_SIZE, refSigPtr);
            locCombFreqPower[f] = locEstEachFreqPower[f];        }
        Point.centroidLoc(retLocPtr, locCombFreqPower);    }}
public class MobileMoteM {
    public static final byte NBR_HT = 2;
    public static final byte RFSIGNALAVG_HT_SIZE = 20/*NBR_RFSIGNALS_IN_SIGNATURE*/ + 5;
    public static RFSignalAvgHT[] rfSignalHT;
    public static byte currHT = 0;
    public static short indexNextSigEst = 0;
    public static short addSignatureFromFile(RefSignature currRefSig)    {
        short i = 0;
        byte f = 0, p = 0, k = 0;
        Signature sigPtr = currRefSig.sig;
        DB.signature_get(currRefSig, indexNextSigEst);
        indexNextSigEst = (short)((indexNextSigEst+1) % DB.SIGNATUREDB_SIZE);
        for (k = 0; k < 3; ++k)  // simulates adding multiple samples to the hashtable
            for (i = 0; i < Signature.NBR_RFSIGNALS_IN_SIGNATURE; ++i) {
                RFSignal sigPtr_rfSignals_i = sigPtr.rfSignals[i];
                if (sigPtr_rfSignals_i.sourceID != 0) {
                        RFSignalAvgHT.put(rfSignalHT[currHT], sigPtr_rfSignals_i.sourceID, (byte)0, sigPtr_rfSignals_i.rssi_0);
                        RFSignalAvgHT.put(rfSignalHT[currHT], sigPtr_rfSignals_i.sourceID, (byte)1, sigPtr_rfSignals_i.rssi_1);                }            }
        return sigPtr.id;    }
    public static short constructSignature(Signature sigPtr)    {
        short srcAddrBcnMaxRSSIPtr;
        byte prevHT = 0;
        prevHT = currHT;
        currHT = (byte)((currHT+1) % NBR_HT);
        RFSignalAvgHT.init(rfSignalHT[currHT]);
        srcAddrBcnMaxRSSIPtr = RFSignalAvgHT.makeSignature(sigPtr, rfSignalHT[prevHT]);
        return srcAddrBcnMaxRSSIPtr;    }
    public static Point estLocAndSend()    {
        Point locEst = new Point();
        Signature sig = new Signature();
        RefSignature refSig = new RefSignature();
        short srcAddrBcnMaxRSSI;
        Point.init(locEst);
        Signature.init(sig);
        short sig_id = addSignatureFromFile(refSig);
        if ((srcAddrBcnMaxRSSI = constructSignature(sig)) == 0) {
            locEst.x = locEst.y = locEst.z = 0;
            RTC.avroraPrintHex32(0xBEEF0006);
            return locEst;        }
        sig.id = sig_id;
        EstimateLoc.estimateLoc(locEst, sig, refSig);
        return locEst;    }
    public static void motetrack_init_benchmark() {
        rfSignalHT = new RFSignalAvgHT[NBR_HT];
        for (int i=0; i<NBR_HT; i++) {
            rfSignalHT[i] =  new RFSignalAvgHT(RFSIGNALAVG_HT_SIZE);        }
        currHT = 0;
        indexNextSigEst = 0;    }}
public class MoteTrackParams {
    public static final byte NBR_FREQCHANNELS = 2;}
public class Point {
    public short x, y, z;
    public Point() {
        init(this);    }
    @Lightweight
    public static void init(Point pointPtr) {
        pointPtr.x = 0;
        pointPtr.y = 0;
        pointPtr.z = 0;    }
    static void centroidLoc(Point retLocPtr, Point points[])    {
        short i = 0;
        short nbrPoints = (short)points.length;
        int x=0, y=0, z=0;  // to prevent overflow from adding multiple 16-bit points
        for (i = 0; i < nbrPoints; ++i) {
            x += points[i].x;
            y += points[i].y;
            z += points[i].z;        }
        retLocPtr.x = (short) (x / i);
        retLocPtr.y = (short) (y / i);
        retLocPtr.z = (short) (z / i);    }}
public class RefSignature {
    public Point location;
    public Signature sig;
    public RefSignature() {
        this.location = new Point();
        this.sig = new Signature();    }
    public static void signatureDiffBidirectional(ShortResults results, RefSignature refSigPtr, Signature sigPtr, ShortResults currSigDiffs)    {
        short s = 0, r = 0, f = 0; //, p = 0;
        results.r0 = 0;
        results.r1 = 1;
        RFSignal[] sigPtr_rfSignals = sigPtr.rfSignals;
        RFSignal[] refSigPtr_sig_rfSignals = refSigPtr.sig.rfSignals;
        while( (s < Signature.NBR_RFSIGNALS_IN_SIGNATURE) ||
               (r < Signature.NBR_RFSIGNALS_IN_SIGNATURE) ) {
            RFSignal sigPtr_rfSignals_s = sigPtr_rfSignals[s];
            RFSignal refSigPtr_sig_rfSignals_r = refSigPtr_sig_rfSignals[r];
            short sigPtr_rfSignals_s_sourceID = sigPtr_rfSignals_s.sourceID;
            short refSigPtr_sig_rfSignals_r_sourceID = refSigPtr_sig_rfSignals_r.sourceID;
            if (!(sigPtr_rfSignals_s_sourceID != 0 || refSigPtr_sig_rfSignals_r_sourceID != 0)) {
                break;            }
            if ( !(s < Signature.NBR_RFSIGNALS_IN_SIGNATURE && sigPtr_rfSignals_s_sourceID != 0) ) {
                RFSignal.rfSignalDiff(currSigDiffs, refSigPtr_sig_rfSignals_r, null);
                r++;            }
            else if ( !(r < Signature.NBR_RFSIGNALS_IN_SIGNATURE && refSigPtr_sig_rfSignals_r_sourceID != 0) ) {
                RFSignal.rfSignalDiff(currSigDiffs, sigPtr_rfSignals_s, null);
                s++;            }
            else if (sigPtr_rfSignals_s_sourceID == refSigPtr_sig_rfSignals_r_sourceID) {
                RFSignal.rfSignalDiff(currSigDiffs, sigPtr_rfSignals_s, refSigPtr_sig_rfSignals_r );
                r++;
                s++;            }
            else if (sigPtr_rfSignals_s_sourceID < refSigPtr_sig_rfSignals_r_sourceID) {
                RFSignal.rfSignalDiff(currSigDiffs, sigPtr_rfSignals_s, null);
                s++;            }
            else if (sigPtr_rfSignals_s_sourceID > refSigPtr_sig_rfSignals_r_sourceID) {
                RFSignal.rfSignalDiff(currSigDiffs, refSigPtr_sig_rfSignals_r, null);
                r++;            }
            else {
                RTC.avroraPrintHex32(0xBEEF0002);
                RTC.avroraBreak();            }
            results.r0 += currSigDiffs.r0;
            results.r1 += currSigDiffs.r1;        }    }
    public static void signatureDiffUnidirectional(ShortResults results, RefSignature refSigPtr, Signature sigPtr) {    }}
public class RefSignatureDB {
    @ConstArray
    public static class X {
        public final static short[] data = {171, 200, 230, 171, 200, 230, 171, 200, 229, 254, 283, 306, 329, 354, 384, 408, 435, 462, 488, 513, 538, 564, 587, 615, 641, 669, 695, 719, 749, 779, 804, 828, 851, 879, 902, 930, 955, 983, 1012,1038,1058,1085,1115,505, 505, 505, 505, 504, 505, 505, 505, 505, 505, 505, 505, 898, 898, 898, 898, 899, 900, 901, 902, 902, 901, 901, 901, 901, 901, 901, 901, 901, 901, 900, 900, 900, 900, 900, 900, 928, 972, 1012,1047,1086,1129,1130,410, 435, 462, 486, 514, 539, 568, 595, 618, 641, 666, 692, 719, 746, 774, 802, 830, 858, 884, 910, 936, 961, 982, 1008,1035,1062,1085,1108,1136,1163,1192,1217,1243,1271,1299,1327,1353,1378,1403,1426,1453,1477,1503,1532,1557,1583,1608,1634,1660,1684,1710,1738,1765,1790,1815,1842,1868,1895,1920,436, 438, 366, 368, 518, 518, 516, 627, 627, 625, 717, 721, 795, 793, 984, 984, 1081,1080,1080,1171,1174,1173,1267,1267,1267,1360,1364,1363,1459,1459,1459,1550,1553,1553,1647,1646,1646,1740,1742,1742,1836,1837,1837,1926,1929,1930,1741,1656,1638,1561,1560,1560,1372,1370,1371,1374,1312,1223,1216,1286,1460,1464,1466,1234,1233,1096,1097,1097,831, 786, 557, 625, 687, 555, 620, 687, 561, 630, 700, 441, 441, 339, 342, 434, 233, 234, 227, 156, 154, 49 , 50 , 159, 89 , 57 , 109, 242, 248, 248, 336, 332, 438, 417, 523, 532, 531, 621, 620, 624, 817, 723, 696, 793};    }
    @ConstArray
    public static class Y {
        public final static short[] data = {275, 275, 276, 250, 250, 250, 226, 226, 226, 225, 225, 224, 223, 224, 225, 224, 224, 224, 224, 224, 224, 224, 224, 224, 224, 224, 225, 225, 225, 225, 225, 225, 225, 225, 225, 225, 225, 225, 225, 225, 225, 225, 225, 249, 276, 303, 330, 356, 382, 403, 428, 457, 482, 514, 540, 79,  102, 130, 155, 185, 213, 248, 277, 303, 329, 355, 378, 405, 430, 455, 483, 508, 536, 606, 634, 664, 690, 715, 738, 79,  95,  104, 118, 135, 160, 195, 572, 572, 572, 572, 572, 572, 572, 572, 572, 572, 572, 572, 572, 572, 572, 572, 572, 572, 572, 572, 572, 572, 572, 572, 572, 572, 572, 572, 572, 572, 572, 572, 572, 572, 572, 572, 572, 572, 572, 572, 572, 572, 572, 572, 572, 572, 572, 572, 572, 572, 572, 572, 572, 572, 572, 572, 572, 572, 572, 646, 739, 647, 737, 636, 692, 737, 635, 690, 738, 634, 739, 634, 740, 633, 739, 633, 696, 740, 634, 696, 740, 633, 697, 741, 633, 696, 739, 633, 694, 739, 632, 695, 739, 633, 693, 743, 634, 697, 739, 631, 692, 739, 630, 693, 739, 439, 394, 499, 511, 437, 372, 493, 412, 334, 242, 223, 186, 287, 287, 335, 412, 491, 476, 359, 491, 370, 332, 500, 468, 471, 471, 468, 382, 383, 382, 277, 303, 292, 495, 369, 372, 495, 297, 329, 413, 490, 380, 477, 478, 382, 169, 169, 127, 69,  172, 116, 48,  169, 65,  63,  167, 168, 108, 49,  169, 116, 52,  52,  54,  170, 175};    }
    @ConstArray
    public static class Z {
        public final static short[] data = {  0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0};    }
    @ConstArray
    public static class SigID {
        public final static short[] data = {101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 123, 124, 125, 126, 127, 128, 129, 130, 131, 132, 133, 134, 135, 136, 137, 138, 139, 140, 141, 142, 143, 144, 145, 146, 147, 148, 149, 150, 151, 152, 153, 154, 155, 156, 157, 158, 159, 160, 161, 162, 163, 164, 165, 166, 167, 168, 169, 170, 171, 172, 173, 174, 175, 176, 177, 178, 179, 180, 181, 182, 183, 184, 185, 186, 192, 193, 194, 195, 196, 197, 198, 199, 200, 201, 202, 203, 204, 205, 206, 207, 208, 209, 210, 211, 212, 213, 214, 215, 216, 217, 218, 219, 220, 221, 222, 223, 224, 225, 226, 227, 228, 229, 230, 231, 232, 233, 234, 235, 236, 237, 238, 239, 240, 241, 242, 243, 244, 245, 246, 247, 248, 249, 250, 300, 301, 302, 303, 304, 305, 306, 307, 308, 309, 310, 311, 312, 313, 314, 315, 316, 317, 318, 319, 320, 321, 322, 323, 324, 325, 326, 327, 328, 329, 340, 341, 342, 343, 344, 345, 346, 347, 348, 349, 350, 351, 352, 353, 354, 355, 356, 357, 358, 359, 360, 361, 362, 363, 364, 365, 366, 367, 368, 369, 370, 371, 372, 373, 374, 375, 376, 377, 378, 379, 380, 381, 382, 383, 384, 385, 386, 387, 388, 389, 390, 391, 392, 393, 394, 395, 396, 397, 398, 399, 400, 401, 402, 403, 404, 405, 406, 407, 408, 409, 410, 411, 412, 413, 414, 415, 416, 417, 418, 419, 420, 421};    }
    @ConstArray
    public static class SourceID {
        public final static short[] data = {  1,  3, 11, 13, 21, 22, 23,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  1,  3, 11, 13, 21, 22, 23, 26,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  1,  3, 11, 13, 21, 22, 23, 26,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  1,  3, 11, 12, 13, 21, 22, 23,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  1,  3, 11, 12, 13, 20, 21, 22, 23, 26, 30,  0,  0,  0,  0,  0,  0,  0,  1,  3, 11, 13, 20, 21, 22, 23, 26, 30,  0,  0,  0,  0,  0,  0,  0,  0,  1,  3, 11, 12, 13, 21, 22, 23, 30,  0,  0,  0,  0,  0,  0,  0,  0,  0,  1,  3, 11, 12, 13, 21, 22, 23, 26, 30,  0,  0,  0,  0,  0,  0,  0,  0,  1,  3, 11, 12, 13, 20, 21, 22, 23, 26, 30,  0,  0,  0,  0,  0,  0,  0,  1,  3, 11, 13, 20, 21, 22, 23, 26, 30,  0,  0,  0,  0,  0,  0,  0,  0,  1,  3, 11, 12, 13, 20, 21, 22, 23, 26, 30,  0,  0,  0,  0,  0,  0,  0,  1,  3,  9, 11, 12, 13, 20, 21, 22, 23, 26, 30,  0,  0,  0,  0,  0,  0,  1,  3,  9, 11, 12, 13, 20, 21, 22, 23, 30,  0,  0,  0,  0,  0,  0,  0,  1,  3,  9, 11, 12, 13, 20, 21, 22, 23, 26, 30,  0,  0,  0,  0,  0,  0,  1,  3, 11, 12, 13, 20, 21, 22, 23, 26, 30,  0,  0,  0,  0,  0,  0,  0,  1,  3,  9, 11, 12, 13, 20, 21, 22, 23, 26, 30,  0,  0,  0,  0,  0,  0,  1,  3,  9, 11, 12, 13, 20, 21, 22, 23, 26, 30,  0,  0,  0,  0,  0,  0,  1,  3,  9, 11, 12, 13, 20, 21, 22, 23, 26, 30,  0,  0,  0,  0,  0,  0,  1,  3,  9, 11, 12, 13, 16, 19, 20, 21, 22, 23, 26, 30,  0,  0,  0,  0,  1,  3,  9, 11, 12, 13, 16, 19, 20, 21, 22, 23, 26, 30,  0,  0,  0,  0,  1,  3,  9, 11, 12, 13, 16, 20, 21, 22, 23, 26, 30,  0,  0,  0,  0,  0,  1,  3,  9, 11, 12, 13, 16, 20, 21, 22, 23, 26, 30,  0,  0,  0,  0,  0,  1,  3,  9, 11, 12, 13, 20, 21, 22, 23, 26, 30,  0,  0,  0,  0,  0,  0,  1,  3,  5,  9, 11, 12, 13, 21, 22, 23, 26, 30,  0,  0,  0,  0,  0,  0,  1,  3,  5,  9, 11, 12, 13, 19, 20, 21, 22, 23, 26, 30,  0,  0,  0,  0,  1,  3,  5,  6,  9, 11, 12, 13, 20, 21, 22, 23, 26, 30,  0,  0,  0,  0,  1,  3,  5,  6,  9, 11, 12, 13, 16, 19, 20, 21, 22, 23, 26, 30,  0,  0,  1,  3,  5,  6,  9, 11, 12, 13, 16, 19, 20, 21, 22, 23, 26, 30,  0,  0,  1,  3,  5,  9, 12, 13, 19, 20, 21, 22, 23, 26, 30,  0,  0,  0,  0,  0,  1,  3,  5, 11, 12, 13, 16, 19, 20, 21, 22, 23, 26, 30,  0,  0,  0,  0,  1,  2,  3,  5,  6,  9, 11, 12, 19, 20, 21, 22, 23, 26, 30,  0,  0,  0,  1,  2,  3,  5,  6,  9, 12, 13, 20, 21, 22, 23, 26, 30,  0,  0,  0,  0,  1,  2,  3,  5,  6, 11, 12, 13, 16, 20, 21, 22, 23, 26, 30,  0,  0,  0,  1,  2,  3,  4,  5,  6,  9, 12, 13, 20, 21, 22, 23, 26, 30,  0,  0,  0,  1,  2,  3,  5,  6,  9, 12, 16, 19, 20, 21, 22, 23, 26, 30,  0,  0,  0,  1,  2,  3,  5,  6,  9, 12, 16, 19, 20, 21, 22, 23, 30,  0,  0,  0,  0,  1,  2,  3,  5,  6,  9, 12, 19, 20, 21, 22, 23, 30,  0,  0,  0,  0,  0,  2,  3,  5,  6,  9, 12, 13, 16, 19, 20, 21, 22, 23, 24, 26, 30,  0,  0,  2,  3,  5,  6, 12, 19, 20, 21, 22, 23, 30,  0,  0,  0,  0,  0,  0,  0,  2,  5,  6,  9, 12, 19, 20, 21, 22, 30,  0,  0,  0,  0,  0,  0,  0,  0,  2,  5,  6, 12, 19, 20, 21, 22, 23, 26, 28, 30,  0,  0,  0,  0,  0,  0,  1,  2,  5,  6, 12, 13, 19, 20, 21, 22, 24, 26, 30,  0,  0,  0,  0,  0,  2,  5,  6, 12, 19, 20, 21, 22, 23, 24, 26, 30,  0,  0,  0,  0,  0,  0,  1,  3,  9, 11, 12, 13, 16, 20, 21, 22, 23, 26, 30,  0,  0,  0,  0,  0,  1,  3,  9, 11, 13, 16, 20, 21, 22, 23, 26,  0,  0,  0,  0,  0,  0,  0,  1,  3,  9, 11, 13, 16, 21, 22, 23, 26,  0,  0,  0,  0,  0,  0,  0,  0,  1,  3,  9, 11, 13, 16, 21, 22, 23, 26,  0,  0,  0,  0,  0,  0,  0,  0,  1,  3,  5,  6,  9, 11, 13, 16, 20, 21, 22, 23, 26,  0,  0,  0,  0,  0,  1,  3,  9, 11, 13, 16, 20, 21, 22, 23, 26,  0,  0,  0,  0,  0,  0,  0,  1,  3,  6,  9, 11, 13, 16, 20, 21, 22, 23, 26,  0,  0,  0,  0,  0,  0,  1,  3,  5,  6,  9, 11, 13, 16, 20, 21, 22, 23, 26,  0,  0,  0,  0,  0,  1,  3,  5,  6,  9, 11, 16, 20, 21, 22, 23, 26,  0,  0,  0,  0,  0,  0,  1,  5,  6,  9, 11, 13, 16, 20, 21, 23, 26,  0,  0,  0,  0,  0,  0,  0,  1,  2,  3,  5,  6,  9, 11, 16, 20, 21, 22, 23, 26,  0,  0,  0,  0,  0,  1,  2,  3,  5,  6,  9, 11, 13, 16, 20, 21, 22, 23, 24, 26,  0,  0,  0,  2,  5,  6,  9, 12, 19, 20, 21, 23, 26, 30,  0,  0,  0,  0,  0,  0,  0,  2,  5,  6,  9, 12, 19, 20, 21, 23, 24, 30,  0,  0,  0,  0,  0,  0,  0,  2,  5,  6,  9, 12, 19, 20, 21, 22, 23, 24, 26, 30,  0,  0,  0,  0,  0,  2,  5,  6,  9, 12, 16, 19, 20, 21, 22, 24, 26, 30,  0,  0,  0,  0,  0,  1,  2,  5,  6, 12, 19, 20, 21, 23, 26, 30,  0,  0,  0,  0,  0,  0,  0,  1,  2,  3,  5,  6,  9, 12, 13, 16, 19, 20, 21, 22, 23, 26, 30,  0,  0,  1,  5,  6,  9, 12, 16, 19, 20, 21, 22, 23, 26, 30,  0,  0,  0,  0,  0,  2,  5,  6,  9, 12, 16, 19, 20, 21, 22, 26, 30,  0,  0,  0,  0,  0,  0,  1,  2,  5,  6, 12, 16, 19, 20, 21, 22, 23, 26, 30,  0,  0,  0,  0,  0,  1,  2,  5,  6,  9, 12, 16, 20, 21, 24, 26, 30,  0,  0,  0,  0,  0,  0,  1,  2,  5,  6,  9, 12, 16, 20, 21, 26, 30,  0,  0,  0,  0,  0,  0,  0,  1,  2,  5,  6,  9, 12, 16, 19, 20, 21, 26, 30,  0,  0,  0,  0,  0,  0,  2,  5,  6,  9, 12, 16, 20, 21, 26, 30,  0,  0,  0,  0,  0,  0,  0,  0,  1,  2,  5,  6,  9, 12, 16, 19, 20, 21, 26, 30,  0,  0,  0,  0,  0,  0,  2,  5,  6,  9, 12, 16, 20, 21, 24, 26, 30,  0,  0,  0,  0,  0,  0,  0,  2,  5,  6,  9, 12, 16, 20, 21, 26,  0,  0,  0,  0,  0,  0,  0,  0,  0,  2,  5,  6,  9, 12, 16, 20, 21, 24, 26, 30,  0,  0,  0,  0,  0,  0,  0,  2,  5,  6,  9, 16, 19, 20, 21, 24, 26, 28, 30,  0,  0,  0,  0,  0,  0,  2,  5,  6,  9, 16, 20, 21, 24, 26,  0,  0,  0,  0,  0,  0,  0,  0,  0,  2,  5,  6,  9, 16, 20, 21, 24, 26, 30,  0,  0,  0,  0,  0,  0,  0,  0,  2,  5,  6,  9, 16, 20, 21, 26, 30,  0,  0,  0,  0,  0,  0,  0,  0,  0,  2,  5,  6,  9, 16, 20, 21, 24,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  2,  5,  6,  9, 16, 20, 21, 26,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  2,  5,  6,  9, 16, 20, 21, 26,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  5,  6, 12, 19, 20, 21, 30,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  2,  5,  6, 12, 19, 20, 21, 23, 30,  0,  0,  0,  0,  0,  0,  0,  0,  0,  2,  5,  6, 12, 19, 20, 21, 30,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  5,  9, 12, 19, 20, 21, 30,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  2,  5, 12, 20, 21, 30,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  2,  5, 12, 19, 20, 24, 30,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  2,  5, 12, 19, 20, 21, 23, 24, 30,  0,  0,  0,  0,  0,  0,  0,  0,  0,  1,  2,  5,  6,  9, 11, 16, 20, 21, 22, 23, 24, 26,  0,  0,  0,  0,  0,  1,  2,  3,  5,  6,  9, 11, 16, 20, 21, 24, 26,  0,  0,  0,  0,  0,  0,  1,  2,  3,  5,  6,  9, 11, 16, 20, 21, 23, 24, 26,  0,  0,  0,  0,  0,  1,  2,  3,  5,  6,  9, 11, 16, 20, 21, 23, 24, 26,  0,  0,  0,  0,  0,  1,  2,  5,  6,  9, 11, 16, 20, 21, 23, 24, 26,  0,  0,  0,  0,  0,  0,  1,  2,  4,  5,  6,  9, 11, 16, 20, 21, 23, 24, 26,  0,  0,  0,  0,  0,  1,  2,  5,  6,  9, 11, 16, 20, 21, 23, 24, 26,  0,  0,  0,  0,  0,  0,  1,  2,  5,  6,  9, 11, 16, 20, 21, 24, 26,  0,  0,  0,  0,  0,  0,  0,  1,  2,  4,  5,  6,  9, 11, 16, 20, 21, 24, 26, 30,  0,  0,  0,  0,  0,  1,  2,  5,  6,  9, 11, 16, 20, 21, 24, 26,  0,  0,  0,  0,  0,  0,  0,  1,  2,  5,  6,  9, 11, 16, 20, 21, 24, 26,  0,  0,  0,  0,  0,  0,  0,  1,  2,  4,  5,  6,  9, 11, 16, 20, 21, 24, 26,  0,  0,  0,  0,  0,  0,  1,  2,  4,  5,  6,  9, 16, 20, 21, 24, 26,  0,  0,  0,  0,  0,  0,  0,  1,  2,  4,  5,  6,  9, 16, 18, 20, 21, 24, 26, 28,  0,  0,  0,  0,  0,  1,  2,  4,  5,  6,  9, 16, 19, 20, 21, 24, 26, 30,  0,  0,  0,  0,  0,  1,  2,  4,  5,  6,  9, 16, 19, 20, 21, 24, 26, 28,  0,  0,  0,  0,  0,  1,  2,  4,  5,  6,  9, 16, 20, 21, 24, 26, 28, 30,  0,  0,  0,  0,  0,  2,  4,  5,  6,  9, 16, 18, 19, 20, 21, 24, 26, 28, 30,  0,  0,  0,  0,  2,  4,  5,  6,  7,  9, 12, 16, 19, 20, 21, 24, 26, 28, 30,  0,  0,  0,  2,  4,  5,  6,  7,  9, 12, 16, 20, 21, 24, 26, 28, 30,  0,  0,  0,  0,  2,  4,  5,  6,  7,  9, 12, 16, 18, 19, 20, 21, 24, 26, 28, 30,  0,  0,  2,  4,  5,  6,  7,  9, 12, 16, 18, 19, 20, 21, 24, 26, 28, 30,  0,  0,  2,  4,  5,  6,  9, 12, 16, 19, 20, 21, 24, 26, 28,  0,  0,  0,  0,  0,  2,  4,  5,  6,  9, 12, 16, 18, 19, 20, 21, 24, 28, 30,  0,  0,  0,  0,  2,  4,  5,  6,  9, 12, 16, 18, 19, 20, 21, 24, 26, 28, 30,  0,  0,  0,  2,  4,  5,  6,  9, 12, 16, 18, 19, 20, 21, 24, 26, 28, 30,  0,  0,  0,  2,  4,  5,  6,  9, 12, 16, 18, 19, 20, 24, 26, 28, 30,  0,  0,  0,  0,  2,  4,  5,  6,  7,  9, 12, 16, 18, 19, 20, 24, 28, 30,  0,  0,  0,  0,  2,  4,  5,  6,  7,  9, 12, 16, 18, 19, 20, 21, 24, 26, 28, 30,  0,  0,  2,  4,  5,  6,  7,  9, 12, 16, 18, 19, 20, 24, 26, 28, 30,  0,  0,  0,  2,  4,  5,  6,  7,  9, 12, 18, 19, 20, 24, 28, 30,  0,  0,  0,  0,  0,  2,  4,  5,  6,  7,  9, 12, 18, 19, 20, 24, 26, 28, 30,  0,  0,  0,  0,  2,  4,  5,  6,  7, 12, 18, 19, 20, 24, 26, 28, 30,  0,  0,  0,  0,  0,  2,  4,  5,  7,  9, 12, 18, 19, 20, 24, 26, 28, 30,  0,  0,  0,  0,  0,  2,  4,  5,  9, 12, 18, 19, 20, 24, 28, 30,  0,  0,  0,  0,  0,  0,  0,  2,  4,  5,  6,  7,  9, 12, 18, 19, 20, 24, 28, 30,  0,  0,  0,  0,  0,  2,  4,  5,  7,  9, 12, 18, 19, 20, 24, 26, 28, 30,  0,  0,  0,  0,  0,  2,  4,  5,  7,  9, 12, 18, 19, 20, 24, 28, 30,  0,  0,  0,  0,  0,  0,  2,  4,  5,  7,  9, 12, 18, 19, 20, 24, 28, 30,  0,  0,  0,  0,  0,  0,  2,  4,  5,  6,  7, 12, 18, 19, 20, 24, 28, 30,  0,  0,  0,  0,  0,  0,  2,  4,  5,  7,  9, 12, 18, 19, 20, 24, 26, 28, 30,  0,  0,  0,  0,  0,  2,  4,  5,  6,  7,  9, 12, 18, 19, 20, 24, 28, 30,  0,  0,  0,  0,  0,  2,  4,  5,  7,  9, 12, 18, 19, 20, 24, 30,  0,  0,  0,  0,  0,  0,  0,  2,  4,  5,  7,  9, 12, 18, 19, 20, 24, 28, 30,  0,  0,  0,  0,  0,  0,  2,  4,  5,  7, 12, 18, 19, 20, 24, 28, 30,  0,  0,  0,  0,  0,  0,  0,  2,  4,  5,  7,  9, 12, 18, 19, 20, 24, 28, 30,  0,  0,  0,  0,  0,  0,  4,  5,  7, 18, 19, 20, 24, 26, 28, 30,  0,  0,  0,  0,  0,  0,  0,  0,  4,  5,  7, 12, 18, 19, 20, 24, 30,  0,  0,  0,  0,  0,  0,  0,  0,  0,  4,  5,  7,  9, 18, 19, 20, 24, 30,  0,  0,  0,  0,  0,  0,  0,  0,  0,  4,  5,  7, 18, 19, 20, 24, 30,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  2,  4,  5,  7, 18, 19, 24, 30,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  4,  7, 12, 18, 19, 24, 30,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  4,  5,  7,  9, 12, 18, 19, 20, 24, 30,  0,  0,  0,  0,  0,  0,  0,  0,  2,  4,  5,  6,  7, 12, 18, 19, 24, 30,  0,  0,  0,  0,  0,  0,  0,  0,  4,  5,  7, 18, 19, 20, 24, 30,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  2,  4,  5,  6,  7,  9, 18, 19, 20, 24, 30,  0,  0,  0,  0,  0,  0,  0,  2,  4,  7, 18, 19, 20, 24, 30,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  2,  4,  5,  6,  7, 12, 18, 19, 20, 24, 30,  0,  0,  0,  0,  0,  0,  0,  4,  5,  7, 18, 19, 24, 30,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  1,  5,  6,  9, 11, 16, 21, 26,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  1,  3,  5,  9, 11, 16, 21, 26,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  1,  5,  9, 11, 16, 21, 26,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  1,  3,  6,  9, 11, 16, 21, 26,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  1,  5,  6,  9, 11, 16, 21, 23, 26,  0,  0,  0,  0,  0,  0,  0,  0,  0,  1,  6,  9, 16, 21, 26,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  1,  5,  6,  9, 16, 20, 21, 26,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  1,  5,  6,  9, 11, 16, 20, 21, 26,  0,  0,  0,  0,  0,  0,  0,  0,  0,  1,  5,  6,  9, 11, 16, 20, 21, 26,  0,  0,  0,  0,  0,  0,  0,  0,  0,  1,  5,  6,  9, 16, 21, 26,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  1,  2,  5,  6,  9, 16, 20, 21, 26,  0,  0,  0,  0,  0,  0,  0,  0,  0,  2,  5,  6,  9, 16, 20, 21, 26,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  2,  4,  5,  6,  9, 12, 16, 20, 21, 24, 26,  0,  0,  0,  0,  0,  0,  0,  2,  5,  6,  9, 16, 20, 21, 26,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  2,  4,  5,  6,  9, 12, 16, 20, 21, 24, 26, 28,  0,  0,  0,  0,  0,  0,  2,  5,  6,  9, 16, 20,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  2,  4,  5,  6,  9, 12, 16, 19, 20, 24, 26, 28, 30,  0,  0,  0,  0,  0,  2,  4,  5,  6,  9, 12, 16, 19, 20, 21, 28,  0,  0,  0,  0,  0,  0,  0,  2,  4,  5,  6, 16, 20, 24, 28,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  2,  4,  5,  6, 12, 18, 19, 20, 24, 28, 30,  0,  0,  0,  0,  0,  0,  0,  2,  4,  5,  6, 12, 18, 19, 20, 24, 28, 30,  0,  0,  0,  0,  0,  0,  0,  2,  4,  5,  6,  9, 12, 19, 20, 28, 30,  0,  0,  0,  0,  0,  0,  0,  0,  2,  4,  5, 12, 18, 19, 20, 24, 28,  0,  0,  0,  0,  0,  0,  0,  0,  0,  2,  4,  5,  6, 18, 19, 20, 24, 28, 30,  0,  0,  0,  0,  0,  0,  0,  0,  2,  4,  5,  6, 12, 18, 19, 20, 24, 28, 30,  0,  0,  0,  0,  0,  0,  0,  2,  4,  5, 12, 18, 19, 20, 24, 28, 30,  0,  0,  0,  0,  0,  0,  0,  0,  2,  4,  5, 12, 18, 19, 20, 24, 28, 30,  0,  0,  0,  0,  0,  0,  0,  0,  2,  4, 12, 18, 19, 20, 24, 28, 30,  0,  0,  0,  0,  0,  0,  0,  0,  0,  2,  4,  7, 12, 18, 19, 20, 24, 28, 30,  0,  0,  0,  0,  0,  0,  0,  0,  2,  4,  5,  7, 12, 18, 19, 20, 24, 28, 30,  0,  0,  0,  0,  0,  0,  0,  4,  7, 12, 18, 19, 20, 24, 28, 30,  0,  0,  0,  0,  0,  0,  0,  0,  0,  4,  7, 12, 18, 19, 20, 24, 30,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  4,  7, 18, 19, 24, 30,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  4,  7, 12, 18, 19, 24, 28, 30,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  4,  5,  7, 18, 19, 20, 24, 30,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  4,  7, 18, 19, 24, 28, 30,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  4,  7, 18, 19, 24, 30,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  7, 18, 19, 24,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  4,  7, 18, 19, 24,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  4,  7, 18, 24,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  7, 18, 19, 24,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  7, 18, 24,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  7, 18, 19, 24,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  7, 19, 24,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  7, 19, 24,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  7, 24,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  7, 12, 18, 19, 24, 30,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  7, 12, 18, 19, 24, 30,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  4,  7, 12, 18, 19, 24, 30,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  4,  7, 12, 18, 19, 24, 30,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  4,  7, 12, 18, 19, 20, 24, 30,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  4,  7, 12, 18, 19, 20, 24, 30,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  4,  5, 12, 18, 19, 20, 24, 28, 30,  0,  0,  0,  0,  0,  0,  0,  0,  0,  4, 12, 18, 19, 20, 24, 28, 30,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  2,  4, 12, 18, 19, 20, 24, 28, 30,  0,  0,  0,  0,  0,  0,  0,  0,  0,  2,  4, 12, 18, 19, 20, 24, 30,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  2,  4, 12, 19, 20, 24, 30,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  2,  4,  5, 12, 19, 20, 24, 28, 30,  0,  0,  0,  0,  0,  0,  0,  0,  0,  2,  4,  5, 12, 19, 20, 24, 30,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  2,  4, 12, 19, 20, 24, 30,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  2,  4, 12, 18, 19, 20, 24, 28, 30,  0,  0,  0,  0,  0,  0,  0,  0,  0,  2,  4,  5, 12, 18, 19, 20, 24, 30,  0,  0,  0,  0,  0,  0,  0,  0,  0,  4,  5, 12, 18, 19, 20, 24, 28, 30,  0,  0,  0,  0,  0,  0,  0,  0,  0,  2,  4,  5, 12, 18, 19, 20, 24, 28, 30,  0,  0,  0,  0,  0,  0,  0,  0,  2,  4,  5, 12, 18, 19, 20, 24, 28, 30,  0,  0,  0,  0,  0,  0,  0,  0,  2,  4,  5,  6,  9, 12, 19, 20, 21, 24, 26, 28, 30,  0,  0,  0,  0,  0,  2,  4,  5,  6, 12, 19, 20, 24, 28, 30,  0,  0,  0,  0,  0,  0,  0,  0,  2,  4,  5,  6,  9, 12, 19, 20, 28, 30,  0,  0,  0,  0,  0,  0,  0,  0,  1,  2,  5,  6,  9, 12, 16, 19, 20, 21, 24, 26, 30,  0,  0,  0,  0,  0,  1,  2,  5,  6,  9, 16, 20, 21, 26,  0,  0,  0,  0,  0,  0,  0,  0,  0,  1,  3,  5,  6,  9, 11, 16, 20, 21, 23, 26,  0,  0,  0,  0,  0,  0,  0,  1,  2,  5,  6,  9, 11, 16, 20, 21, 23, 26,  0,  0,  0,  0,  0,  0,  0,  1,  5,  6,  9, 11, 16, 20, 21, 26,  0,  0,  0,  0,  0,  0,  0,  0,  0,  1,  3,  5,  6,  9, 11, 16, 20, 21, 22, 23, 26,  0,  0,  0,  0,  0,  0,  1,  5,  6,  9, 11, 16, 20, 21, 23, 26,  0,  0,  0,  0,  0,  0,  0,  0,  1,  3,  5,  6,  9, 11, 16, 20, 21, 23, 26,  0,  0,  0,  0,  0,  0,  0,  1,  3,  9, 11, 12, 13, 21, 22, 23, 26,  0,  0,  0,  0,  0,  0,  0,  0,  1,  3,  9, 11, 12, 13, 16, 20, 21, 23, 26,  0,  0,  0,  0,  0,  0,  0,  1,  5,  9, 16, 20, 21, 23, 26, 30,  0,  0,  0,  0,  0,  0,  0,  0,  0,  1,  3,  6,  9, 11, 16, 20, 21, 22, 23, 26,  0,  0,  0,  0,  0,  0,  0,  1,  3,  9, 11, 13, 16, 21, 22, 23, 26,  0,  0,  0,  0,  0,  0,  0,  0,  1,  3,  9, 11, 13, 16, 21, 22, 23, 26,  0,  0,  0,  0,  0,  0,  0,  0,  1,  3,  9, 11, 13, 21, 22, 23, 26,  0,  0,  0,  0,  0,  0,  0,  0,  0,  1,  3,  9, 11, 13, 21, 22, 23, 26,  0,  0,  0,  0,  0,  0,  0,  0,  0,  1,  3, 11, 13, 21, 22, 23, 26,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  1,  3,  9, 11, 13, 21, 22, 23, 26,  0,  0,  0,  0,  0,  0,  0,  0,  0,  1,  3,  9, 11, 13, 21, 22, 23, 26,  0,  0,  0,  0,  0,  0,  0,  0,  0,  1,  3, 11, 13, 21, 22, 23, 26,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  1,  3, 11, 13, 21, 22, 26,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  1,  3, 11, 13, 21, 22, 23,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  1,  3, 11, 13, 21, 22, 23, 26,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  1,  3, 11, 13, 21, 22, 23,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  1,  3, 11, 13, 22, 23,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  1,  3, 11, 13, 22, 23,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  1,  3, 11, 13, 22, 23,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  1,  3, 11, 13, 21, 22, 23,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  1,  3, 11, 13, 22, 23,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  1,  3, 11, 13, 21, 22, 23,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  1,  3, 11, 13, 21, 22, 23,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  1,  3, 11, 13, 21, 22, 23, 26,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  1,  3, 11, 13, 21, 22, 23,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  1,  3, 11, 13, 21, 22, 23, 26,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  1,  3,  9, 13, 21, 22, 23, 26,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  1, 11, 13, 21, 22, 23, 26,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  1,  3, 13, 21, 22, 23, 26,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  1, 12, 21, 22, 23, 26,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  1,  9, 13, 21, 22, 23, 26, 30,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 13, 20, 21, 22, 23, 26, 30,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  2,  5, 12, 20, 21, 23, 30,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 12, 20, 21, 23, 30,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  1,  5, 12, 19, 20, 21, 23, 26, 30,  0,  0,  0,  0,  0,  0,  0,  0,  0,  2,  5, 12, 19, 20, 21, 23, 30,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0};    }
    @ConstArray
    public static class Rssi0 {
        public final static byte[]  data = { 30, 38, 18, 29,  0, 39, 18,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 23, 29, 34, 34,  8, 33, 26,  7,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 26, 26, 34, 28,  0, 33, 18,  8,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 27, 30, 28,  0, 34, 19, 35, 27,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 26, 33, 32,  6, 24,  0, 19, 32, 22,  7,  0,  0,  0,  0,  0,  0,  0,  0, 32, 21, 36, 30,  7, 14, 31, 28, 10,  0,  0,  0,  0,  0,  0,  0,  0,  0, 11, 40, 29,  0, 21, 10, 37, 26,  7,  0,  0,  0,  0,  0,  0,  0,  0,  0, 32, 29, 32,  9, 24, 16, 22, 18,  9,  9,  0,  0,  0,  0,  0,  0,  0,  0, 32, 26, 34,  9, 31,  0, 11, 32, 21,  9,  0,  0,  0,  0,  0,  0,  0,  0, 29, 28, 30, 36,  0, 18, 28, 31, 13,  8,  0,  0,  0,  0,  0,  0,  0,  0, 28, 19, 23,  0, 29,  0, 15, 20, 35,  0, 11,  0,  0,  0,  0,  0,  0,  0, 24, 20,  7, 11,  8, 36,  8, 16, 30, 30, 10,  0,  0,  0,  0,  0,  0,  0, 30, 21,  8, 14, 11, 26,  0, 10, 30, 29,  0,  0,  0,  0,  0,  0,  0,  0, 36, 23, 11, 23, 11, 26, 14, 20, 13, 27,  0,  9,  0,  0,  0,  0,  0,  0, 33, 20, 23, 11, 25, 12, 26, 20, 26, 16,  9,  0,  0,  0,  0,  0,  0,  0, 34, 20,  0, 18, 10, 27, 10, 21, 22, 28, 11,  9,  0,  0,  0,  0,  0,  0, 30, 15,  9, 20, 10, 24, 12, 24, 28, 36, 16,  7,  0,  0,  0,  0,  0,  0, 21, 17, 11, 17,  7, 22, 12, 30, 21, 23, 20, 15,  0,  0,  0,  0,  0,  0, 30, 11, 16, 20, 16, 20,  7,  7,  7, 30, 18, 18, 24, 17,  0,  0,  0,  0, 30, 11, 10, 12,  0, 11,  6,  7, 11, 38, 22, 23,  8, 20,  0,  0,  0,  0, 23,  8,  9,  9, 15, 22,  7, 13, 38, 18, 28, 25, 23,  0,  0,  0,  0,  0, 22, 18, 16, 12,  7, 21,  6, 11, 36, 13, 27, 16, 22,  0,  0,  0,  0,  0, 27, 12,  9,  8, 15,  9, 16, 26, 14, 26,  8, 21,  0,  0,  0,  0,  0,  0, 21, 13,  0,  9, 15,  0, 13, 34,  9, 30, 24, 20,  0,  0,  0,  0,  0,  0, 12, 10,  0,  9, 11, 12, 16,  8,  7, 30,  9, 12, 17, 14,  0,  0,  0,  0, 19, 13,  7,  0, 11,  9, 19,  9, 11, 30,  9, 20, 18, 10,  0,  0,  0,  0, 19,  7, 10,  8, 14,  0, 18, 13,  0,  8, 13, 20, 10, 16, 20, 16,  0,  0,  0,  0,  0,  0,  0,  0, 14, 16,  8,  9, 12, 24, 18, 24, 20, 25,  0,  0,  8,  9,  0,  0, 13, 11,  8, 13, 27,  8, 14,  8,  0,  0,  0,  0,  0,  0, 20,  9, 12,  9, 21,  7,  0,  9,  9, 30, 12, 13, 17,  9,  0,  0,  0,  0, 11,  9,  8, 13, 10,  7,  7, 22,  7, 20, 17, 12, 13, 12, 24,  0,  0,  0, 12, 10, 12, 13, 10,  0, 19,  9, 31, 18,  9,  7, 16, 23,  0,  0,  0,  0,  7, 12,  9, 22, 13,  0, 16,  8,  6, 25, 22, 10, 12,  9, 19,  0,  0,  0,  9, 14,  9,  6, 22, 21, 11, 20,  0, 27, 21, 10,  0, 17, 22,  0,  0,  0,  7,  8, 11, 28, 17,  7, 19,  7, 10, 11,  8, 18,  0,  0, 19,  0,  0,  0,  0, 22,  9, 20, 23,  8, 17, 12, 15, 23, 17, 12,  8, 11,  0,  0,  0,  0,  0, 13,  8, 19, 18,  7,  0, 15, 18, 20, 12,  0, 21,  0,  0,  0,  0,  0,  8,  9, 17, 15,  7, 24,  0,  0,  8, 35, 13, 13,  0,  7,  9, 24,  0,  0, 13,  8, 22, 14, 22, 12, 23, 10,  0,  8, 27,  0,  0,  0,  0,  0,  0,  0,  7, 12, 15,  7, 27, 10, 30, 15,  9, 32,  0,  0,  0,  0,  0,  0,  0,  0,  0, 12,  0, 27,  0, 28, 16,  6,  0,  6,  7, 29,  0,  0,  0,  0,  0,  0,  8, 12, 12, 10, 24,  0, 12, 32, 11,  9,  8,  9, 26,  0,  0,  0,  0,  0, 16, 16, 12, 32,  8, 30, 12,  7,  7, 12,  7, 24,  0,  0,  0,  0,  0,  0, 20,  0, 18,  9,  0, 17,  6, 10, 22, 17, 32, 28,  8,  0,  0,  0,  0,  0, 30, 10, 14, 15, 14,  7,  7, 34, 13, 30, 20,  0,  0,  0,  0,  0,  0,  0, 31, 12, 23, 22, 17,  8, 30, 10, 20, 24,  0,  0,  0,  0,  0,  0,  0,  0, 18, 11, 18, 21, 10,  9, 31, 13, 26, 26,  0,  0,  0,  0,  0,  0,  0,  0, 32, 21,  9,  0, 19, 16, 10, 10,  9, 31,  8, 23, 25,  0,  0,  0,  0,  0, 38, 12, 28, 20, 11,  0,  0, 40,  0, 19, 28,  0,  0,  0,  0,  0,  0,  0, 38, 12,  7, 16, 22,  0, 14, 11, 32, 11, 22, 26,  0,  0,  0,  0,  0,  0, 20, 11,  9, 13, 30, 24,  9,  8,  8, 29,  7, 12, 27,  0,  0,  0,  0,  0, 21,  9,  8,  0, 23, 22, 15, 12, 31,  7, 12, 31,  0,  0,  0,  0,  0,  0, 23,  9,  7, 27, 23,  8, 11, 10, 33, 18, 30,  0,  0,  0,  0,  0,  0,  0, 32,  7, 11, 19,  0, 26, 18, 26,  0, 29,  0, 17, 36,  0,  0,  0,  0,  0, 21,  0, 11, 20, 20, 30, 16,  9, 22, 13, 37,  7, 16, 10, 35,  0,  0,  0, 20, 15, 14,  0, 17, 17, 31, 12,  6, 10, 16,  0,  0,  0,  0,  0,  0,  0,  0, 22, 11,  0, 24, 10, 24,  8,  7,  8, 26,  0,  0,  0,  0,  0,  0,  0,  7, 24, 18,  6, 11, 13, 17, 17,  6,  7,  7, 10, 21,  0,  0,  0,  0,  0, 13, 24, 11,  0, 12,  0, 11, 19, 15,  9,  7,  7, 13,  0,  0,  0,  0,  0,  9,  8, 24,  9, 15, 10, 30, 18, 12,  7, 17,  0,  0,  0,  0,  0,  0,  0, 16,  8,  0, 24, 24,  9, 19,  9,  0,  9, 20, 10, 14, 11,  9, 23,  0,  0,  8, 13,  9,  9, 21,  6, 10, 20, 17,  0,  7, 10, 25,  0,  0,  0,  0,  0,  0, 25, 17, 10, 19, 11,  9, 35, 15,  9, 10, 22,  0,  0,  0,  0,  0,  0,  7, 11, 27, 15,  8, 13,  9, 26, 22,  6,  7, 10, 18,  0,  0,  0,  0,  0,  7, 12, 31, 20,  0, 13,  8, 32, 19,  7, 18, 16,  0,  0,  0,  0,  0,  0,  9, 20, 20, 16, 13, 16,  7, 39, 12, 18, 10,  0,  0,  0,  0,  0,  0,  0,  0, 23, 13, 14, 20, 10,  0,  8, 30, 17, 18, 10,  0,  0,  0,  0,  0,  0, 13, 26, 21,  8, 13,  7, 29, 20, 16,  0,  0,  0,  0,  0,  0,  0,  0,  0,  7, 17, 22, 22,  9,  0, 14,  8, 33, 13, 11, 11,  0,  0,  0,  0,  0,  0, 14, 35, 26, 17,  9, 11, 35, 12,  8, 12, 10,  0,  0,  0,  0,  0,  0,  0, 15, 25, 34, 15,  8, 13, 33, 11,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 22, 38, 30, 17,  7, 10, 34, 10,  8, 15,  8,  0,  0,  0,  0,  0,  0,  0, 14, 31, 34, 19,  9,  8, 30, 15, 12, 13,  7,  9,  0,  0,  0,  0,  0,  0, 19, 32, 34, 22, 20, 10,  8, 11, 17,  0,  0,  0,  0,  0,  0,  0,  0,  0, 19, 35, 37, 12, 12, 23,  9,  8, 11,  7,  0,  0,  0,  0,  0,  0,  0,  0, 13, 45, 36, 22, 16, 27,  9,  0,  7,  0,  0,  0,  0,  0,  0,  0,  0,  0, 22, 39, 26, 12, 20, 23,  7,  6,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 14, 36, 40, 16, 23, 23,  7,  9,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 14, 31, 25, 20, 17, 18,  8,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 10, 10, 21,  6, 20,  0, 20,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 13, 12, 11, 25,  0, 25, 10,  7, 23,  0,  0,  0,  0,  0,  0,  0,  0,  0,  9,  7, 12, 23,  0, 31, 11, 22,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 14,  7, 26, 12, 25,  0, 22,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  9,  0, 20, 23, 10, 24,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  9,  9, 24, 10, 25,  7, 24,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 10, 12, 13,  0, 23,  7,  6,  0, 12,  0,  0,  0,  0,  0,  0,  0,  0,  0, 22,  8, 10,  0, 26,  0, 20, 12, 15,  6,  6, 15, 15,  0,  0,  0,  0,  0, 20,  7, 10,  0, 12, 26, 13, 23, 15, 25, 13, 19,  0,  0,  0,  0,  0,  0, 21,  6,  9,  9, 10, 18, 19, 19,  9, 18,  9, 11, 29,  0,  0,  0,  0,  0, 18, 12,  7, 23, 15, 35, 10, 16, 11, 30,  0,  8, 31,  0,  0,  0,  0,  0, 14,  0, 20, 12, 34,  8, 23, 10, 25, 11, 10, 24,  0,  0,  0,  0,  0,  0, 11, 17,  0, 20, 10, 30, 12, 25, 11, 19,  9, 14, 36,  0,  0,  0,  0,  0,  9, 15, 22, 26, 32,  0, 21, 15, 30,  0,  9, 27,  0,  0,  0,  0,  0,  0, 19, 10, 12, 22, 32,  7, 22, 20, 22, 13, 33,  0,  0,  0,  0,  0,  0,  0, 14, 11,  0, 24, 15, 45,  8, 32, 13, 22, 17, 26,  7,  0,  0,  0,  0,  0, 22, 11, 24, 20, 33,  8, 24, 18, 32, 17, 32,  0,  0,  0,  0,  0,  0,  0,  9, 12, 20, 24, 37,  0, 32, 11, 24, 14, 28,  0,  0,  0,  0,  0,  0,  0,  8, 12,  8, 29, 22, 25,  0, 32, 13, 23, 12, 27,  0,  0,  0,  0,  0,  0, 12, 11,  9, 19, 21, 30, 20, 21, 23, 17, 26,  0,  0,  0,  0,  0,  0,  0, 13, 23, 10, 24, 24, 20, 11,  6, 17, 20, 10, 20,  0,  0,  0,  0,  0,  0,  0, 22,  0, 30, 26, 24, 23,  9, 12, 12, 11, 16,  7,  0,  0,  0,  0,  0,  9, 20, 12, 26, 34, 19, 24,  8, 28, 13, 11, 22,  6,  0,  0,  0,  0,  0,  0, 11, 10, 31, 28, 22, 11, 28, 11,  7, 15,  8,  9,  0,  0,  0,  0,  0, 24,  7, 38, 36, 26,  9,  6,  8, 31, 12,  7, 19,  8,  9,  0,  0,  0,  0,  9,  7, 31, 18,  7, 20,  0, 21,  8, 23, 16, 16,  9, 14, 11,  0,  0,  0, 15,  0, 39,  9,  9, 22, 12, 20, 26, 12, 15, 11, 10, 16,  0,  0,  0,  0, 11,  0, 38, 19, 11, 11, 11, 10,  7, 14, 35, 11, 21, 17,  7, 11,  0,  0, 26,  0, 39, 18,  7, 20, 12, 18,  7, 18, 33, 17, 18, 12, 11,  0,  0,  0, 19,  9, 37, 22, 16,  9,  8, 17, 29,  0, 20, 10, 15,  0,  0,  0,  0,  0, 31,  0, 28, 19, 12, 11,  7,  8,  7, 22, 14, 20,  0,  0,  0,  0,  0,  0, 26,  9, 24, 22, 13, 10,  9,  0, 10, 34, 11, 18,  9, 18, 11,  0,  0,  0, 24,  9,  9, 19,  0,  0,  0,  0, 18, 32,  0, 25, 11, 11, 14,  0,  0,  0, 25, 20, 17, 18, 11,  7, 12,  7, 24, 34, 21,  9, 15, 20,  0,  0,  0,  0, 23, 16, 29, 10,  9,  9, 14,  9,  7, 11, 38, 10, 18, 22,  0,  0,  0,  0, 33, 18, 13, 14, 10,  8, 15,  8,  0, 10, 39,  0, 19, 10, 11, 22,  0,  0, 33, 12, 14,  9,  8,  9, 12,  0,  0, 15, 36, 22,  0, 10, 22,  0,  0,  0, 23, 24, 24,  8,  9, 12, 10, 13, 22, 24, 19, 11,  0,  0,  0,  0,  0,  0, 26, 15, 28,  0,  9,  8, 21,  0, 20, 26, 12,  9, 19, 16,  0,  0,  0,  0, 18, 18, 14,  0, 10, 20,  0,  0, 25, 13,  9, 23, 25,  0,  0,  0,  0,  0, 22, 24, 18, 10,  0,  7, 14, 23, 23, 22,  9, 25, 17,  0,  0,  0,  0,  0, 20, 31, 16,  9, 18, 18, 13, 17, 22, 24, 17,  0,  0,  0,  0,  0,  0,  0, 22, 18, 15, 10, 11,  0, 22, 15, 17, 24, 11,  0, 13,  0,  0,  0,  0,  0, 17, 23, 16, 11, 11, 18, 26, 23, 12, 32,  8, 20, 24,  0,  0,  0,  0,  0, 11, 32, 17, 10, 10, 17, 22, 26, 10,  9, 17, 28,  0,  0,  0,  0,  0,  0, 19, 17, 10, 19,  0, 15, 26, 26, 13, 24, 10, 16,  0,  0,  0,  0,  0,  0, 11, 13,  9, 11,  8,  8, 22, 30, 11, 30,  8, 23,  0,  0,  0,  0,  0,  0,  6, 24, 15, 13, 10, 10, 26, 28,  9, 35,  7,  0,  0,  0,  0,  0,  0,  0,  9, 10, 17,  7, 10,  0,  0, 26, 19,  9, 29, 14, 19,  0,  0,  0,  0,  0, 12, 20, 10,  8,  8, 19, 24, 28, 17, 38, 10,  0,  0,  0,  0,  0,  0,  0,  0, 10, 18, 20,  8,  0, 23, 16, 12, 32,  7, 15,  0,  0,  0,  0,  0,  0,  8, 23,  9, 20, 10, 22, 24, 15, 40,  8, 13,  0,  0,  0,  0,  0,  0,  0,  0, 13, 11, 19,  0, 16, 21, 27,  8, 41,  9,  8,  0,  0,  0,  0,  0,  0, 20, 16, 23, 26, 23, 18, 44,  6,  8, 10,  0,  0,  0,  0,  0,  0,  0,  0, 13,  7, 15, 11, 19, 19,  7, 39, 16,  0,  0,  0,  0,  0,  0,  0,  0,  0,  9,  8, 26, 11, 25, 26, 14, 46, 12,  0,  0,  0,  0,  0,  0,  0,  0,  0, 13, 11, 26, 19, 26,  6, 32, 10,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  7,  8,  7, 18,  9, 12, 36,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  8, 28,  0, 12, 22, 35,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 14,  7, 31,  0,  9, 10, 24,  7, 19,  7,  0,  0,  0,  0,  0,  0,  0,  0,  0,  7,  9,  8, 24,  6, 18, 11, 22,  7,  0,  0,  0,  0,  0,  0,  0,  0, 11,  9, 23, 14, 19, 10, 27,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  8,  0,  9,  7, 27,  0,  9, 10,  7, 30, 13,  0,  0,  0,  0,  0,  0,  0,  0,  0, 28, 14, 15,  0, 22,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  7,  7, 10, 17,  0,  8, 11,  9, 23,  7,  0,  0,  0,  0,  0,  0,  0,  7,  6, 17, 15, 13, 28,  7,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  9, 11, 11, 20, 17, 17, 22, 18,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 19,  7,  0, 24,  9, 18, 11, 18,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 12,  7, 23,  7, 12, 13, 19,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 14,  7,  8, 21,  8, 19, 12, 17,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 18, 13, 14, 39,  0, 24, 20,  7, 18,  0,  0,  0,  0,  0,  0,  0,  0,  0, 14, 10, 36, 22, 20, 19,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 12, 35, 20,  6, 20, 24,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 16, 21, 11, 46,  8, 27, 10, 23, 26,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 15, 13, 43,  7, 25,  7,  9, 25,  0,  0,  0,  0,  0,  0,  0,  0,  0,  7,  7,  9, 38, 26, 12, 21,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  7, 19, 13, 37, 35, 15, 25, 22,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  9, 28, 34, 40, 11, 15, 16,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 10,  0, 36, 35, 19,  7, 23, 17, 10,  8, 14,  0,  0,  0,  0,  0,  0,  0,  9, 25, 43, 24, 18, 21,  7, 18,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 21,  8, 47, 31, 22,  0, 20, 21,  0, 13, 11,  8,  0,  0,  0,  0,  0,  0, 37, 38, 26, 13, 16, 21,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 36, 15, 39, 10,  8,  7,  0,  0, 25, 11,  7, 18, 11,  0,  0,  0,  0,  0, 49, 13, 27, 25, 11,  7, 13,  9, 25,  7, 17,  0,  0,  0,  0,  0,  0,  0, 45, 11, 34, 24,  8, 31, 10, 17,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 31, 11, 16, 14, 14,  9, 12, 34, 19, 27, 11,  0,  0,  0,  0,  0,  0,  0, 29, 26, 14, 12, 10,  7,  8, 24,  0, 28, 14,  0,  0,  0,  0,  0,  0,  0, 36, 16, 24,  8,  0, 11,  0, 27, 23,  0,  0,  0,  0,  0,  0,  0,  0,  0, 18, 27, 15, 18,  7, 23, 13, 15, 33,  0,  0,  0,  0,  0,  0,  0,  0,  0, 12, 35, 16,  7, 17, 11, 20, 10, 35, 19,  0,  0,  0,  0,  0,  0,  0,  0, 22, 30, 12,  0, 11, 10, 11, 15, 12, 28, 16,  0,  0,  0,  0,  0,  0,  0, 15, 42, 13, 12, 25, 21, 18, 22, 24, 18,  0,  0,  0,  0,  0,  0,  0,  0, 11, 45, 17,  0, 19, 13, 16, 15, 25, 12,  0,  0,  0,  0,  0,  0,  0,  0, 12, 38, 10, 16, 19, 12, 11, 16, 23,  0,  0,  0,  0,  0,  0,  0,  0,  0, 11, 28, 10, 15, 29, 20, 10, 31,  8, 23,  0,  0,  0,  0,  0,  0,  0,  0,  8, 20,  0,  0,  9, 41, 18,  0, 19, 18,  8,  0,  0,  0,  0,  0,  0,  0, 32,  0,  0, 39, 18,  0, 23, 15,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 20, 16,  0, 34, 25, 13, 29,  9,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 17, 21, 40, 21, 30, 10,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 22,  8,  7, 44, 15, 27,  8,  7,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 10,  9, 18, 26, 25,  7, 35, 12,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 18, 22, 26, 23, 20,  7,  8,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 14, 29, 14, 14, 24,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 38, 16, 13, 24,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 27, 25, 10, 26,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 45, 10, 13,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 25, 12,  8, 12,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 34, 11, 21,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 36,  8,  7, 23,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 11, 14, 18,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 11,  0, 16,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 29, 16,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  9, 11,  9, 23, 40, 11,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 18,  9,  9, 21, 38,  9,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  8, 10,  9, 18, 26, 44, 11,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 12, 18, 21, 11, 31, 47, 18,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 17, 22, 12, 29, 11, 36, 20,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  8, 10, 18, 21, 25,  8, 23, 25,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 14,  0, 14,  0, 38, 18, 21, 15, 31,  0,  0,  0,  0,  0,  0,  0,  0,  0, 22, 28, 12, 36,  9, 28, 15, 30,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  7, 15, 16,  9, 27, 18, 21,  8, 36,  0,  0,  0,  0,  0,  0,  0,  0,  0,  8, 16, 27,  8, 38, 20, 18, 29,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 11, 12, 32, 16, 15, 14, 39,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  8,  8,  0, 36, 31, 22, 13,  0, 43,  0,  0,  0,  0,  0,  0,  0,  0,  0,  9,  7,  8, 40, 22, 23, 16, 33,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  7,  9, 39, 27, 22, 20, 42,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  7, 19, 27, 17, 29, 18, 26, 11, 22,  0,  0,  0,  0,  0,  0,  0,  0,  0, 11, 11,  6, 13, 10, 37, 21, 27, 34,  0,  0,  0,  0,  0,  0,  0,  0,  0, 21, 12, 13, 12, 43, 18, 32,  8, 15,  0,  0,  0,  0,  0,  0,  0,  0,  0, 16, 11, 16, 20, 11, 16, 29, 23, 13, 28,  0,  0,  0,  0,  0,  0,  0,  0, 11, 12,  9, 30,  0, 30, 26, 24, 15, 19,  0,  0,  0,  0,  0,  0,  0,  0, 25, 11, 23, 11,  8,  0, 11, 53, 10, 15,  0,  8, 17,  0,  0,  0,  0,  0, 14,  7, 24,  9, 25, 10, 38, 10,  9, 25,  0,  0,  0,  0,  0,  0,  0,  0, 11,  8, 20,  7,  0, 18, 12, 38,  9, 13,  0,  0,  0,  0,  0,  0,  0,  0,  8, 16, 26, 26, 22,  0, 11,  0, 32, 22,  7, 19,  0,  0,  0,  0,  0,  0, 10,  9, 25, 23, 19, 12, 21, 31, 19,  0,  0,  0,  0,  0,  0,  0,  0,  0, 30,  9, 13,  8, 23, 17, 18,  0, 39,  0, 45,  0,  0,  0,  0,  0,  0,  0, 12,  6, 10, 13, 36,  0, 22,  7, 23,  0, 20,  0,  0,  0,  0,  0,  0,  0, 22, 11, 17, 30,  0, 20,  7, 42, 46,  0,  0,  0,  0,  0,  0,  0,  0,  0, 23, 11,  9, 12, 28, 17, 10,  0, 38,  0, 13, 31,  0,  0,  0,  0,  0,  0, 32, 12,  8, 23, 13, 12, 10, 47, 12, 34,  0,  0,  0,  0,  0,  0,  0,  0, 19,  9, 19,  0, 18, 18, 10,  0, 36,  0, 31,  0,  0,  0,  0,  0,  0,  0, 26,  8,  8,  8,  8,  9, 31, 13, 23, 24,  0,  0,  0,  0,  0,  0,  0,  0, 16,  8, 11,  9,  6,  0,  0,  8, 44, 20, 26,  0,  0,  0,  0,  0,  0,  0, 20,  0, 14, 10, 12, 32, 14, 20,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 23, 11,  7, 22, 19, 16,  9, 35,  0, 10, 11,  0,  0,  0,  0,  0,  0,  0, 42, 24, 19, 26,  0,  6, 31,  0, 18, 25,  0,  0,  0,  0,  0,  0,  0,  0, 44, 19, 13, 27,  0,  7, 34, 14, 16, 18,  0,  0,  0,  0,  0,  0,  0,  0, 45, 14, 12, 36, 10, 19,  7, 10, 23,  0,  0,  0,  0,  0,  0,  0,  0,  0, 22, 12, 12, 17, 11,  0,  7, 25, 17,  0,  0,  0,  0,  0,  0,  0,  0,  0, 24, 34, 44, 30, 13, 26, 15,  9,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 35, 37, 12, 44, 17, 18, 22, 14, 16,  0,  0,  0,  0,  0,  0,  0,  0,  0, 25, 25,  0, 39, 20, 19, 15,  8, 12,  0,  0,  0,  0,  0,  0,  0,  0,  0, 19, 41, 18, 12, 13, 18, 11,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 25, 35, 30, 18, 16, 18,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 21, 25, 20, 14, 12, 22,  8,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 23, 28, 21,  0,  7, 23,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 19, 30, 17, 23,  0, 50, 26,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 12, 18, 12, 25, 43, 13,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 14, 19, 12, 23, 35, 12,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 17, 16, 11, 22, 37, 21,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 19, 28, 21, 44,  8, 22, 23,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 15, 22, 17, 36, 25, 28,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 21, 10, 20, 26,  6, 25, 30,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 25, 10, 14, 29, 11, 33, 27,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 22, 15, 17, 24,  8, 32, 26, 10,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 18,  9, 13, 19, 13, 13, 36,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 22, 10,  8, 25, 23, 21, 45, 11,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  9,  9, 11, 16, 14, 28, 14,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  9, 11, 17, 10, 10, 35,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 16,  0, 11, 25,  9, 35,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  8,  9, 27,  8, 20, 11,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 12,  0, 10, 34,  0, 18,  7,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 27,  9, 19,  9,  7,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  7,  8,  9, 18, 18,  8, 12,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 10, 14, 16,  0, 13,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 16,  7, 10, 12,  9,  8, 11,  0,  0,  0,  0,  0,  0,  0,  0,  0,  7,  0, 12,  9, 26, 14, 11, 20,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0};    }
    @ConstArray
    public static class Rssi1 {
        public final static byte[]  data = { 22, 38, 22, 22, 15, 32,  9,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 26, 34, 34, 34, 12, 26, 24,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 31, 32, 38, 23, 11, 33, 30,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 18, 38, 14,  8, 27, 10, 33, 23,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 24, 28, 29,  0, 25,  9, 13, 38, 16,  9, 11,  0,  0,  0,  0,  0,  0,  0, 24, 15, 28, 31,  8, 12, 33, 29, 10,  6,  0,  0,  0,  0,  0,  0,  0,  0, 28, 39, 26,  9, 30, 16, 31, 23,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 27, 32, 28,  8, 31, 13, 38, 26,  9, 10,  0,  0,  0,  0,  0,  0,  0,  0, 30, 28, 17,  0, 36,  9, 12, 27, 22,  9, 12,  0,  0,  0,  0,  0,  0,  0, 15, 13, 29, 19,  9, 12, 25, 27,  9,  8,  0,  0,  0,  0,  0,  0,  0,  0, 28, 23, 27,  9, 22,  8, 10, 32, 31, 13, 13,  0,  0,  0,  0,  0,  0,  0, 33,  0,  0, 16,  9, 19,  0, 10, 23, 22, 13, 11,  0,  0,  0,  0,  0,  0, 35, 18,  0, 24, 10, 21,  8, 12, 27, 30, 18,  0,  0,  0,  0,  0,  0,  0, 36, 16,  7, 18,  9, 28,  0, 13, 24, 35, 14, 13,  0,  0,  0,  0,  0,  0, 30, 22, 20,  8, 19,  0, 24, 26, 35,  8,  8,  0,  0,  0,  0,  0,  0,  0, 30, 20, 10, 20,  8, 24,  0, 12, 27, 31, 13, 13,  0,  0,  0,  0,  0,  0, 20, 17, 10, 11, 10, 22,  0, 26, 23, 35, 21, 19,  0,  0,  0,  0,  0,  0, 14, 16,  0, 20,  0, 19,  9, 19, 21, 27, 24, 16,  0,  0,  0,  0,  0,  0, 28, 11, 23, 10,  9, 14,  0,  0,  8, 23, 18, 22, 19, 20,  0,  0,  0,  0, 30, 20,  0, 16,  8,  9,  0,  0,  0, 30, 13, 26, 30, 16,  0,  0,  0,  0, 27, 11, 11, 10,  0, 22,  0, 11, 21, 10, 23, 14, 17,  0,  0,  0,  0,  0, 15,  0, 10,  8,  0,  0,  0,  8, 30, 11, 15, 11, 16,  0,  0,  0,  0,  0, 23, 12, 13, 16, 10,  0,  9, 17, 18, 21, 12, 19,  0,  0,  0,  0,  0,  0, 19,  0,  8,  6, 10, 19, 10, 36, 11, 18, 22, 18,  0,  0,  0,  0,  0,  0, 19,  8, 10, 10, 11, 16, 12,  0, 10, 21,  9, 23, 11, 19,  0,  0,  0,  0, 17, 11,  9,  8,  0, 13, 12, 11, 18, 30, 12, 13, 22, 13,  0,  0,  0,  0, 18,  7,  0,  0, 15, 12, 10,  8,  7,  0, 11, 25,  0, 12, 20, 17,  0,  0, 12,  9, 10,  7,  8, 15, 18,  8,  0,  0, 19, 24, 10,  0, 18, 15,  0,  0, 10,  0, 10,  9, 13,  9,  0, 16, 20, 14, 12, 17, 18,  0,  0,  0,  0,  0,  0,  9,  8,  0,  8,  8,  7,  0, 21, 25, 10, 12, 19, 17,  0,  0,  0,  0, 13,  0,  7, 18,  0,  0,  0,  0,  0, 21, 25, 10, 16, 19, 20,  0,  0,  0, 11,  0,  0, 22,  8,  7, 11,  0, 23, 21,  0, 12, 11, 18,  0,  0,  0,  0,  9,  0, 11, 17, 11,  9, 16,  0,  0, 30, 21, 13, 11, 12, 22,  0,  0,  0,  0, 10,  8,  0, 24, 15, 14, 19,  7, 30, 12,  9,  9,  9, 24,  0,  0,  0,  0, 15,  9, 26,  0,  0, 17,  0,  0, 22, 23,  8,  8,  9, 20,  0,  0,  0,  9,  0,  0, 19, 11,  0, 13,  0, 10, 21,  8,  8,  7, 26,  0,  0,  0,  0,  8, 19,  0, 19,  0,  0, 21,  8, 25, 17,  9,  8, 21,  0,  0,  0,  0,  0,  8,  0, 13,  0,  7, 18,  8,  7, 15, 23, 14,  0, 10,  0,  7, 23,  0,  0, 12,  0, 22, 10, 24, 17, 32, 15,  8,  0, 18,  0,  0,  0,  0,  0,  0,  0, 16,  8, 12,  7, 22, 15, 35, 10,  8, 25,  0,  0,  0,  0,  0,  0,  0,  0, 10, 22,  8, 24, 13, 30, 16, 13,  8,  0,  0, 32,  0,  0,  0,  0,  0,  0,  0,  8, 12,  8, 24,  8,  7, 26,  0,  9,  0,  0, 24,  0,  0,  0,  0,  0,  7, 22,  9, 27,  8, 25, 10,  8,  0,  0,  0, 13,  0,  0,  0,  0,  0,  0, 30, 20,  7, 10,  8, 12,  9,  0, 25, 15, 17, 10,  7,  0,  0,  0,  0,  0, 33, 12, 17, 22, 13,  8,  0, 29, 13, 26, 24,  0,  0,  0,  0,  0,  0,  0, 35, 12, 17, 15, 12,  0, 23,  9, 23, 25,  0,  0,  0,  0,  0,  0,  0,  0, 32, 12, 21, 18, 16,  0, 28,  0, 14, 28,  0,  0,  0,  0,  0,  0,  0,  0, 36,  8,  9, 10, 19, 22,  9,  0,  8, 32, 10, 13, 30,  0,  0,  0,  0,  0, 23, 20, 18, 21, 10, 16,  8, 33, 15, 23, 32,  0,  0,  0,  0,  0,  0,  0, 33, 13,  7, 21, 26,  6,  7,  0, 40,  0, 16, 23,  0,  0,  0,  0,  0,  0, 34, 15,  8,  9, 22, 22,  8, 14, 10, 37,  8, 13, 20,  0,  0,  0,  0,  0, 26,  8,  0, 10, 30, 21,  0,  0, 28,  0, 10, 34,  0,  0,  0,  0,  0,  0, 21, 10, 13, 26, 15,  9, 13,  7, 36, 11, 34,  0,  0,  0,  0,  0,  0,  0, 28,  7, 17, 11,  9, 24,  9, 10,  8, 21,  8, 13, 39,  0,  0,  0,  0,  0, 24, 11, 10, 21, 20, 24, 15,  0, 24, 13, 32,  7, 13,  0, 38,  0,  0,  0, 10, 16,  9,  7, 20,  0, 19, 12,  5,  7, 25,  0,  0,  0,  0,  0,  0,  0,  8, 13, 13,  7, 21, 12, 15, 15,  0,  0, 23,  0,  0,  0,  0,  0,  0,  0,  0, 21, 15,  0, 12,  9, 13, 11,  0,  0,  0, 10, 17,  0,  0,  0,  0,  0,  0, 24, 11,  8, 21,  7, 11, 25, 18,  0,  0,  0, 26,  0,  0,  0,  0,  0,  0,  8, 22, 10, 19, 10, 25, 10, 15,  0, 21,  0,  0,  0,  0,  0,  0,  0, 12, 11, 13, 22, 21,  8, 24,  7,  8,  0, 12, 13, 13,  0,  9, 24,  0,  0,  0, 23, 12,  0, 13,  0,  0, 23, 18, 11, 11, 10, 14,  0,  0,  0,  0,  0, 13, 24, 22, 10, 19,  0, 16, 21, 19,  0,  8, 15,  0,  0,  0,  0,  0,  0,  0,  0, 12, 18, 14,  8,  6, 32, 15,  0,  0, 20, 14,  0,  0,  0,  0,  0,  7, 22, 17, 11,  8, 16,  9, 38, 19,  0, 13,  8,  0,  0,  0,  0,  0,  0,  0,  9, 26, 24, 15, 10, 19, 34, 17, 16, 21,  0,  0,  0,  0,  0,  0,  0,  8, 18, 30, 18, 18, 16,  9,  0, 40, 10, 12,  7,  0,  0,  0,  0,  0,  0, 20, 29, 14, 19,  9, 12, 23, 25, 17,  8,  0,  0,  0,  0,  0,  0,  0,  0,  7, 21, 32, 16, 14,  8, 11,  0, 26, 17,  9,  8,  0,  0,  0,  0,  0,  0, 18, 32, 34, 18,  9, 10, 20, 12,  0, 14, 18,  0,  0,  0,  0,  0,  0,  0, 18, 29, 22, 16,  8,  8, 32, 10, 14,  0,  0,  0,  0,  0,  0,  0,  0,  0, 27, 25, 23, 16,  0, 20, 18, 18,  0, 11,  0,  0,  0,  0,  0,  0,  0,  0, 11, 27, 21, 12, 20,  0, 33, 12,  7,  8,  8,  0,  0,  0,  0,  0,  0,  0, 21, 38, 30, 18, 23, 26, 17, 11, 16,  0,  0,  0,  0,  0,  0,  0,  0,  0, 21, 38, 33, 15, 21, 23, 13,  9, 16,  0,  0,  0,  0,  0,  0,  0,  0,  0, 10, 38, 30, 22, 11, 20, 13, 11,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 24, 28, 21, 20, 25, 11, 12,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 25, 17, 38, 16, 18, 19,  8, 12,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 19, 38, 34, 20, 10, 19,  0,  9,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 15,  9, 23, 10, 14,  9, 18,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 20, 12, 18,  9, 17,  0,  0, 12,  0,  0,  0,  0,  0,  0,  0,  0,  0, 12, 18, 10,  8,  7, 20,  8, 20,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 10,  0, 12,  8, 29, 11, 10,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  8, 16, 16, 30,  0, 26,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  9, 25,  8, 24,  0, 21,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 11, 28,  8, 30,  0,  0,  8, 12,  0,  0,  0,  0,  0,  0,  0,  0,  0, 12,  0, 20,  8, 23,  9, 22, 15, 24,  0,  0,  6, 26,  0,  0,  0,  0,  0, 20,  7,  0, 21, 14, 12, 12,  7, 16, 24,  0, 12,  0,  0,  0,  0,  0,  0, 22,  0,  9, 15, 15, 29, 20, 19, 14, 22,  0, 10, 30,  0,  0,  0,  0,  0, 23,  8,  8, 16, 11, 25, 11, 14, 10, 20,  8,  8, 29,  0,  0,  0,  0,  0, 20, 12, 15, 13, 29, 12, 15, 15, 21,  0, 10, 28,  0,  0,  0,  0,  0,  0, 17, 15,  6, 24, 18, 31,  0, 21, 15, 26,  0,  0, 36,  0,  0,  0,  0,  0, 14, 11, 17, 21, 31,  8, 25, 19, 29,  8, 10, 27,  0,  0,  0,  0,  0,  0, 12, 10, 25, 12, 34,  0, 25, 18, 21, 11, 33,  0,  0,  0,  0,  0,  0,  0, 10, 11,  9, 25, 16, 33,  0, 22, 16, 30, 14, 33,  0,  0,  0,  0,  0,  0,  9, 11, 24, 11, 40,  0, 24, 22, 28, 16, 26,  0,  0,  0,  0,  0,  0,  0,  9, 18, 24, 23, 38,  8, 30, 11, 33,  7, 25,  0,  0,  0,  0,  0,  0,  0,  9,  0,  0, 22, 28, 33,  8, 33, 11, 32, 13, 23,  0,  0,  0,  0,  0,  0,  8, 17,  0, 28, 26, 27, 16, 22, 26, 10, 26,  0,  0,  0,  0,  0,  0,  0,  9, 12,  0, 32, 26, 29, 22,  0, 12, 23, 11, 20,  8,  0,  0,  0,  0,  0,  8, 24,  9, 26, 34, 11, 19,  8, 20, 25, 17, 23,  0,  0,  0,  0,  0,  0,  0,  9,  9, 11, 30, 20, 20,  0, 10, 10, 10, 19,  0,  0,  0,  0,  0,  0,  8, 12,  8, 30, 30, 23, 24, 25, 17, 16, 18,  8, 10,  0,  0,  0,  0,  0, 18,  8, 34, 26, 12, 19,  0,  9, 30, 10, 19,  0,  0,  8,  0,  0,  0,  0, 22,  0, 34, 22,  0, 18, 13, 19,  0, 20,  9, 12,  0,  8, 12,  0,  0,  0, 18, 12, 40, 21,  0, 21,  0, 15, 26,  9,  8, 18, 10, 10,  0,  0,  0,  0, 18, 14, 40, 31,  0, 11, 11, 12,  0,  8, 34,  0, 17,  0,  8,  0,  0,  0, 20, 15, 35, 26,  0, 15,  9,  8,  0,  0, 27,  7, 11, 10, 10, 10,  0,  0, 22, 16, 33, 22, 14,  0, 15, 13, 36, 10, 18, 10,  0,  0,  0,  0,  0,  0, 28, 16, 40, 26,  9,  8, 10, 11,  0, 30,  9, 14, 16,  8,  0,  0,  0,  0, 30, 16, 29, 16,  8, 10, 12, 10, 12, 30,  0, 21, 10, 10,  8,  0,  0,  0, 23, 16, 31, 20, 13, 11,  7, 19, 18, 34,  8, 24,  0, 12, 16,  0,  0,  0, 24, 13, 29, 15,  7,  8, 12,  9, 13, 32, 10, 10,  9, 19,  0,  0,  0,  0, 24,  0, 17, 15,  0, 12, 15,  0, 16,  0, 36, 21, 18, 19,  0,  0,  0,  0, 34, 16, 30, 11,  0,  9,  0,  8, 17, 20, 32,  9, 24,  8, 18, 11,  0,  0, 23, 21, 19, 15,  0, 14, 10,  8,  8, 21, 36, 13,  8, 24, 11,  0,  0,  0, 23, 14, 25,  8,  8, 13, 20, 16, 20, 32, 26, 24, 17,  0,  0,  0,  0,  0, 28, 23, 19, 11, 10, 13,  0, 15,  8, 29, 25,  0, 19, 20,  0,  0,  0,  0, 21, 31, 21,  9,  8, 15, 16, 24, 23, 19,  0, 24,  0,  0,  0,  0,  0,  0, 15, 28,  8,  7, 11, 21, 18, 23, 26, 19,  0, 30, 23,  0,  0,  0,  0,  0,  0, 24, 10,  0, 13, 16, 10, 23, 20, 25, 18,  0,  0,  0,  0,  0,  0,  0, 12, 30, 16,  0,  9,  8,  6, 22, 26, 26, 31, 15, 16,  0,  0,  0,  0,  0, 18, 30, 18,  0,  7, 21, 13, 26, 25, 17,  0, 16,  9,  0,  0,  0,  0,  0, 13, 28, 14,  9,  0, 16,  8, 26, 19, 29, 14, 18,  0,  0,  0,  0,  0,  0, 11, 28, 17, 12,  8, 18, 10, 18, 12, 28, 10, 22,  0,  0,  0,  0,  0,  0,  9, 16, 20,  0, 11,  8, 14, 20, 10, 28, 13, 17,  0,  0,  0,  0,  0,  0, 12, 25, 12,  0,  0, 11, 21, 22, 16, 32,  0, 17, 15,  0,  0,  0,  0,  0,  0, 19, 10,  8, 13,  8,  9, 20, 20, 17, 37,  0, 16,  0,  0,  0,  0,  0,  9, 11,  8, 16,  9, 12, 30, 24,  9, 38,  8,  0,  0,  0,  0,  0,  0,  0, 11, 19,  0, 18, 12, 10, 26, 25, 10, 32,  0, 18,  0,  0,  0,  0,  0,  0, 12,  8,  0, 16,  0, 24, 30,  9, 35,  9, 14,  0,  0,  0,  0,  0,  0,  0,  8, 10, 10, 15,  9, 11, 23, 12, 10, 45,  0, 11,  0,  0,  0,  0,  0,  0,  9,  0, 21, 18, 20, 11, 40,  0,  0, 10,  0,  0,  0,  0,  0,  0,  0,  0,  9,  0, 12,  0, 13, 30,  0, 42, 12,  0,  0,  0,  0,  0,  0,  0,  0,  0, 10, 10, 20,  0, 24, 14,  0, 36, 16,  0,  0,  0,  0,  0,  0,  0,  0,  0, 12,  9,  8, 14, 21,  0, 22, 12,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 10, 12, 24, 11, 19, 37,  8,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  9, 31,  9, 19, 11, 30,  9,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  8, 28,  8,  0, 18, 15,  8, 30,  0,  0,  0,  0,  0,  0,  0,  0,  0, 10, 13,  0,  0, 26,  0, 10, 13, 23,  0,  0,  0,  0,  0,  0,  0,  0,  0,  9,  0, 16,  8, 14,  9, 19,  7,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  8, 12,  9,  0, 16,  9, 10, 15,  8, 18,  8,  0,  0,  0,  0,  0,  0,  0,  8,  8, 17, 12,  8,  7, 18,  8,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  9, 10, 10,  9, 23,  8, 14, 12, 12, 28,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 24,  9, 18, 21,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  7, 11,  0, 36,  8, 13, 13, 18,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 17,  0,  8, 24, 13,  9, 19, 12,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 27,  0, 17,  9, 12,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  9,  7,  0, 17,  0, 15, 10, 25,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  8,  9,  0, 33,  6, 27, 19,  0, 29,  0,  0,  0,  0,  0,  0,  0,  0,  0, 10,  9, 23, 20, 13, 17,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 12,  9, 11, 32, 23,  0, 10, 15,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  8, 12, 11, 43,  8, 33,  0, 25, 27,  0,  0,  0,  0,  0,  0,  0,  0,  0,  9, 11, 11, 43,  0, 25,  0, 18, 26,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  8,  9, 45, 32, 19, 14,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  7,  0, 26, 32, 31, 30, 21, 20, 22,  0,  0,  0,  0,  0,  0,  0,  0,  0,  8, 21, 30, 34, 42, 13, 10, 22,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 10,  7, 34, 38, 19,  0, 27, 26, 17,  0, 15,  0,  0,  0,  0,  0,  0,  0, 14, 25, 37, 24, 30, 24,  0,  9,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 21,  0, 49, 30, 17,  7,  9, 34,  8, 12, 12,  8,  0,  0,  0,  0,  0,  0, 31, 43, 26, 12,  0, 26,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 33, 10, 37, 23, 10, 11,  9,  8, 27,  0,  0, 12,  9,  0,  0,  0,  0,  0, 46,  0, 37, 30, 11,  0, 10,  0, 29,  0, 17,  0,  0,  0,  0,  0,  0,  0, 41,  7, 28, 22,  0, 18,  0, 10,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 35, 24, 27,  0,  0, 10, 12, 27,  0, 22, 17,  0,  0,  0,  0,  0,  0,  0, 38, 24, 13,  8, 12,  0,  9, 23,  9, 21, 14,  0,  0,  0,  0,  0,  0,  0, 36, 16, 23, 11,  8,  0,  9, 20, 30,  7,  0,  0,  0,  0,  0,  0,  0,  0, 22, 31, 17,  8, 24, 10, 15,  0, 38,  0,  0,  0,  0,  0,  0,  0,  0,  0, 11, 17, 10,  0, 19, 13, 17, 12, 32, 10,  0,  0,  0,  0,  0,  0,  0,  0, 16, 25, 16,  8,  7, 11, 16, 15,  8, 36,  0,  0,  0,  0,  0,  0,  0,  0, 10, 33, 14, 10, 30, 22, 12, 12, 25, 23,  0,  0,  0,  0,  0,  0,  0,  0,  9, 44, 10, 11, 24, 12,  8,  8, 22, 20,  0,  0,  0,  0,  0,  0,  0,  0,  8, 38, 11, 17, 10,  9, 15, 26, 13,  0,  0,  0,  0,  0,  0,  0,  0,  0, 11, 25, 10, 18, 32, 26,  8, 25, 16, 23,  0,  0,  0,  0,  0,  0,  0,  0,  0, 30,  7, 10,  9, 30, 15,  8, 24, 14, 10,  0,  0,  0,  0,  0,  0,  0, 30,  7,  6, 19, 26,  8, 24,  9, 11,  0,  0,  0,  0,  0,  0,  0,  0,  0, 12, 19, 11, 31, 26,  0, 23, 11,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 21, 17, 27, 14, 21, 13,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 22, 14,  0, 43, 16, 20,  7,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 12,  0, 23, 25, 24,  0, 43,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 18, 35, 28, 14, 36,  0,  8,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 13, 25, 26, 11, 26,  7,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 39, 16, 15, 24,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 10, 40, 22, 15, 36,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  9, 45, 18, 29,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 16, 16, 12, 27,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 25,  0, 20,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 33,  9,  0, 24,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 20,  8, 20,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 19,  8, 20,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 24, 14,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 12, 11, 10, 25, 31, 17,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 12,  9, 11, 19, 32,  7,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 11, 12, 15, 14, 45, 17,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 14, 10, 13, 18, 31, 45, 20,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 11, 12, 21, 13, 28, 11, 36, 21,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 14, 13, 11, 26,  0, 39, 22,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 25, 11, 21, 17, 42, 25, 29, 14, 36,  0,  0,  0,  0,  0,  0,  0,  0,  0, 13, 25,  0, 34, 19, 27, 13, 23,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  9, 15, 33,  0, 29, 19, 28,  0, 40,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 16, 31,  0, 32,  9, 26, 46,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 11, 41, 25, 16, 11, 41,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  8, 30, 17, 22, 16,  8, 35,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  8,  8, 25, 15, 23, 10, 36,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  8, 31, 18, 18, 14, 46,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 10, 21, 10, 34, 10, 19,  0, 31,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 12,  0, 28, 10, 36, 16, 26, 26,  0,  0,  0,  0,  0,  0,  0,  0,  0, 19,  0, 20, 12, 44, 20, 32,  9, 14,  0,  0,  0,  0,  0,  0,  0,  0,  0,  9, 22,  8, 23, 11, 26, 28, 19, 20, 29,  0,  0,  0,  0,  0,  0,  0,  0, 10, 12,  9, 23, 11, 31, 27, 18, 13, 27,  0,  0,  0,  0,  0,  0,  0,  0, 28, 10, 24,  8,  9, 10, 19, 53,  0,  8,  6,  8, 21,  0,  0,  0,  0,  0, 22,  9, 15,  9, 14, 14, 31, 10,  8, 19,  0,  0,  0,  0,  0,  0,  0,  0, 16,  8, 21, 10,  7, 24, 12, 42,  0, 19,  0,  0,  0,  0,  0,  0,  0,  0,  9, 20, 32, 21, 19,  8, 21,  7, 28, 26,  8, 23,  8,  0,  0,  0,  0,  0,  9,  0, 25, 20, 14, 22, 27, 27, 28,  0,  0,  0,  0,  0,  0,  0,  0,  0, 24,  0,  9,  8, 36, 18, 20, 10, 35,  7, 45,  0,  0,  0,  0,  0,  0,  0, 22,  0, 20,  0, 26, 17, 11, 17, 31,  6, 25,  0,  0,  0,  0,  0,  0,  0, 19, 19, 10, 19,  9, 12,  9, 38, 40,  0,  0,  0,  0,  0,  0,  0,  0,  0, 33,  0, 12,  9, 17, 21, 10,  9, 42,  6, 13, 33,  0,  0,  0,  0,  0,  0, 24,  8,  0, 11, 15, 11,  0, 30,  0, 36,  0,  0,  0,  0,  0,  0,  0,  0, 25,  0,  0, 10, 28, 10, 19, 14, 35,  8, 34,  0,  0,  0,  0,  0,  0,  0, 21,  0, 18, 10,  0, 16, 30,  9, 22, 18,  0,  0,  0,  0,  0,  0,  0,  0, 10,  0, 16, 11,  0,  6,  7,  9, 40, 10, 24,  0,  0,  0,  0,  0,  0,  0, 13,  7, 19,  9, 11, 23,  0, 26,  7,  0,  0,  0,  0,  0,  0,  0,  0,  0, 40, 12,  0, 19, 25, 12,  8, 14,  6, 13, 28,  0,  0,  0,  0,  0,  0,  0, 46, 26, 13, 26, 13,  0, 25,  9, 17, 27,  0,  0,  0,  0,  0,  0,  0,  0, 45, 16, 12, 34, 15,  0, 32, 16, 12, 24,  0,  0,  0,  0,  0,  0,  0,  0, 33, 19, 14, 28,  0, 31,  9, 17,  9,  0,  0,  0,  0,  0,  0,  0,  0,  0, 11, 10, 11, 25, 12, 22, 13, 26, 22,  0,  0,  0,  0,  0,  0,  0,  0,  0, 35, 31, 39, 13, 19, 13, 18,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 37, 32,  7, 44, 11, 24, 23, 14, 15,  0,  0,  0,  0,  0,  0,  0,  0,  0, 25, 24,  8, 36, 17, 24, 15, 10,  9,  0,  0,  0,  0,  0,  0,  0,  0,  0, 27, 40, 33, 17, 14, 22, 14,  8,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 13, 36, 35, 15, 11, 14,  7,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 24, 26, 24, 10, 19, 23,  7,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  8, 30, 29, 18,  8, 26,  9,  8,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 18, 28, 11, 29,  7, 44, 20,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  6, 18,  9, 29, 43, 17,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 11, 13, 11, 23, 37,  9,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 14, 16,  0, 27, 40, 20,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 20, 27, 20, 39, 10, 28, 21,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 18, 10, 21, 40, 24, 15,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 18, 14, 33, 13, 19, 25,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 19,  9, 18, 31, 11, 22, 34,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 23,  9,  7, 32,  8, 18, 37,  8,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 16,  8,  8, 13, 12, 19, 34,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 26,  0, 15, 14, 16, 14, 45,  9,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 17,  0,  9,  9, 19, 22, 35,  9,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  9,  8, 15, 22, 10, 31, 13,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 12,  8, 18, 17, 10, 28, 10,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 11,  0, 23,  0, 14, 17,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 14,  9,  0, 25,  8, 17,  0,  6,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  9,  8, 20,  0, 16, 12,  9,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  9, 10, 20,  7,  0, 20,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 11, 21, 20, 11, 16,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  8, 10, 11,  0, 12, 19, 11,  9, 16,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 16,  8,  0,  9, 13,  0, 15,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0};    }}
public class RFSignal {
    public static final byte MIN_RSSI = 0;
    public static final byte MAX_RSSI = 100;
    public static final byte MAX_RSSI_DIFF = (MAX_RSSI - MIN_RSSI);
    public short sourceID;
    public byte rssi_0;
    public byte rssi_1;
    public RFSignal()    {    }
    @Lightweight
    public static void init(RFSignal sigPtr) {
        sigPtr.sourceID = 0;
        sigPtr.rssi_0 = 0;
        sigPtr.rssi_1 = 0;    }
    @Lightweight
    public static void rfSignalDiff(ShortResults results, RFSignal rfSig1Ptr, RFSignal rfSig2Ptr)    {
        if (rfSig2Ptr == null) {
                results.r0 = rfSig1Ptr.rssi_0;
                results.r1 = rfSig1Ptr.rssi_1;        }
        else if ( rfSig2Ptr != null && (rfSig1Ptr.sourceID == rfSig2Ptr.sourceID) ) {
                short x;
                x = (short)(rfSig1Ptr.rssi_0 - rfSig2Ptr.rssi_0);
                if (x > 0) {
                    results.r0 = x;
                } else {
                    results.r0 = (short)-x;                }
                x = (short)(rfSig1Ptr.rssi_1 - rfSig2Ptr.rssi_1);
                if (x > 0) {
                    results.r1 = x;
                } else {
                    results.r1 = (short)-x;                }        }
        else {
            RTC.avroraPrintHex32(0xBEEF0001);
            RTC.avroraBreak();        }    }
    public static byte convertRSSI(short rssi)    {
        short rssi_dBm = (short) (rssi - 45);
        short rssi_Scaled = (short)(MAX_RSSI_DIFF + rssi_dBm);
        if (rssi_Scaled < MIN_RSSI) 
            return MIN_RSSI;
        if (rssi_Scaled > MAX_RSSI)
            return MAX_RSSI;
        else
            return (byte)rssi_Scaled;    }
    public static void sortSrcID(RFSignal rfSignals[], short size)    {
        int i = 0;
        int k = 0;
        RFSignal temp;
        for (i = 0; i < size-1; ++i) {
            for (k = i+1; k < size; ++k) {
                if (rfSignals[k].sourceID < rfSignals[i].sourceID) {
                    temp = rfSignals[i];
                    rfSignals[i] = rfSignals[k];
                    rfSignals[k] = temp;                }            }        }    }}
public class RFSignalAvg {
    short sourceID;
    short rssiSum_0;
    byte nbrSamples_0;
    short rssiSum_1;
    byte nbrSamples_1;
    public RFSignalAvg() {    }
    public static void init(RFSignalAvg rfSigPtr) {
        rfSigPtr.sourceID = 0;
        rfSigPtr.rssiSum_0 = 0;
        rfSigPtr.nbrSamples_0 = 0;
        rfSigPtr.rssiSum_1 = 0;
        rfSigPtr.nbrSamples_1 = 0;          }}
public class RFSignalAvgHT {
    public RFSignalAvg[] htData;
    public short size;
    public short capacity;
    public RFSignalAvgHT(short hashCapacity) {
        this.htData = new RFSignalAvg[hashCapacity];
        this.capacity = hashCapacity;
        for (short i=0; i<hashCapacity; i++) {
            this.htData[i] = new RFSignalAvg();        }        
        init(this);    }
    @Lightweight
    public static void init(RFSignalAvgHT rfSigPtr) {
        rfSigPtr.size = 0;
        short capacity = rfSigPtr.capacity;
        for (short i=0; i<capacity; i++) {
            RFSignalAvg.init(rfSigPtr.htData[i]);        }            }
    @Lightweight
    public static boolean put(RFSignalAvgHT HPtr, short newSrcID, byte f, short newRssi)    {
        short hashID = (short)(((short)(newSrcID*13)) % HPtr.capacity);
        short i = hashID;
        do {
            if (HPtr.htData[i].sourceID == 0) {
                HPtr.htData[i].sourceID = newSrcID;  // this only matters when nbrSamples[i] == 0!
                HPtr.size++;            }
            if (HPtr.htData[i].sourceID == newSrcID) {
                if (f == 0) {
                    HPtr.htData[i].rssiSum_0 += newRssi;   // DANGER! if rssiSum is uint8_t there may be an overflow problem!
                    HPtr.htData[i].nbrSamples_0++;
                } else {
                    HPtr.htData[i].rssiSum_1 += newRssi;   // DANGER! if rssiSum is uint8_t there may be an overflow problem!
                    HPtr.htData[i].nbrSamples_1++;                }
                return true;                                                             }
            i = (short)(((short)(i + 1)) % HPtr.capacity);
        } while (i != hashID);
        if (HPtr.size == HPtr.capacity) {
            RTC.avroraPrintHex32(0xBEEF0004);
            RTC.avroraBreak();        }
        else {
            RTC.avroraPrintHex32(0xBEEF0005);
            RTC.avroraBreak();        }
        return false;    }
    static short makeSignature(Signature retSigPtr, RFSignalAvgHT HPtr)    {
        short i = 0, k = 0, f = 0; //, p = 0;
        short maxRssiIndex = 0;
        short retSrcIDMaxRSSIPtr;
        if (HPtr.size == 0) {
            retSrcIDMaxRSSIPtr = 0;      // the hash table is empty, so can't construct Signature    
        } else if (HPtr.size <= Signature.NBR_RFSIGNALS_IN_SIGNATURE) {
            k = -1;
            maxRssiIndex = 0;
            for (i = 0; i < HPtr.capacity; ++i) {
                RFSignalAvg HPtr_htData_i = HPtr.htData[i];
                if (HPtr_htData_i.sourceID > 0) {  // ASSUMING sourceID=0 means the cell is empty!
                    RFSignal retSigPtr_rfSignals_k = retSigPtr.rfSignals[++k];
                    retSigPtr_rfSignals_k.sourceID = HPtr_htData_i.sourceID;
                        if (HPtr_htData_i.nbrSamples_0 > 0) {
                            retSigPtr_rfSignals_k.rssi_0 = (byte)(HPtr_htData_i.rssiSum_0 / HPtr_htData_i.nbrSamples_0);
                        }                        else
                            retSigPtr_rfSignals_k.rssi_0 = 0;
                        if (HPtr_htData_i.nbrSamples_1 > 0) {
                            retSigPtr_rfSignals_k.rssi_1 = (byte)(HPtr_htData_i.rssiSum_1 / HPtr_htData_i.nbrSamples_1);
                        }                        else
                            retSigPtr_rfSignals_k.rssi_1 = 0;
                    if (retSigPtr_rfSignals_k.rssi_0 > retSigPtr.rfSignals[maxRssiIndex].rssi_0)
                        maxRssiIndex = k;                                       }                }
            retSrcIDMaxRSSIPtr = retSigPtr.rfSignals[maxRssiIndex].sourceID;
                RFSignal.sortSrcID(retSigPtr.rfSignals, HPtr.size);       }
        else {
            Signature.init(retSigPtr);
            k = 0;
            for (i = 0; i < HPtr.capacity; ++i) {
                if (HPtr.htData[i].sourceID > 0) {  // ASSUMING sourceID=0 means the cell is empty!                    
                    HPtr.htData[k].sourceID = HPtr.htData[i].sourceID;
                        if (HPtr.htData[i].nbrSamples_0 > 0) {
                            HPtr.htData[k].rssiSum_0 = (short)(HPtr.htData[i].rssiSum_0 / HPtr.htData[i].nbrSamples_0);
                            HPtr.htData[k].nbrSamples_0 = 1;  // statement not necessary, but keeps datastructure consistent                        }
                        else
                            HPtr.htData[k].rssiSum_0 = 0;
                        if (HPtr.htData[i].nbrSamples_1 > 0) {
                            HPtr.htData[k].rssiSum_1 = (short)(HPtr.htData[i].rssiSum_1 / HPtr.htData[i].nbrSamples_1);
                            HPtr.htData[k].nbrSamples_1 = 1;  // statement not necessary, but keeps datastructure consistent                        }
                        else
                            HPtr.htData[k].rssiSum_1 = 0;
                    k++;               }                           }
            retSrcIDMaxRSSIPtr = HPtr.htData[0].sourceID;
            for (i = 0; i < Signature.NBR_RFSIGNALS_IN_SIGNATURE && i < HPtr.size; ++i) {             
                retSigPtr.rfSignals[i].sourceID = HPtr.htData[i].sourceID;
                    retSigPtr.rfSignals[i].rssi_0 = (byte)(HPtr.htData[i].rssiSum_0);
                    retSigPtr.rfSignals[i].rssi_1 = (byte)(HPtr.htData[i].rssiSum_1);            }
                RFSignal.sortSrcID(retSigPtr.rfSignals, HPtr.size);        }
        return retSrcIDMaxRSSIPtr;    }}
public class ShortResults {
    public short r0, r1;}
public class SignalSpaceDiff {
    short refSigIndex;
    short diff;
    public SignalSpaceDiff() {
        init(this);    }
    public static void init(SignalSpaceDiff ssDiffPtr) {
        ssDiffPtr.refSigIndex = 0;
        ssDiffPtr.diff = 32767; // 65535L; // (2^16-1) because it's 16 bits            }
    public static void put(SignalSpaceDiff[] SSDiffs_0, SignalSpaceDiff[] SSDiffs_1, byte f, ShortResults diffs, short refSigIndex)    {
        short i=0;
        SignalSpaceDiff[] SSDiffs;
        short diff;
        if (f == 0) {
            SSDiffs = SSDiffs_0;
            diff = diffs.r0;
        } else {
            SSDiffs = SSDiffs_1;
            diff = diffs.r1;        }
        short ssDiffsSize = (short)SSDiffs.length;
        if (diff < SSDiffs[ssDiffsSize-1].diff) {    // we can add it
            for (i = (short)(ssDiffsSize-1); i > 0 && diff < SSDiffs[i-1].diff; --i) {
                SSDiffs[i].diff = SSDiffs[i-1].diff;
                SSDiffs[i].refSigIndex = SSDiffs[i-1].refSigIndex;            }
            SSDiffs[i].diff = diff;
            SSDiffs[i].refSigIndex = refSigIndex;        }    }
    public static void centroidLoc(Point retLocPtr, SignalSpaceDiff[] SSDiffs, short nbrRefSigs, RefSignature currRefSig)    {
        short i=0;
        int x=0, y=0, z=0;  // to prevent overflow from adding multiple 16-bit points
        for (i = 0; i < nbrRefSigs; ++i) {
            DB.refSignature_get(currRefSig, SSDiffs[i].refSigIndex);
            x += currRefSig.location.x;
            y += currRefSig.location.y;
            z += currRefSig.location.z;        }
        retLocPtr.x = (short)(x/nbrRefSigs);
        retLocPtr.y = (short)(y/nbrRefSigs);
        retLocPtr.z = (short)(z/nbrRefSigs);    }}
public class Signature {
    public static final byte NBR_RFSIGNALS_IN_SIGNATURE = 18;
    private static short GlobUniqSignatureID = 0;
    public short id;
    public RFSignal[] rfSignals;
    public Signature()    {
        rfSignals = new RFSignal[NBR_RFSIGNALS_IN_SIGNATURE];
        for(short i = 0; i < NBR_RFSIGNALS_IN_SIGNATURE; ++i) {
            this.rfSignals[i] = new RFSignal();        }
        init(this);    }
    public static void init(Signature sig) {
        for(short i = 0; i < NBR_RFSIGNALS_IN_SIGNATURE; ++i) {
            RFSignal.init(sig.rfSignals[i]);        }                     
        sig.id = ++GlobUniqSignatureID;    }}
public class SignatureDB {
    @ConstArray
    public static class X {
        public final static short[] data = { 169 };}
    @ConstArray
    public static class Y {
        public final static short[] data = { 226 };}
    @ConstArray
    public static class Z {
        public final static short[] data = {   0 };}
    @ConstArray
    public static class SigID {
        public final static short[] data = { 801 };}
    @ConstArray
    public static class SourceID {
        public final static short[] data = {  1 ,  3 , 11 , 13 , 21 , 22 , 23 , 30 ,  0 ,  0 ,  0 ,  0 ,  0 ,  0 ,  0 ,  0 ,  0 ,  0 };}
    @ConstArray
    public static class Rssi0 {
        public final static byte[]  data = { 27 , 32 , 24 , 28 ,  0 , 35 , 12 ,  7 ,  0 ,  0 ,  0 ,  0 ,  0 ,  0 ,  0 ,  0 ,  0 ,  0 };}
    @ConstArray
    public static class Rssi1 {
        public final static byte[]  data = { 23 , 32 , 21 , 25 , 10 , 22 , 25 ,  0 ,  0 ,  0 ,  0 ,  0 ,  0 ,  0 ,  0 ,  0 ,  0 ,  0 };}}