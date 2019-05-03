package TransfereCC;

import java.io.IOException;

class ErrorControl {

        static byte[] calculateChecksum(byte[] data){
            int tamanho = data.length;
            int odd = tamanho%2;

            long checksum = 0;
            for(int i = 0; i < tamanho-odd; i+=2){
                checksum += ((data[i] & 0xFF) << 8  | data[i+1] & 0xFF);
                checksum = overflowWrapAround(checksum);
            }
            if(odd == 1){
                checksum += (data[tamanho-1] << 8 & 0xFF00);
                checksum = overflowWrapAround(checksum);
            }

            checksum = ~checksum & 0xFFFF;

            return new byte[] {(byte)((checksum & 0xFF00) >> 8), (byte)(checksum & 0xFF)};
        }

        static boolean verificaChecksum(MySegment segment) {
            byte[] segment_checksum = segment.getChecksum();
            segment.resetChecksum();
            byte[] checksum = calculateChecksum(segment.toByteArray());

            return (checksum[0] & 0xFF) == (segment_checksum[0] & 0xFF) && (checksum[1] & 0xFF) == (segment_checksum[1] & 0xFF) ;
        }


        private static long overflowWrapAround(long checksum){
            if ((checksum & 0xFFFF0000) > 0) {
                checksum = checksum & 0xFFFF;
                checksum += 1;
            }

        return checksum;
        }


}


