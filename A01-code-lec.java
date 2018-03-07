public class LEC {
    public static short benchmark_main(short[] numbers, Stream stream) {
        BSI bsi = new BSI();
        short ri_1 = 0;
        short NUMNUMBERS = (short)numbers.length;
        for (short i=0; i<NUMNUMBERS; i++) {
            short ri = numbers[i];
            compress(ri, ri_1, stream, bsi);

            ri_1 = ri;
        }

        // Return the number of bytes in the output array
        return (short)(stream.current_byte_index+1);
    }

    @ConstArray
    public static class si_tbl {
        public final static short data[] = {
            0b00, 0b010, 0b011, 0b100, 0b101, 0b110, 0b1110, 0b11110, 0b111110, 0b1111110, 0b11111110, 0b111111110, 0b1111111110, 0b11111111110, 0b111111111110, 0b1111111111110, 0b11111111111110
        };
    }

    @ConstArray
    public static class si_length_tbl {
        public static byte data[] = {
            2, 3, 3, 3, 3, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14
        };
    }

    // pseudo code:
    // compress(ri, ri_1, stream)
    //     // compute difference di
    //     SET di TO ri - ri_1
    //     // encode difference di
    //     CALL encode() with di RETURNING bsi
    //     // append bsi to stream
    //     SET stream TO <<stream,bsi>>
    //     RETURN stream
    public static void compress(short ri, short ri_1, Stream stream, BSI bsi_obj) {
        // compute difference di
        short di = (short)(ri - ri_1);
        // encode difference di
        encode(di, bsi_obj);
        int bsi = bsi_obj.value;
        byte bsi_length = bsi_obj.length;
        // append bsi to stream
        byte bits_left_current_in_byte = (byte)(8 - stream.bits_used_in_current_byte);
        while (bsi_length > 0) {
            if (bsi_length > bits_left_current_in_byte) {
                // Not enough space to store all bits

                // Calculate bits to write to current byte
                byte bits_to_add_to_current_byte =
                	(byte)(bsi >> (bsi_length - bits_left_current_in_byte));

                // Add them to the current byte
                stream.data[stream.current_byte_index] |= bits_to_add_to_current_byte;
                // Remove those bits from the to-do list
                bsi_length -= bits_left_current_in_byte;

                // Advance the stream to the next byte
                stream.current_byte_index++;
                // Whole new byte for the next round
                bits_left_current_in_byte = 8;
            } else {
                // Enough space to store all bits

                // After this we'll have -bsi_length bits left.
                bits_left_current_in_byte -= bsi_length;

                // Calculate bits to write to current byte
                byte bits_to_add_to_current_byte =
                	(byte)(bsi << bits_left_current_in_byte);

                // Add them to the current byte
                stream.data[stream.current_byte_index] |= bits_to_add_to_current_byte;
                // Remove those bits from the to-do list
                bsi_length = 0;
            }
        }

        stream.bits_used_in_current_byte = (byte)(8 - bits_left_current_in_byte);
        // Note that if we filled the last byte, stream_bits_used_in_current_byte
        // will be 8, which means in the next call to encode the first iteration of
        // the while loop won't do anything, except advance the stream pointer.
    }

    // pseudo code:
    // encode(di)
    //     // compute di category
    //     IF di = 0
    //         SET ni to 0
    //     ELSE
    //         SET ni to CEIL(log_2(|di|))
    //     ENDIF
    //     // extract si from Table
    //     SET si TO Table[ni]
    //     // build bsi
    //     IF ni = 0 THEN
    //         // ai is not needed
    //         SET bsi to si
    //     ELSE
    //         // build ai
    //         IF di > 0 THEN
    //             SET ai TO (di)|ni
    //         ELSE
    //             SET ai TO (di-1)|ni
    //         ENDIF
    //         // build bsi
    //         SET bsi TO <<si,ai>>
    //     ENDIF
    //     RETURN bsi
    private static void encode(short di, BSI bsi) {
        // compute di category
        short di_abs;
        if (di < 0) {
            di_abs = (short)-di;
        } else {
            di_abs = di;
        }
        byte ni = computeBinaryLog(di_abs);
        // extract si from Table
        short si = si_tbl.data[ni];
        byte si_length = si_length_tbl.data[ni];
        short ai = 0;
        byte ai_length = 0;
        // build bsi
        if (ni == 0) {
            bsi.value = si;
            bsi.length = si_length;
        } else {
            // build ai
            if (di > 0) {
                ai = di;
                ai_length = ni;
            } else {
                ai = (short)(di-1);
                ai_length = ni;            
            }
            bsi.value = (si << ai_length) | (ai & ((1 << ni) -1));
            bsi.length = (byte)(si_length + ai_length);
        }
    }

    // pseudo code:
    // computeBinaryLog(di)
    //     // CEIL(log_r|di|)
    //     SET ni TO 0
    //     WHILE di > 0
    //         SET di TO di/2
    //         SET ni to ni + 1
    //     ENDWHILE
    //     RETURN ni
    private static byte computeBinaryLog(short di) {
        byte ni = 0;
        while (di > 0) {
            di >>= 1;
            ni++;
        }
        return ni;
    }
}

public class BSI {
    public int value;
    public byte length;
}

public class Stream {
    public Stream(short capacity) {
        data = new byte[capacity];
        current_byte_index = 0;
        bits_used_in_current_byte = 0;
    }

    public byte[] data;
    public short current_byte_index;
    public byte bits_used_in_current_byte;
}
