import java.util.*;
import java.io.*;

public class TLVParser {

    private static final Map<String, String> tagNameMap = new LinkedHashMap<>();

    public static void loadTagDefinitions(String filename) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false; // Skip header line
                    continue;
                }
                String[] parts = line.split(",", 3);
                //System.out.println("Tag: " + parts[0] + ", Name: " + parts[1]);
                if (parts.length == 3) {
                    tagNameMap.put(parts[0].trim(), parts[1].trim());
                }
            }
        }
    }

    public static Map<String, String> parseTLV(String data) {
        Map<String, String> tlvMap = new LinkedHashMap<>();
        int index = 0;
        while (index < data.length()) {
            String tag = null;

            // Try 4-character tag first
            if (index + 4 <= data.length()) {
                String potentialTag = data.substring(index, index + 4);
                if (tagNameMap.containsKey(potentialTag)) {
                    tag = potentialTag;
                    index += 4;
                }
            }

            // Try 2-character tag
            if (tag == null && index + 2 <= data.length()) {
                String potentialTag = data.substring(index, index + 2);
                if (tagNameMap.containsKey(potentialTag)) {
                    tag = potentialTag;
                    index += 2;
                }
            }

            if (tag == null) {
                throw new IllegalArgumentException("Unknown tag at index " + index);
            }

            // Read Length (2 characters)
            if (index + 2 > data.length()) {
                throw new IllegalArgumentException("Incomplete Length at index " + index);
            }
            String lengthStr = data.substring(index, index + 2);
            int length = Integer.parseInt(lengthStr, 16);
            index += 2;

            // Read Value
            int valueLength = length * 2;
            if (index + valueLength > data.length()) {
                throw new IllegalArgumentException("Incomplete Value at index " + index);
            }
            String value = data.substring(index, index + valueLength);
            index += valueLength;

            tlvMap.put(tag, value);
        }
        return tlvMap;
    }

    public static void main(String[] args) {
        try {
            // Load tag names from CSV
            loadTagDefinitions("tag_definitions.csv");
    
            String tlvData = "5F2A0208405F340101820218008407A00000000422039A032503259C01009F02060000000006009F03060000000000009F0607A00000000422039F0702FFC09F090200029F0D05B0509C88009F0E0500000000009F0F05B0709C98009F10120110A00001220000000000000000000000FF9F1A0208409F1E0830373535383338369F21031641579F2608805E0208127472549F2701809F3303E0F8C89F3403420300950580000480009F3501229F360200599F3704A50F24349F3901059F4005F000F0A0019F410400000003DF7906372E302E3372";
    
            Map<String, String> parsedTLV = parseTLV(tlvData);
    
            for (Map.Entry<String, String> entry : parsedTLV.entrySet()) {
                String tag = entry.getKey();
                String value = entry.getValue();
                String name = tagNameMap.getOrDefault(tag, "Unknown");
    
                // Print in simple format: TAG  NAME  VALUE
                System.out.printf("%-6s %-40s %s%n", tag, name, value);
            }
        } catch (IOException e) {
            System.err.println("Error loading tag definitions: " + e.getMessage());
        }
    }
}
