    public static int rtcbenchmark_measure_java_performance(final byte[] pt, byte[] ct, int skey_rounds, int[] skey_K)    {
        int A, B;
        int r;
        short K;
        for (byte i=0; i<100; i++) {
        A = ((int)(pt[3] & 255)<<24) | ((int)(pt[2] & 255)<<16) | ((int)(pt[1] & 255)<<8) | ((int)(pt[0] & 255));
        B = ((int)(pt[(4+3)] & 255)<<24) | ((int)(pt[(4+2)] & 255)<<16) | ((int)(pt[(4+1)] & 255)<<8) | ((int)(pt[(4+0)] & 255));
        A += skey_K[0];
        B += skey_K[1];
        K  = 2;
        if ((skey_rounds & 1) == 0) {
            for (r = 0; r < skey_rounds; r += 2) {
                A = ( ((A ^ B)<<(B&31)) | ((A ^ B)>>>(32-(B&31))) ) + skey_K[(K+0)];
                B = ( ((B ^ A)<<(A&31)) | ((B ^ A)>>>(32-(A&31))) ) + skey_K[(K+1)];
                A = ( ((A ^ B)<<(B&31)) | ((A ^ B)>>>(32-(B&31))) ) + skey_K[(K+2)];
                B = ( ((B ^ A)<<(A&31)) | ((B ^ A)>>>(32-(A&31))) ) + skey_K[(K+3)];
                K += 4;
            }
            K++; 
        } else {
            for (r = 0; r < skey_rounds; r++) {
                A = ( ((A ^ B)<<(B&31)) | ((A ^ B)>>>(32-(B&31))) ) + skey_K[(K+0)];
                B = ( ((B ^ A)<<(A&31)) | ((B ^ A)>>>(32-(A&31))) ) + skey_K[(K+1)];
                K += 2;            }        }
        ct[3] = (byte)(((A)>>>24)&255); ct[2] = (byte)(((A)>>>16)&255); ct[1] = (byte)(((A)>>>8)&255); ct[0] = (byte)((A)&255);
        ct[(4+3)] = (byte)(((B)>>>24)&255); ct[(4+2)] = (byte)(((B)>>>16)&255); ct[(4+1)] = (byte)(((B)>>>8)&255); ct[(4+0)] = (byte)((B)&255);        }
        return CRYPT_OK;    }