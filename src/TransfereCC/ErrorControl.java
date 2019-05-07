package TransfereCC;

import Common.Pair;

import java.io.IOException;
import java.time.LocalTime;

class ErrorControl {

        static void setSegmentSeqNumber(StateTable st, MySegment segment){
            segment.seq_number = st.curr_seq_num++;
        }

        static void setAckNumber(StateTable st, MySegment segment){
            segment.ack_number = ++st.last_ack_value;
        }

    /**
     * @return -1 if there are no segments to re-send
     * @return n>1 if segment with seq_number=n is to be re-sent
     */
        static int processReceivedAck(MySegment segment, StateTable st){
            int send_base = st.unAckedSegments.first().seq_number;
            int ack_num = segment.ack_number;

            if(ack_num > send_base) {
                System.out.println("Comparacao feita : ACK : " + ack_num + " Send Base : " + send_base);
                // tree set ordenado por ordem crescente de seq num -> eliminar segmentos com seq num menor que o do ack (cumulative ack)
                while (st.unAckedSegments.size() != 0 && st.unAckedSegments.first().seq_number < ack_num) {
                    MySegment confirmed = st.unAckedSegments.pollFirst(); // elimina o primeiro
                    System.out.println("Rececao de segmento confirmada (SEQ: "+confirmed.seq_number+") - "+ LocalTime.now() );
                }
                return -2;
            }
            else{
                int num_dup = st.dupACKs.getOrDefault(ack_num,0);
                st.dupACKs.put(ack_num,num_dup+1);
                if(num_dup == 1)
                    return ack_num; // recebeu segundo duplicado
                else
                    return -1;
            }
        }

        static boolean isInOrder(StateTable st, MySegment segment){
            boolean in_order =  segment.seq_number == st.last_ack_value;
            if(in_order)System.out.println("Recebi segmento em ordem ("+segment.seq_number+")"+ LocalTime.now() );
            else System.out.println("Recebi segmento fora de ordem (" + segment.seq_number+") em vez de " + st.last_ack_value +" "+ LocalTime.now() );
            return in_order;
        }

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


