package TransfereCC;

import java.io.ByteArrayOutputStream;

import static TransfereCC.ErrorControl.processReceivedAck;

class ConnectionControl {

    static final int SYN = 1;
    static final int FYN = 2;
    static final int ACK = 3;
    static final int FYNREJECTED = 4;
    static final int SYNACK = 5;

    static boolean isFileGetRequest(MySegment received){
        return (received.flag == SYN) && (extractOpMode(received) == 0);
    }

    static boolean isFilePutRequest(MySegment received){
        return (received.flag == SYN) && (extractOpMode(received) == 1);
    }

    static boolean isFYN(MySegment received){
        return (received.flag == FYN);
    }

    static boolean isACK(MySegment received){
        return (received.flag == ACK);
    }

    static boolean isSYNACK(MySegment received){
        return (received.flag == SYNACK);
    }

    static boolean isRejectedConnectionFYN(MySegment received){
        return (received.flag == FYNREJECTED);
    }

    static void buildFYN(MySegment to_send){
         to_send.flag = FYN;
    }

    static void buildACK(MySegment to_send){
        to_send.flag = ACK;
    }

    static void buildSYN(MySegment to_send, String filename, Integer op){
         to_send.flag = SYN;
         String to_convert = op.toString() + filename;
         to_send.fileData = to_convert.getBytes();
    }

    static int extractOpMode(MySegment syn_segment){
        String content = new String(syn_segment.fileData);
        return Integer.parseInt(String.valueOf(content.charAt(0)));
    }

    static String extractFileName(MySegment syn_segment){
        String content = new String(syn_segment.fileData);
        String to_return =  content.substring(1);
        System.out.println("FILENAME: "+to_return );
        return  to_return;
    }

    static void processReceivedSYNAck(MySegment received, StateTable st) {
        if(st.opMode == 0) processReceivedAck(received, st);

        byte read_assinatura[] = new byte[4];
        byte read_pubkey[] = new byte[4];

        System.arraycopy(received.fileData, 0, read_assinatura, 0, 4);
        System.arraycopy(received.fileData, 4, read_pubkey, 0, 4);

        int assinatura_size = fromByteArray(read_assinatura);
        int pubkey_size = fromByteArray(read_pubkey);

        byte assinatura[] = new byte[assinatura_size];
        byte public_key[] = new byte[pubkey_size];

        System.arraycopy(received.fileData, 8, assinatura, 0, assinatura_size);
        System.arraycopy(received.fileData, 8+assinatura_size, public_key, 0, pubkey_size);

        st.setCrypto(assinatura, public_key);
    }


    static void buildRejectedConnectionFYN(MySegment to_send){
         to_send.flag = FYNREJECTED;
    }

    static void buildSYNACK(MySegment to_send, byte[] assinatura, byte[] public_key){
        to_send.flag = SYNACK;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(toByteArray(assinatura.length));
            outputStream.write(toByteArray(public_key.length));

            outputStream.write(assinatura);
            outputStream.write(public_key);

            byte[] dados = outputStream.toByteArray();

            to_send.setFileData(dados);
        }
        catch (Exception e){}

    }

   private static byte[] toByteArray(int value) {
        return new byte[] {
                (byte)(value >> 24),
                (byte)(value >> 16),
                (byte)(value >> 8),
                (byte)value };
    }


    private static int fromByteArray(byte[] bytes) {
        return bytes[0] << 24 | (bytes[1] & 0xFF) << 16 | (bytes[2] & 0xFF) << 8 | (bytes[3] & 0xFF);
    }
}
