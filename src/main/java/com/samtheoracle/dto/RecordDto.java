package com.samtheoracle.dto;

import com.samtheoracle.discovery.Status;

public class RecordDto {

	public LocationDto location;
	public String name;
	public String status;
	public String registration;

	public static class LocationDto{
		public Integer port;
		public String root;
		public String host;
		public Boolean ssl;
		public String endpoint;

		@Override
		public String toString() {
			return "LocationDto{" + "port=" + port + ", root='" + root + '\'' + ", host='" + host + '\'' + ", ssl=" + ssl + ", endpoint='"
					+ endpoint + '\'' + '}';
		}
	}

	@Override
	public String toString() {
		return "RecordDto{" +
				"location=" + location +
				", name='" + name + '\'' +
				", status='" + status + '\'' +
				", registration='" + registration + '\'' +
				'}';
	}
}
