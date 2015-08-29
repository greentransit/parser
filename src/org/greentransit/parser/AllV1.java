package org.greentransit.parser;

import org.greentransit.parser.my.MGenerator;

public class AllV1 {

	public static final String GTFS_DIR = "GTFS";
	public static final String DEST_DIR = "output";

	public static final String[] STM_BUS = new String[] { GTFS_DIR + "/CA_MONTREAL_STM/gtfs_stm-150831.zip", DEST_DIR, "ca_mtl_stm_bus_", "true", "false" };
	public static final String[] STM_SUBWAY = new String[] { GTFS_DIR + "/CA_MONTREAL_STM/gtfs_stm-150831.zip", DEST_DIR, "ca_mtl_stm_subway_", "true", "true" };

	public static void main(String[] args) {
		System.out.println("Generating all data...");
		long start = System.currentTimeMillis();
		System.out.println("----------");
		STMBus.main(STM_BUS); // STM - Bus
		System.out.println("----------");
		STMSubway.main(STM_SUBWAY); // STM - Subway
		System.out.println("----------");
		System.out.printf("DONE in %s seconds\n", MGenerator.getPrettyDuration(System.currentTimeMillis() - start));
	}
}
