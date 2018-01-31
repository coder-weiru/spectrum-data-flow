package li.spectrum.ingestion.utils;

import java.nio.ByteBuffer;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.UUID;
import java.util.zip.CRC32;

import org.apache.commons.codec.binary.Base64;

public class IdGenerator {

	public static String generateTimestampId(Timestamp timestamp) {
		String id = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(timestamp);
		return id;
	}

	public static String generateDeterministicId(String partOne, String partTwo) {

		CRC32 localCRC32Generator = new CRC32();
		localCRC32Generator.update(partOne.getBytes());
		Long tempPartOneChecksum = localCRC32Generator.getValue();

		localCRC32Generator.update(partTwo.getBytes());
		Long tempPartTwoChecksum = localCRC32Generator.getValue();

		localCRC32Generator.reset();
		Long tempMSBits = tempPartOneChecksum * tempPartTwoChecksum;
		Long tempLSBits = tempPartTwoChecksum * tempPartTwoChecksum;

		StringBuffer tempUUID = new StringBuffer(new UUID(tempMSBits, tempLSBits).toString());
		tempUUID.replace(14, 15, "4");
		tempUUID.replace(19, 20, convertSecondReservedCharacter(tempUUID.substring(19, 20)));

		return uuidToBase64(tempUUID.toString()).toUpperCase();
	}

	public static String generateRandomUUID() {
		return UUID.randomUUID().toString();
	}

	private static String convertSecondReservedCharacter(String aString) {
		switch (aString.charAt(0) % 4) {
		case 0:
			return "8";
		case 1:
			return "9";
		case 2:
			return "a";
		case 3:
			return "b";
		default:
			return aString;
		}
	}

	protected static String uuidToBase64(String str) {

		UUID uuid = UUID.fromString(str);
		ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
		bb.putLong(uuid.getMostSignificantBits());
		bb.putLong(uuid.getLeastSignificantBits());
		return Base64.encodeBase64URLSafeString(bb.array());
	}

	protected static String uuidFromBase64(String str) {

		byte[] bytes = Base64.decodeBase64(str);
		ByteBuffer bb = ByteBuffer.wrap(bytes);
		UUID uuid = new UUID(bb.getLong(), bb.getLong());
		return uuid.toString();
	}

}
